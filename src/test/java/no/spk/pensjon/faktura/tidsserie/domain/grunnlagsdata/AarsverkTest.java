package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * {@link AarsverkTest} inneheld enheitstestane av årsverk.
 *
 * @author Tarjei Skorgenes
 */
public class AarsverkTest {
    @Rule
    public ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeTillateNullVerdiarVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("verdi er påkrevd, men var null");
        Aarsverk.aarsverk(null);
    }

    @Test
    public void skalReturnereSammeProsentVerdiSomVedKonstruksjon() {
        final Prosent expected = prosent("10%");
        assertThat(Aarsverk.aarsverk(expected).tilProsent()).isEqualTo(expected);
    }

    @Test
    public void skalLeggeSamanProsentVerdiane() {
        assertThat(aarsverk("10%").plus(aarsverk("20%")).tilProsent().toDouble())
                .isEqualTo(prosent("30%").toDouble(), offset(0.0001));
    }

    private Aarsverk aarsverk(final String tekst) {
        return Aarsverk.aarsverk(prosent(tekst));
    }
}