package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

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
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av
     *                beregningsregelen
     * @return underlagsperiodas andel av det totale maskinelle grunnlaget for premieåret
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        final Aarsfaktor aarsfaktor = periode
                .beregn(AarsfaktorRegel.class);
        return Kroner.min(
                aarsfaktor
                        .multiply(
                                periode.beregn(DeltidsjustertLoennRegel.class)
                        )
                        .plus(
                                // Lønstillegga blir justert i henhold til årsfaktor av den andre regelen
                                periode.beregn(LoennstilleggRegel.class)
                        )
                        .plus(
                                aarsfaktor.multiply(periode.beregn(MedregningsRegel.class))
                        )
                ,
                aarsfaktor
                        .multiply(
                                periode
                                        .beregn(OevreLoennsgrenseRegel.class)
                        )
        );
    }
}
