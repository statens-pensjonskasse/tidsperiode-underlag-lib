package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Test;

public class LoennstilleggRegelTest {
    @Test
    public void skalGenerereTommeLoennstilleggVissStillingaErUteIPermisjonUtanLoenn() {
        assertLoennstillegg(
                eiPeriodeSomErEnDagLang()
                        .med(new Variabletillegg(kroner(10_000)))
                        .med(new Fastetillegg(kroner(20_000)))
                        .med(new Funksjonstillegg(kroner(30_000)))
                        .med(Aksjonskode.PERMISJON_UTAN_LOENN)
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at funksjonstillegg ikkje blir nedjustert i henhold til periodas årsfaktor på samme måte som for den
     * deltidsjusterte årslønna.
     */
    @Test
    public void skalNedjustereFunksjonstilleggEtterAarsfaktor() {
        assertLoennstillegg(
                eiPeriodeSomErEnDagLang()
                        .med(new Funksjonstillegg(kroner(365_000)))
        ).isEqualTo(kroner(365_000));
    }

    /**
     * Verifiserer at maskinelt grunnlag framleis kan beregnast når underlagsperioda ikkje er annotert med funksjonstillegg
     * slik at ein slepp å ta annotere perioder med funksjonstillegg lik 0 for alle stillingar som ikkje har nokon tillegg.
     * <p>
     * Intensjonen med å unngå å annotere når verdien er lik kr 0, er å oppføre oss konsistent med lønnstrinnhandteringa
     * for lønnstrinn 0, der vi også hoppar over og ikkje annoterer perioda med eit tomt lønnstrinn.
     */
    @Test
    public void skalIkkjeFeileDersomPeriodeIkkjeErAnnotertMedFunksjonstillegg() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .uten(Funksjonstillegg.class)
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at funksjonstillegg som er innrapportert blir brukt as is ved beregning av maskinelt grunnlag, uten
     * å forsøke å deltidjustere dei basert på stillingsprosenten. Dette fordi funksjonstillegg er uavhengig av
     * stillingsprosent og derfor aldri skal deltidsjusterast.
     */
    @Test
    public void skalIkkjeDeltidsjustereFunksjonstillegg() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Funksjonstillegg(kroner(14_400)))
        ).isEqualTo(kroner(14_400));
    }

    /**
     * Verifiserer at funksjonstillegg blir inkludert i sluttsummen ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereFunksjonstilleggIBeregningar() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Funksjonstillegg(kroner(14_400)))
        ).isEqualTo(kroner(14_400));
    }

    /**
     * Verifiserer at dei variable tillegga ikkje blir nedjustert i henhold til periodas årsfaktor på samme måte som for den
     * deltidsjusterte årslønna.
     */
    @Test
    public void skalIkkjeNedjustereVariableTilleggEtterAarsfaktor() {
        assertLoennstillegg(
                eiPeriodeSomErEnDagLang()
                        .med(new Variabletillegg(kroner(365_000)))
        ).isEqualTo(kroner(365_000));
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
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .uten(Variabletillegg.class)
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at dei variable tillegga som er innrapportert blir brukt as is ved beregning av maskinelt grunnlag, uten
     * å forsøke å deltidjustere dei basert på stillingsprosenten. Dette fordi variable tillegg blir innrapportert ferdig
     * deltidsjustert.
     */
    @Test
    public void skalIkkjeDeltidsjustereVariableTillegg() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Variabletillegg(kroner(14_400)))
        ).isEqualTo(kroner(14_400));
    }

    /**
     * Verifiserer at variable tillegg blir inkludert i sluttsummen ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereVariableTilleggIBeregningar() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Variabletillegg(kroner(14_400)))
        ).isEqualTo(kroner(14_400));
    }

    /**
     * Verifiserer at dei faste tillegga ikkje blir nedjustert i henhold til periodas årsfaktor på samme måte som for den
     * deltidsjusterte årslønna.
     */
    @Test
    public void skalIkkjeNedjustereFasteTilleggEtterAarsfaktor() {
        assertLoennstillegg(
                eiPeriodeSomErEnDagLang()
                        .med(new Fastetillegg(kroner(365_000)))
        ).isEqualTo(kroner(365_000));
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
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .uten(Fastetillegg.class)
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at dei faste tillegga som er innrapportert blir brukt as is ved beregning av maskinelt grunnlag, uten
     * å forsøke å deltidjustere dei basert på stillingsprosenten. Dette fordi faste tillegg blir innrapportert ferdig
     * deltidsjustert.
     */
    @Test
    public void skalIkkjeDeltidsjustereFasteTillegg() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Stillingsprosent(new Prosent("10%")))
                        .med(new Fastetillegg(kroner(16_457)))
        ).isEqualTo(kroner(16_457));
    }

    /**
     * Verifiserer at faste tillegg blir inkludert i sluttsummen ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereFasteTilleggIBeregningar() {
        assertLoennstillegg(
                eiPeriodeSomErEtAarLang()
                        .med(new Fastetillegg(kroner(16_457)))
        ).isEqualTo(kroner(16_457));
    }

    private static UnderlagsperiodeBuilder periode(final String fraOgMed, final String tilOgMed) {
        return Support.periode(fraOgMed, tilOgMed)
                .med(new Aarstall(2007))
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel())
                .med(new AarsfaktorRegel())
                .med(new LoennstilleggRegel())
                .med(new ErMedregningRegel())
                .med(new ErPermisjonUtanLoennRegel())
                ;
    }

    private static UnderlagsperiodeBuilder eiPeriodeSomErEtAarLang() {
        return periode("2007.01.01", "2007.12.31");
    }

    private static UnderlagsperiodeBuilder eiPeriodeSomErEnDagLang() {
        return periode("2007.01.01", "2007.01.01");
    }

    private static AbstractComparableAssert<?, Kroner> assertLoennstillegg(final UnderlagsperiodeBuilder periode) {
        return assertThat(beregn(periode));
    }

    private static Kroner beregn(final UnderlagsperiodeBuilder periode) {
        return periode.bygg().beregn(LoennstilleggRegel.class);
    }
}