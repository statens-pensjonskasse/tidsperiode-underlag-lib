package no.spk.pensjon.faktura.tidsserie.domain.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.internal.Aarsfaktor}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsfaktorTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeKunneVereStoerreEnn1() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere mindre enn eller lik 1, men var 2");
        new Aarsfaktor(2d);
    }

    @Test
    public void skalIkkjeKunneVereLik0() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere større enn 0, men var 0");
        new Aarsfaktor(0d);
    }

    @Test
    public void skalIkkjeKunneVereNegativ() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere større enn 0, men var -2");
        new Aarsfaktor(-2d);
    }
}