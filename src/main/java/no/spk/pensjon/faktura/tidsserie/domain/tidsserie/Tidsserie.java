package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

/**
 * {@link Tidsserie} representerer algoritma som genererer ein ny tidsserie med observasjonar
 * for kvart stillingsforhold, avtale og observsjonsdato for kvart medlem.
 * <p>
 * F�rste steg ved generering av tidsseriar for medlemmet er generering av stillingsforholdunderlag for kvart
 * av stillingsforholda som medlemmet har vore aktiv p� i l�pet av observasjonsperioda. Kvart
 * stillingsforholdunderlag blir periodisert basert p� stillingsendringar / medregning og avtalekoblingar. I
 * tillegg blir tidsperiodiserte referansedata som �rsperioder, m�nedsperioder, l�nnstrinnperioder,
 * omregningsperioder, regelperioder m.m. inkludert og bidrar til periodiseringa av underlaget.
 * <p>
 * Outputen fr� f�rste steg blir eit stillingsforholdunderlag pr stillingsforhold. Dette underlaget er splitta
 * opp i underlagsperioder som alle strekker seg over ei periode p� minimum 1 dag og maksimum 1 m�ned.
 * <p>
 * Andre steg i tidsseriegenereginga er � bygge opp eit �rsunderlag for kvart unike �r som observasjonsperioda
 * strekker seg over. Intensjonen med dette underlaget er � tilrettelegge for beregningar som opererer p� �rsbasis
 * og der verdiane skal summerast saman / aggregerast til ein verdi pr �r. Det typiske eksempelet for denne typen
 * beregningar er maskinelt grunnlag / �rsl�nn.
 * <p>
 * Tredje steg tar �rsunderlaget og genererer opp 1-12 observasjonsunderlag basert p� dette. For kvar m�ned i �ret
 * som stillingsforholdet har vore aktivt eller for kvar m�ned resten av �ret etter stillingsforholdets siste
 * arbeidsdag, blir det generert eit nytt observasjonsunderlag som inneheld alle synlige endringar fram til og med
 * siste dag i m�neden observasjonsunderlaget skal observerast. I tillegg inneheld underlaget ei ny fiktiv periode
 * som strekker seg ut �ret og som er annotert med samme verdiar som siste synlige periode. Den fiktive perioda
 * representerer ei prognose for korleis ein p� observasjonsdatoen antar at stillingsforholdet kjem til � sj� ut
 * resten av �ret. Dei einaste situasjonen der ei slik fiktiv periode ikkje blir generert er for desember m�ned
 * eller for m�nedar der observasjonsdato ligg etter stillingsforholdet sluttdato viss stillinga blir avslutta
 * i l�pet av �ret.
 * <p>
 * Siste steg i tidsseriegenereringa er � utf�re ein eller fleire observasjonar for kvart av observasjonsunderlaga
 * og publisere observasjonane for vidare bearbeiding eller persistering via ein observasjonspublikator.
 *
 * @author Tarjei Skorgenes
 */
public class Tidsserie {
    private final StandardTidsserieAnnotering strategi = new StandardTidsserieAnnotering();

    private final Observasjonsunderlag observasjonsunderlag = new Observasjonsunderlag();

    private final Aarsunderlag aarsunderlag = new Aarsunderlag();

    private Feilhandtering feilhandtering = (s, u, t) -> {
        System.err.println("Generering av tidsserie feila for stillingsforhold " + s.id());
        System.err.println("Observasjonsunderlag: " + u);
        t.printStackTrace(System.err);
    };

    /**
     * Overstyrer feilhandteringsstrategien til tidsserien.
     * <p>
     * Alle {@link RuntimeException} eller subtyper av denne som blir kasta under generering av observasjonar
     * for eit bestemt stillingsforhold, vil medf�re at vidare prosessering av stillingsforholdet blir avbrutt
     * umiddelbart.
     * <p>
     * Kva som skal skje med feilen som avbryt prosesseringa blir handtert av feilhandteringsstrategien som ein her
     * overstyrer.
     *
     * @param feilhandtering den nye feilhandteringsstrategien som skal benyttast
     */
    public void overstyr(final Feilhandtering feilhandtering) {
        this.feilhandtering = requireNonNull(feilhandtering, () -> "Feilhandteringstrategien er p�krevd, men var null");
    }

    /**
     * Genererer nye tidsserar for kvart stillingsforhold tilknytta medlemmet og populerer tidsseriane
     * med observasjonar av maskinelt grunnlag pr stillingsforhold pr avtale pr �r.
     * <p>
     * Algoritma genererer tidsseriane pr stillingsforhold og tek forel�pig ikkje hensyn til om medlemmet har
     * fleire overlappande stillingsforhold.
     *
     * @param medlemsdata       medlemsdata for medlemmet som skal prosesserast
     * @param periode           observasjonsperioda som det skal genererast observasjonar pr siste dag i m�naden for
     * @param publikator        mottar kvar observasjon etterkvart som dei blir generert
     * @param referanseperioder tidsperiodiserte referanseperioder som skal inkluderast i periodiseringa
     *                          av stillingsforholdunderlaget
     */
    public void generer(final Medlemsdata medlemsdata, final Observasjonsperiode periode,
                        final Observasjonspublikator<TidsserieObservasjon> publikator,
                        final Stream<Tidsperiode<?>> referanseperioder) {
        final TidsserieUnderlagFacade fasade = new TidsserieUnderlagFacade();
        fasade.addReferansePerioder(referanseperioder);
        fasade.endreAnnoteringsstrategi(strategi);

        fasade.prosesser(medlemsdata, lagObservator(publikator), periode);
    }

    /**
     * Genererer ein ny callback som behandlar stillingsforholdunderlag og genererer m�nedlige observasjonar ved hjelp
     * av �rs- og observasjonsunderlag utleda fr� dette.
     * <p>
     * Kvar observasjon som blir generert blir publisert via <code>publikator</code>en.
     *
     * @param publikator publikatoren mottar alle observasjonane som blir generert av callbacken for kvart
     *                   stillingsforhold den prosesserer
     * @return ein ny callback som vil generere observasjonar for tidsserien
     */
    StillingsforholdUnderlagCallback lagObservator(final Observasjonspublikator<TidsserieObservasjon> publikator) {
        return (stillingsforhold, underlag) -> {
            try {
                aarsunderlag
                        .genererUnderlagPrAar(underlag)
                        .flatMap(observasjonsunderlag::genererUnderlagPrMaaned)
                        .flatMap(this::genererObservasjonPrAvtale)
                        .forEach(publikator::publiser);
            } catch (final RuntimeException e) {
                feilhandtering.handterFeil(stillingsforhold, underlag, e);
            }
        };
    }

    /**
     * Genererer ein ny observasjon pr avtale som er stillingsforholdet er aktivt p� i l�pet av premie�ret
     * som <code>observasjonsunderlag</code> inneheld underlagsperioder for.
     * <p>
     * For observasjonsunderlag der alle periodene er tilknytta ein go samme avtale, vil det kun bli returnert ein
     * observasjon.
     * <p>
     * For observasjonsunderlag der stillingsforholdet har vore gjennom eit eller fleire avtalebytte i l�pet
     * av premie�ret, vil det bli returnert ein observaasjon pr avtale som stillingsforholdet har vore
     * tilknytta i l�pet av premie�ret som observasjonsunderlaget representerer.
     *
     * @param observasjonsunderlag eit observasjonsunderlag som det skal genererast ein eller fleire
     *                             observasjonar av for tidsserien
     * @return ein straum med ein observasjon for stillingsforhold som ikkje har vore gjennom eit avtalebytte i
     * l�pet av premie�ret, eller ein straum med ein observasjon pr avtale stillingsforholdet har vore innom i
     * l�pet av premie�ret for stillingsforhold som har vore gjennom eit eller fleire avtalebytte
     */
    private Stream<TidsserieObservasjon> genererObservasjonPrAvtale(final Underlag observasjonsunderlag) {
        return observasjonsunderlag
                .stream()
                .map(observerPeriode(observasjonsunderlag))
                .collect(summerPrAvtale())
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Opprettar ein ny {@link Collector} som blir brukt for � gruppere og summere alle
     * {@link TidsserieObservasjon observasjonar} p� periodeniv� basert p� kvar periodeobservasjon sin avtale.
     *
     * @return ein ny collector som grupperer periodeobservasjonane pr avtale og sl�r dei saman til ein observasjon som
     * inneheld avtalens totalresultat for heile premie�ret
     * @see java.util.stream.Collectors#groupingBy(Function, Collector)
     * @see TidsserieObservasjon#plus(TidsserieObservasjon)
     */
    private static Collector<TidsserieObservasjon, ?, Map<AvtaleId, Optional<TidsserieObservasjon>>> summerPrAvtale() {
        return groupingBy(
                TidsserieObservasjon::avtale,
                reducing(TidsserieObservasjon::plus)
        );
    }

    /**
     * Genererer ein ny funksjon som genererer ein ny tidsserie-observasjon for ei underlagsperiode.
     * <p>
     * Observasjonane generert av denne metoda m� seinare grupperast pr avtale og summerast saman
     * for � sitte igjen med ein total observasjon av avtalen for heile premie�ret.
     * <p>
     * For underlag tilknytta stillingsforhold som har vore gjennom eit eller fleire avtalebytte,
     * vil grupperinga sikre at maskinelt grunnlag og dei andre m�lingane som inng�r i tidsserien, blir summert
     * pr avtale slik at det blir generert eit innslag i tidsserien for kvar avtale stillingsforholdet har vore
     * aktivt p� i l�pet av kvart premie�r.
     *
     * @param observasjonsunderlag observasjonsunderlaget som stillingsforhold og observasjonsdato blir henta fr�
     * @return ein ny funksjon som vil generere ein tidsserieobservasjon pr underlagsperiode den blir kalla p�
     */
    private static Function<Underlagsperiode, TidsserieObservasjon> observerPeriode(final Underlag observasjonsunderlag) {
        return p -> new TidsserieObservasjon(
                observasjonsunderlag.annotasjonFor(StillingsforholdId.class),
                p.annotasjonFor(AvtaleId.class),
                observasjonsunderlag.annotasjonFor(Observasjonsdato.class),
                p.beregn(MaskineltGrunnlagRegel.class)
        );
    }
}
