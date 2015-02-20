package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;

/**
 * Beregningsregel som reknar ut maskinelt grunnlag for ei {@link Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar underlagsperiodas andel av premie�rets totale maskinelle grunnlag.
     * <br>
     * Beregninga som blir foretatt er forel�pig ein forenkla variant som kun tar hensyn til
     * l�nnstillegg og deltidsjustert l�nn, enten innrapportert fr� arbeidsgivar eller utleda basert p�
     * innrapportert l�nnstrinn som er justert i henhold til stillingsprosent.
     * <p>
     * I motsetning til dei andre beregningsreglane, genererer vi her maskinelt grunnlag som er justert i henhold
     * til periodas �rsfaktor slik at klienten kan akkumulere delberegningane for �ret og ende opp med totalt
     * maskinelt grunnlag pr �r uten � m�tte foreta justering av returnert bel�p i henhold til periodas �rsfaktor.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av
     *                beregningsregelen
     * @return underlagsperiodas andel av det totale maskinelle grunnlaget for premie�ret
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        if (!periode.valgfriAnnotasjonFor(Medregning.class).isPresent()) {
            if (periode.beregn(MinstegrenseRegel.class).erUnderMinstegrensa(periode.annotasjonFor(Stillingsprosent.class))) {
                return kroner(0);
            }
        }
        return periode.beregn(AarsfaktorRegel.class).multiply(
                Kroner.min(
                        periode.beregn(DeltidsjustertLoennRegel.class)
                                .plus(
                                        // L�nstillegga blir justert i henhold til �rsfaktor av den andre regelen
                                        periode.beregn(LoennstilleggRegel.class)
                                )
                                .plus(
                                        periode.beregn(MedregningsRegel.class)
                                ),
                        periode
                                .beregn(OevreLoennsgrenseRegel.class)
                )
        );
    }
}
