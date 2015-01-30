package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

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
        new Aarsverk(null);
    }

    @Test
    public void skalReturnereSammeProsentVerdiSomVedKonstruksjon() {
        final Prosent expected = new Prosent("10%");
        assertThat(new Aarsverk(expected).tilProsent()).isEqualTo(expected);
    }
}