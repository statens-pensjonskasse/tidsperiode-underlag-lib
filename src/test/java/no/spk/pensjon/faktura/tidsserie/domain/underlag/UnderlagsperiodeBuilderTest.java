package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

public class UnderlagsperiodeBuilderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalAnnoterePeriodaBasertPaaVerdiensType() {
        final Underlagsperiode periode = bygg(
                builder()
                        .med(new Integer(0))
        );
        assertThat(periode.annotasjonFor(Integer.class)).isEqualTo(new Integer(0));
    }

    /**
     * Verifiserer at {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder#med(Object)}
     * kun annoterer perioda basert på verdiens eksakte {@link Object#getClass()}, ikkje eventuelle interface eller
     * superklasser som verdien implementerer eller arvar frå, direkte eller indirekte.
     */
    @Test
    public void skalIkkjeAnnoterePeriodeMedVerdiensSuperTyper() {
        final Underlagsperiode periode = bygg(builder()
                        .med(new Integer(0))
        );
        assertThat(periode.valgfriAnnotasjonFor(Number.class).isPresent()).isFalse();
        assertThat(periode.valgfriAnnotasjonFor(Object.class).isPresent()).isFalse();
    }

    private static UnderlagsperiodeBuilder builder() {
        return new UnderlagsperiodeBuilder().fraOgMed(dato("2001.01.01")).tilOgMed(dato("2001.12.31"));
    }

    private static Underlagsperiode bygg(final UnderlagsperiodeBuilder builder) {
        return builder.bygg();
    }
}