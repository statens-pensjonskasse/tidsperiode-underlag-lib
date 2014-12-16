package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MinstegrenseTest {
    @Test
    public void skalVereOverMinstegrensaOmVerdienErStoerreEnnGrenseverdien() {
        assertErUnderMinstegrense(minstegrense("35%"), "100%").isFalse();
        assertErUnderMinstegrense(minstegrense("35%"), "36%").isFalse();
    }

    @Test
    public void skalVereOverMinstegrensaOmVerdienErEksaktLikGrenseverdien() {
        assertErUnderMinstegrense(minstegrense("38.46%"), "38.46%").isFalse();
        assertErUnderMinstegrense(minstegrense("35%"), "35.00%").isFalse();
    }

    @Test
    public void skalVereUnderMinstegrensaOmVerdienErMindreEnnGrenseverdien() {
        assertErUnderMinstegrense(minstegrense("37.33%"), "0%").isTrue();
        assertErUnderMinstegrense(minstegrense("37.33%"), "1%").isTrue();
        assertErUnderMinstegrense(minstegrense("37.33%"), "37.32%").isTrue();
    }

    private static AbstractBooleanAssert<?> assertErUnderMinstegrense(
            final Minstegrense minstegrense, final String stillingsprosent) {
        return assertThat(minstegrense.erUnderMinstegrensa(stillingsprosent(stillingsprosent)))
                .as("er " + stillingsprosent + " under " + minstegrense);
    }

    private static Stillingsprosent stillingsprosent(final String prosent) {
        return new Stillingsprosent(new Prosent(prosent));
    }

    private static Minstegrense minstegrense(final String prosent) {
        return new Minstegrense(new Prosent(prosent));
    }
}