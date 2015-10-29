package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;

/**
 * Beregningsregel som reknar ut maskinelt grunnlag for ei {@link Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar underlagsperiodas andel av premieårets totale maskinelle grunnlag.
     * <br>
     * Beregninga som blir foretatt er foreløpig ein forenkla variant som kun tar hensyn til
     * lønnstillegg og deltidsjustert lønn, enten innrapportert frå arbeidsgivar eller utleda basert på
     * innrapportert lønnstrinn som er justert i henhold til stillingsprosent.
     * <p>
     * I motsetning til dei andre beregningsreglane, genererer vi her maskinelt grunnlag som er justert i henhold
     * til periodas årsfaktor slik at klienten kan akkumulere delberegningane for året og ende opp med totalt
     * maskinelt grunnlag pr år uten å måtte foreta justering av returnert beløp i henhold til periodas årsfaktor.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av
     *                beregningsregelen
     * @return underlagsperiodas andel av det totale maskinelle grunnlaget for premieåret
     */
    @Override
    public Kroner beregn(final Beregningsperiode<?> periode) {
        if (periode.beregn(ErUnderMinstegrensaRegel.class)) {
            return kroner(0);
        }
        return periode.beregn(AarsfaktorRegel.class).multiply(
                Kroner.min(
                        periode.beregn(DeltidsjustertLoennRegel.class)
                                .plus(
                                        // Lønstillegga blir justert i henhold til årsfaktor av den andre regelen
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
