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
     * Beregnar underlagsperiodas andel av premieårets totale maskinelle grunnlag.
     * <br>
     * Beregninga som blir foretatt er foreløpig ein forenkla variant som kun tar hensyn til
     * deltidsjustert lønn, enten innrapportert frå arbeidsgivar eller utleda basert på
     * innrapportert lønnstrinn som er justert i henhold til stillingsprosent.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av
     *                beregningsregelen
     * @return underlagsperiodas andel av det totale maskinelle grunnlaget for premieåret
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
