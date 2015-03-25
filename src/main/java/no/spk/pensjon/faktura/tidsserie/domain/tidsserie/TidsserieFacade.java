package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StillingsforholdunderlagFactory.AvtaleinformasjonRepository;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;

/**
 * {@link TidsserieFacade} representerer ei fasade som genererer ein ny tidsserie med observasjonar
 * basert p� medlems-, avtale- og l�nnsdata tilknytta eit medlem.
 * <p>
 * F�rste steg i genereringa av tidsseriar for medlemmet er generering av stillingsforholdunderlag for kvart
 * av stillingsforholda som medlemmet har vore aktiv p� i l�pet av observasjonsperioda. Kvart
 * stillingsforholdunderlag blir periodisert basert p� stillingsendringar / medregning og avtalekoblingar. I
 * tillegg blir tidsperiodiserte referansedata som �rsperioder, m�nedsperioder, l�nnstrinnperioder,
 * omregningsperioder, regelperioder og avtalerelaterte periodar for stillingas tilknytt avtalar inkludert og
 * bidrar til periodiseringa av underlaget.
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
 * Siste steg i tidsseriegenereringa er � fortl�pande publisere observasjonsunderlaga som blir generert, til ein
 * observasjonspublikator som er ansvarlig for � prosessere underlaga og periodene dei inneheld og generere dei
 * endelige observasjonane av desse.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieFacade {
    private final StandardTidsserieAnnotering strategi = new StandardTidsserieAnnotering();

    private final ObservasjonsunderlagFactory observasjonsunderlagFactory = new ObservasjonsunderlagFactory();

    private final AarsunderlagFactory aarsunderlagFactory = new AarsunderlagFactory();

    private AvtaleinformasjonRepository repository = avtale -> Stream.empty();

    private Feilhandtering feilhandtering = (s, u, t) -> {
        System.err.println("Generering av tidsserie feila for stillingsforhold " + s.id());
        System.err.println("Observasjonsunderlag: " + u);
        t.printStackTrace(System.err);
    };

    /**
     * Overstyrer repositoriet som tidsperiodisert avtaleinformasjon blir sl�tt opp via.
     *
     * @param repository repositoriet som avtaleinformasjon blir sl�tt opp via
     * @throws NullPointerException dersom <code>repository</code> er <code>null</code>
     */
    public void overstyr(final AvtaleinformasjonRepository repository) {
        this.repository = requireNonNull(repository, () -> "avtaleinformasjonrepository er p�krevd, men var null");
    }

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
     * <p>
     * Kvar observasjon som genererast blir sendt vidare til <code>publikator</code>. For � unng� at den p�virkar/senkar
     * ytelsen til prosesseringa anbefalast det sterkt at den utf�rer vidare behandling eller persistering av
     * observasjonane asynkront.
     *
     * @param publikator publikatoren som vil bli notifisert kvar gang ein ny observasjon blir genrerert i tidsseriane
     * for medlemmet, den er ansvarlig for all vidare behandling/persistering av observasjonane
     * @see #generer(Medlemsdata, Observasjonsperiode, StillingsforholdUnderlagCallback, Stream)
     * @see #lagObservator(Observasjonspublikator)
     */
    public void generer(final Medlemsdata medlemsdata, final Observasjonsperiode periode,
            final Observasjonspublikator publikator,
            final Stream<Tidsperiode<?>> referanseperioder) {
        generer(medlemsdata, periode, lagObservator(publikator), referanseperioder);
    }

    /**
     * Genererer nye stillingsforholdunderlag for kvart stillingsforhold tilknytta medlemmet og sender dei vidare til
     * <code>callback</code> for vidare behandling.
     *
     * @param medlemsdata medlemsdata for medlemmet som skal prosesserast
     * @param periode observasjonsperioda som det skal genererast observasjonar pr siste dag i m�naden for
     * @param callback callbacken som forl�pande tar i mot og behandlar vidare kvart stillingsforholdunderlag
     * som blir generert for medlemmet
     * @param referanseperioder tidsperiodiserte referanseperioder som skal inkluderast i periodiseringa
     * av stillingsforholdunderlaget
     */
    public void generer(final Medlemsdata medlemsdata, final Observasjonsperiode periode,
            final StillingsforholdUnderlagCallback callback,
            final Stream<Tidsperiode<?>> referanseperioder) {
        final StillingsforholdunderlagFactory fasade = new StillingsforholdunderlagFactory();
        fasade.addReferansePerioder(referanseperioder);
        fasade.endreAnnoteringsstrategi(strategi);
        fasade.endreAvtaleinformasjonRepository(repository);
        fasade.prosesser(medlemsdata, callback, periode);
    }

    /**
     * Genererer ein ny callback som behandlar stillingsforholdunderlag og genererer m�nedlige observasjonar ved hjelp
     * av �rs- og observasjonsunderlag utleda fr� dette.
     * <p>
     * Kvar observasjon som blir generert blir publisert via <code>publikator</code>en.
     *
     * @param publikator publikatoren mottar alle observasjonane som blir generert av callbacken for kvart
     * stillingsforhold den prosesserer
     * @return ein ny callback som vil generere observasjonar for tidsserien
     */
    public StillingsforholdUnderlagCallback lagObservator(final Observasjonspublikator publikator) {
        final GenererObservasjonPrStillingsforholdOgAvtale aggregator = new GenererObservasjonPrStillingsforholdOgAvtale(publikator::publiser);
        return (stillingsforhold, underlag) -> {
            try {
                aggregator.publiser(
                        aarsunderlagFactory
                                .genererUnderlagPrAar(underlag)
                                .flatMap(observasjonsunderlagFactory::genererUnderlagPrMaaned)
                );
            } catch (final RuntimeException e) {
                feilhandtering.handterFeil(stillingsforhold, underlag, e);
            }
        };
    }

}
