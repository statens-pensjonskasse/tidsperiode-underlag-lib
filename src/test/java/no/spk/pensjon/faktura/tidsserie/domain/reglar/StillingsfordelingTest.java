package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

public class StillingsfordelingTest {
    private Stillingsfordeling fordeling;

    @Before
    public void _before() {
        fordeling = new Stillingsfordeling();
    }

    @Test
    public void skalAvkorteBasertPaaInnleggingsRekkefoelgeIkkjeSortertStoerrelse() {
        leggTil(2L, "47%");
        leggTil(1L, "100%");

        assertAndelForStilling(2L).isEqualTo(of("47%"));
        assertAndelForStilling(1L).isEqualTo(of("53%"));
    }

    @Test
    public void skalMarkereAlleStillingarSomFakturerbareVissAlleHarYskOgTotalStillingsprosentIkkjeOverstig100Prosent() {
        leggTil(1L, "50%");
        leggTil(2L, "20%");
        leggTil(3L, "30%");

        assertAndelForStilling(1L).isEqualTo(of("50%"));
        assertAndelForStilling(2L).isEqualTo(of("20%"));
        assertAndelForStilling(3L).isEqualTo(of("30%"));
    }

    @Test
    public void skalMarkereAlleStillingarEtterTotalStillingsprosentOverstig100ProsentSomIkkjeFakturerbare() {
        leggTil(1L, "80%");
        leggTil(2L, "20%");
        leggTil(3L, "30%");

        assertAndelForStilling(1L).isEqualTo(of("80%"));
        assertAndelForStilling(2L).isEqualTo(of("20%"));
        assertAndelForStilling(3L).isEqualTo(of("0%"));
    }

    @Test
    public void skalAvkorteAndelForStillingSomFoererTilAtTotalStillingsprosentOverstig100Prosent() {
        leggTil(1L, "77.3%");
        leggTil(2L, "40%");

        assertAndelForStilling(1L).isEqualTo(of("77,3%"));
        assertAndelForStilling(2L).isEqualTo(of("22,7%"));

        fordeling.clear();

        leggTil(1L, "100%");
        leggTil(2L, "40%");

        assertAndelForStilling(1L).isEqualTo(of("100%"));
        assertAndelForStilling(2L).isEqualTo(of("0%"));

        fordeling.clear();

        leggTil(1L, "0.0001%");
        leggTil(2L, "100%");

        assertAndelForStilling(2L).isEqualTo(of("100%"));
        assertAndelForStilling(1L).isEqualTo(of("0%"));
    }

    @Test
    public void skalEkskludereMedregningFraaFordelinga() {
        leggTilMedregning(3L);
        leggTil(2L, "50%");
        leggTilMedregning(1L);

        assertAndelForStilling(2L).isEqualTo(of("50%"));
        assertAndelForStilling(1L).isEqualTo(of("0%"));
        assertAndelForStilling(3L).isEqualTo(of("0%"));
    }

    private void leggTilMedregning(final long stillingsforhold) {
        fordeling.leggTil(new AktivStilling(stillingsforhold(stillingsforhold), empty()));
    }

    private void leggTil(final long stillingsforhold, final String stillingsprosent) {
        fordeling.leggTil(new AktivStilling(stillingsforhold(stillingsforhold), of(prosent(stillingsprosent))));
    }

    private AbstractObjectAssert<?, ?> assertAndelForStilling(final long stillingsforhold) {
        return assertThat(fordeling.andelFor(stillingsforhold(stillingsforhold)).map(Prosent::toString))
                .as(stillingsforhold(stillingsforhold) + " sin andel av fordelinga " + fordeling);
    }

    private static Offset<Double> offset() {
        return Offset.offset(0.0001);
    }
}