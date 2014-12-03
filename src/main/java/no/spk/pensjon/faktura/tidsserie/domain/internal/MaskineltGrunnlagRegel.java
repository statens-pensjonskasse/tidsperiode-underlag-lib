package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * Beregningsregel som reknar ut maskinelt grunnlag for ei
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegel implements BeregningsRegel<Kroner> {
    private static final Fastetillegg INGEN_FASTE_TILLEGG = new Fastetillegg(new Kroner(0));

    private static final Variabletillegg INGEN_VARIABLE_TILLEGG = new Variabletillegg(new Kroner(0));

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
                        grunnloenn(periode)
                                .plus(fastetillegg(periode))
                                .plus(variabletillegg(periode))
                );
    }

    private Kroner variabletillegg(final Underlagsperiode periode) {
        return periode.valgfriAnnotasjonFor(Variabletillegg.class).orElse(INGEN_VARIABLE_TILLEGG).beloep();
    }

    private Kroner grunnloenn(final Underlagsperiode periode) {
        return periode.beregn(DeltidsjustertLoennRegel.class).beloep();
    }

    private Kroner fastetillegg(final Underlagsperiode periode) {
        return periode.valgfriAnnotasjonFor(Fastetillegg.class).orElse(INGEN_FASTE_TILLEGG).beloep();
    }
}
