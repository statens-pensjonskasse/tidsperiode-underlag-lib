package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegelTest {
    @Test
    public void skalBeregneMaskineltGrunnlagLikDeltidsjustertLoennUtenAaTaHensynTilMinstegrense() {
        assertMaskineltGrunnlag(
                periode("2012.01.01", "2012.12.31")
                        .med(new Aarstall(2012))
                        .med(new DeltidsjustertLoenn(new Kroner(50_000)))
                        .med(new Stillingsprosent(new Prosent("10%")))
        ).isEqualTo(new Kroner(50_000));
    }

    @Test
    public void skalBeregneMaskineltGrunnlagLikDeltidsjustertLoennUtenAaTaHensynTilOevreGrenseForLoenn() {
        assertMaskineltGrunnlag(
                periode("2012.01.01", "2012.12.31")
                        .med(new Aarstall(2012))
                        .med(new DeltidsjustertLoenn(new Kroner(50_000_000)))
        ).isEqualTo(new Kroner(50_000_000));
    }

    @Test
    public void skalAvkorteMaskineltGrunnlagUtFraAarsfaktor() {
        assertMaskineltGrunnlag(
                periode("2011.01.01", "2011.01.31")
                        .med(new DeltidsjustertLoenn(new Kroner(365_000)))
                        .med(new Aarstall(2011))
        ).isEqualTo(new Kroner(31_000));
    }

    private static UnderlagsperiodeBuilder periode(final String fraOgMed, final String tilOgMed) {
        return Support.periode(fraOgMed, tilOgMed)
                .med(new MaskineltGrunnlagRegel())
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel())
                .med(new AarsfaktorRegel())
                .med(new Stillingsprosent(new Prosent("100%")));
    }

    private AbstractComparableAssert<?, Kroner> assertMaskineltGrunnlag(final UnderlagsperiodeBuilder periode) {
        return assertThat(periode.bygg().beregn(MaskineltGrunnlagRegel.class));
    }

}