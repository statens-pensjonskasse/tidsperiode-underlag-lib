package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

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

        fasade.prosesser(medlemsdata, (stillingsforhold, underlag) -> {
            try {
                aarsunderlag
                        .genererUnderlagPrAar(underlag)
                        .flatMap(observasjonsunderlag::genererUnderlagPrMaaned)
                        .flatMap(this::genererObservasjonPrAvtale)
                        .forEach(publikator::publiser);
            } catch (final RuntimeException e) {
                e.printStackTrace(System.err);
            }
        }, periode);
    }

    private Stream<TidsserieObservasjon> genererObservasjonPrAvtale(final Underlag observasjonsunderlag) {
        return Stream.of(
                observasjonsunderlag
                        .stream()
                        .map(p -> new TidsserieObservasjon(
                                        observasjonsunderlag.annotasjonFor(StillingsforholdId.class),
                                        p.annotasjonFor(AvtaleId.class),
                                        observasjonsunderlag.annotasjonFor(Observasjonsdato.class),
                                        p.beregn(MaskineltGrunnlagRegel.class)
                                )
                        )
                        .reduce(TidsserieObservasjon::plus)
        )
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
