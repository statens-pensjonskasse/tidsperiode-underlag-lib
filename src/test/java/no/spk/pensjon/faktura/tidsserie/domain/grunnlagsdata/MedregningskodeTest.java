package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;

import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.BISTILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.TILLEGG_ANNEN_ARBGIV;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

public class MedregningskodeTest {
    @Test
    public void skalKunneFakturereBistillingar() {
        assertErFakturerbar(BISTILLING).isTrue();
    }

    @Test
    public void skalKunneFakturereLoennHosAnnanArbeidsgivar() {
        assertErFakturerbar(TILLEGG_ANNEN_ARBGIV).isTrue();
    }

    @Test
    public void skalIkkjeKunneFakturereAndreMedregningskoder() {
        rangeClosed(0, 99).filter(kode -> kode != 12 && kode != 14).mapToObj(Medregningskode::valueOf).forEach(kode -> {
            assertErFakturerbar(kode).isFalse();
        });
    }

    @Test
    public void skalVereBistilling() {
        assertErBistilling(BISTILLING).isTrue();
        assertErBistilling(valueOf(12)).isTrue();
        assertErBistilling(TILLEGG_ANNEN_ARBGIV).isFalse();
    }

    @Test
    public void skalVereLoennAnnenArbeidsgiver() {
        assertErTilleggAnnenArbeidsgiver(TILLEGG_ANNEN_ARBGIV).isTrue();
        assertErTilleggAnnenArbeidsgiver(valueOf(TILLEGG_ANNEN_ARBGIV.kode())).isTrue();
        assertErTilleggAnnenArbeidsgiver(BISTILLING).isFalse();
    }

    private static AbstractBooleanAssert<?> assertErTilleggAnnenArbeidsgiver(final Medregningskode kode) {
        return assertThat(kode.erTilleggAnnenArbeidsgiver()).as("er " + kode + " tillegg hos annan arbeidsgivar?");
    }

    private static AbstractBooleanAssert<?> assertErBistilling(final Medregningskode kode) {
        return assertThat(kode.erBistilling()).as("er " + kode + " bistilling?");
    }

    private static AbstractBooleanAssert<?> assertErFakturerbar(Medregningskode kode) {
        return assertThat(kode.erFakturerbar()).as("er " + kode + " fakturerbar?");
    }
}