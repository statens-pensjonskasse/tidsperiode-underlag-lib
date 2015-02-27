package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn}.
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoennTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeKunneKonstruereMedNullBeloep() {
        e.expect(NullPointerException.class);
        e.expectMessage("beløp er påkrevd, men var null");
        new DeltidsjustertLoenn(null);
    }
}