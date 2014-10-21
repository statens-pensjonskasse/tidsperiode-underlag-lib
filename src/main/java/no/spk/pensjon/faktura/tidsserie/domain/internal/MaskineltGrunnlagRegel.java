package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * Beregningsregel som reknar ut maskinelt grunnlag for ei
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar underlagsperiodas andel av premie�rets totale maskinelle grunnlag.
     * <br>
     * Beregninga som blir foretatt er forel�pig ein forenkla variant som kun tar hensyn til
     * deltidsjustert l�nn, enten innrapportert fr� arbeidsgivar eller utleda basert p�
     * innrapportert l�nnstrinn som er justert i henhold til stillingsprosent.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av
     *                beregningsregelen
     * @return underlagsperiodas andel av det totale maskinelle grunnlaget for premie�ret
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        return periode
                .beregn(AarsfaktorRegel.class)
                .multiply(
                        periode
                                .beregn(DeltidsjustertLoennRegel.class)
                                .beloep
                );
    }
}
