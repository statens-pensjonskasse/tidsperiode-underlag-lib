package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;

import org.assertj.core.api.OptionalAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AktivStillingTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKreveAksjonskodeForStillingarBasertPaaStillingsendring() {
        e.expect(IllegalStateException.class);
        e.expectMessage("stillingsforhold som ikkje er medregningsbaserte må ha både stillingsprosent og aksjonskode");
        new AktivStilling(
                stillingsforhold(1L),
                of(fulltid().prosent()),
                empty()
        );
    }

    @Test
    public void skalKreveStillingsprosentOgAksjonskodeForStillingarBasertPaaStillingsendring() {
        e.expect(IllegalStateException.class);
        e.expectMessage("stillingsforhold som ikkje er medregningsbaserte må ha både stillingsprosent og aksjonskode");
        new AktivStilling(
                stillingsforhold(1L),
                Optional.<Prosent>empty(),
                of(Aksjonskode.ENDRINGSMELDING)
        );
    }

    @Test
    public void skalEndreStillingsprosentKunForStillingarBasertPaaStillingsendringar() {
        final Prosent expected = prosent("50%");
        assertStillingsprosent(
                new AktivStilling(
                        stillingsforhold(2L),
                        of(fulltid().prosent()),
                        of(Aksjonskode.ENDRINGSMELDING)
                )
                        .juster(expected)
        ).isEqualTo(of(expected));

        assertStillingsprosent(
                new AktivStilling(
                        stillingsforhold(2L),
                        empty(),
                        empty()
                )
                        .juster(expected)
        ).isEqualTo(empty());
    }

    private static OptionalAssert<Prosent> assertStillingsprosent(final AktivStilling stilling) {
        return assertThat(stilling.stillingsprosent())
                .as("stillingsprosent for stilling " + stilling);
    }
}