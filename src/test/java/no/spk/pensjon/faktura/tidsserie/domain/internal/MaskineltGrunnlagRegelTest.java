package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegelTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at dei variable tillegga blir nedjustert i henhold til periodas årsfaktor på samme måte som for den
     * deltidsjusterte årslønna.
     */
    @Test
    public void skalNedjustereVariableTilleggEtterAarsfaktor() {
        assertMaskineltGrunnlag(
                periode("2007.01.01", "2007.01.01")
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .med(new Aarstall(2007))
                        .med(new DeltidsjustertLoenn(kroner(36_500)))
                        .med(new Variabletillegg(kroner(365_000)))
        ).isEqualTo(kroner(100).plus(kroner(1_000)));
    }

    /**
     * Verifiserer at maskinelt grunnlag framleis kan beregnast når underlagsperioda ikkje er annotert med variable tillegg
     * slik at ein slepp å ta annotere perioder med variable tillegg lik 0 for alle stillingar som ikkje har nokon tillegg.
     * <p>
     * Intensjonen med å unngå å annotere når verdien er lik kr 0, er å oppføre oss konsistent med lønnstrinnhandteringa
     * for lønnstrinn 0, der vi også hoppar over og ikkje annoterer perioda med eit tomt lønnstrinn.
     */
    @Test
    public void skalIkkjeFeileDersomPeriodeIkkjeErAnnotertMedVariableTillegg() {
        assertMaskineltGrunnlag(
                periode("2007.01.01", "2007.12.31")
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .med(new Aarstall(2007))
                        .med(new DeltidsjustertLoenn(kroner(403_700)))
                        .uten(Variabletillegg.class)
        ).isEqualTo(kroner(403_700));
    }

    /**
     * Verifiserer at dei variable tillegga som er innrapportert blir brukt as is ved beregning av maskinelt grunnlag, uten
     * å forsøke å deltidjustere dei basert på stillingsprosenten. Dette fordi variable tillegg blir innrapportert ferdig
     * deltidsjustert.
     */
    @Test
    public void skalIkkjeDeltidsjustereVariableTillegg() {
        assertMaskineltGrunnlag(
                periode("2007.01.01", "2007.12.31")
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Aarstall(2007))
                        .med(new DeltidsjustertLoenn(kroner(403_700)))
                        .med(new Variabletillegg(kroner(14_400)))
        ).isEqualTo(kroner(403_700).plus(kroner(14_400)));
    }

    /**
     * Verifiserer at variable tillegg blir inkludert i sluttsummen ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereVariableTilleggIBeregningar() {
        assertMaskineltGrunnlag(
                periode("2007.01.01", "2007.12.31")
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .med(new Aarstall(2007))
                        .med(new DeltidsjustertLoenn(kroner(403_700)))
                        .med(new Variabletillegg(kroner(14_400)))
        ).isEqualTo(kroner(403_700).plus(kroner(14_400)));
    }

    @Test
    @Ignore
    public void skalInkludereFunksjonsTilleggIBeregningar() {
    }

    /**
     * Verifiserer at dei faste tillegga blir nedjustert i henhold til periodas årsfaktor på samme måte som for den
     * deltidsjusterte årslønna.
     */
    @Test
    public void skalNedjustereFasteTilleggEtterAarsfaktor() {
        assertMaskineltGrunnlag(
                periode("2006.01.01", "2006.01.01")
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .med(new Aarstall(2006))
                        .med(new DeltidsjustertLoenn(kroner(365_000)))
                        .med(new Fastetillegg(kroner(365_000)))
        ).isEqualTo(kroner(1_000).plus(kroner(1_000)));
    }

    /**
     * Verifiserer at maskinelt grunnlag framleis kan beregnast når underlagsperioda ikkje er annotert med faste tillegg
     * slik at ein slepp å ta annotere perioder med fast tillegg lik 0 for alle stillingar som ikkje har nokon tillegg.
     * <p>
     * Intensjonen med å unngå å annotere når verdien er lik kr 0, er å oppføre oss konsistent med lønnstrinnhandteringa
     * for lønnstrinn 0, der vi også hoppar over og ikkje annoterer perioda med eit tomt lønnstrinn.
     */
    @Test
    public void skalIkkjeFeileDersomPeriodeIkkjeErAnnotertMedFasteTillegg() {
        assertMaskineltGrunnlag(
                periode("2006.01.01", "2006.12.31")
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Aarstall(2006))
                        .med(new DeltidsjustertLoenn(kroner(372_000)))
                        .uten(Fastetillegg.class)
        ).isEqualTo(kroner(372_000));
    }

    /**
     * Verifiserer at dei faste tillegga som er innrapportert blir brukt as is ved beregning av maskinelt grunnlag, uten
     * å forsøke å deltidjustere dei basert på stillingsprosenten. Dette fordi faste tillegg blir innrapportert ferdig
     * deltidsjustert.
     */
    @Test
    public void skalIkkjeDeltidsjustereFasteTillegg() {
        assertMaskineltGrunnlag(
                periode("2006.01.01", "2006.12.31")
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Aarstall(2006))
                        .med(new DeltidsjustertLoenn(kroner(372_000)))
                        .med(new Fastetillegg(kroner(16_457)))
        ).isEqualTo(kroner(372_000).plus(kroner(16_457)));
    }

    /**
     * Verifiserer at faste tillegg blir inkludert i sluttsummen ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereFasteTilleggIBeregningar() {
        assertMaskineltGrunnlag(
                periode("2006.01.01", "2006.12.31")
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .med(new Aarstall(2006))
                        .med(new DeltidsjustertLoenn(kroner(372_000)))
                        .med(new Fastetillegg(kroner(16_457)))
        ).isEqualTo(kroner(372_000).plus(kroner(16_457)));
    }

    @Test
    public void skalBeregneDeltidsjustertLoennViaAnnanRegel() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage(DeltidsjustertLoennRegel.class.getSimpleName());

        beregn(
                periode("2013.01.01", "2013.12.31")
                        .med(new Aarstall(2013))
                        .uten(DeltidsjustertLoennRegel.class)
        ).multiply(0d);
    }

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
                .med(new DeltidsjustertLoennRegel())
                .med(new Stillingsprosent(new Prosent("100%")));
    }

    private AbstractComparableAssert<?, Kroner> assertMaskineltGrunnlag(final UnderlagsperiodeBuilder periode) {
        return assertThat(beregn(periode));
    }

    private Kroner beregn(final UnderlagsperiodeBuilder periode) {
        return periode.bygg().beregn(MaskineltGrunnlagRegel.class);
    }

}