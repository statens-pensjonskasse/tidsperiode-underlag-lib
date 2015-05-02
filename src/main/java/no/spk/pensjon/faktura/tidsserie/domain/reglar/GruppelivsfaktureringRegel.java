package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;

import java.util.function.Predicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;

/**
 * Regel med strategien som styrer kva underlagsperioder som skal fakturerast for gruppelivsproduktet.
 * <p>
 * Gruppeliv er eit forsikringsprodukt der beregningane av premie m� skje pr medlem, ikkje pr stillingsforhold.
 * Gruppelivspremien blir betalt basert p� antall dagar medlemmet er aktivt i l�pet av eit �r.
 * <p>
 * Sidan premien er p� medlemsniv� m� ein for perioder der medlemmet har fleire parallelle stillingar, ha ein
 * strategi for kva for eit stillingsforhold og avtale som skal betale premien for dagane i perioda.
 * <p>
 * SPK har valgt � bruke stillingsst�rrelse som strategi for kva stillinga og avtale som skal dekke inn premien.
 * <p>
 * Regelen er som f�lger:
 * <ol>
 * <li>Stillingar tilknytta avtalar utan gruppelivsprodukt hos SPK skal aldri betale gruppelivspremie.</li>
 * <li>Stillinga med st�rst stillingsprosent og der avtalen har gruppelivsprodukt, skal vere ansvarlig for periodas
 * gruppelivspremie</li>
 * <li>Variant: I f�rre punkt, viss fleire stillingar har stillingsprosent lik medlemmet si st�rste stilling i perioda,
 * blir ei av stillingane tilfeldig valgt som ansvarlig for periodas gruppelivspermie</li>
 * </ol>
 *
 * @author Tarjei Skorgenes
 */
public class GruppelivsfaktureringRegel implements BeregningsRegel<GruppelivsfaktureringStatus> {
    private static final Prosent FULLTID = new Prosent("100%");

    /**
     * Beregnar korvidt det skal trekkast inn gruppelivspremie for underlagsperioda.
     * <p>
     * Gjeldande strategi for fakturering av gruppelivspremie seier at kun ei stilling skal fakturerast pr periode,
     * dette blir oppn�dd ved � justere alle aktive stillingar opp til 100%. Dermed vil stillingsfordelinga ende opp
     * med � avkorte alle stillingar etter f�rste f�rste stilling til 0%. Sidan aktive stillingar er sortert p�
     * reell stillingsst�rrelse i forkant av denne oppjusteringa, medf�rer det at stillinga med den
     * h�gaste reelle stillingsst�rrelsen endar opp med 100% av fordelinga og blir einaste fakturerbare stilling for
     * perioda.
     *
     * @param periode beregningsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av
     *                beregningsregelen
     * @return status p� korvidt avtalen periodas stillingsforhold er tilknytta, skal betale gruppelivspremien
     * @throws PaakrevdAnnotasjonManglarException viss perioda ikkje er annotert med {@link StillingsforholdId},
     *                                            {@link Medlemsavtalar} eller {@link AktiveStillingar}
     */
    @Override
    public GruppelivsfaktureringStatus beregn(final Beregningsperiode<?> periode) throws PaakrevdAnnotasjonManglarException {
        final Medlemsavtalar avtalar = periode.annotasjonFor(Medlemsavtalar.class);
        final Predicate<AktivStilling> harGruppeliv = s -> avtalar.betalarTilSPKFor(s.stillingsforhold(), GRU);

        final StillingsforholdId stilling = periode.annotasjonFor(StillingsforholdId.class);
        return new GruppelivsfaktureringStatus(
                stilling,
                periode.annotasjonFor(AktiveStillingar.class)
                        .stillingar()
                        .filter(harGruppeliv)
                        .map(this::oppjusterTilFulltid)
                        .reduce(
                                new Stillingsfordeling(),
                                Stillingsfordeling::leggTil,
                                Stillingsfordeling::kombiner
                        )
                        .andelFor(stilling)
                        .orElse(ZERO) // Medregning manglar stillingsprosent og skal heller aldri belastast for GRU
        );
    }

    private AktivStilling oppjusterTilFulltid(final AktivStilling stilling) {
        return new AktivStilling(
                stilling.stillingsforhold(),
                stilling
                        .stillingsprosent()
                        .map(p -> FULLTID));
    }
}
