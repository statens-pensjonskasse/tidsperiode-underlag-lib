package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Set;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId}.
 *
 * @author Tarjei Skorgenes
 */
public class AvtaleIdTest {
    @Rule
    public ExpectedException e = ExpectedException.none();

    @Test
    public void skalKreveAvtalenummerVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("avtalenummer");
        e.expectMessage("er påkrevd, men var null");

        new AvtaleId(null);
    }

    /**
     * Kort fortalt: Grovverifiserer at equal og hashcode er implementert korrekt slik at
     * ein kan legge inn avtalenummer i eit set og vere sikker på at det ikkje vil inneholde nokon duplikat.
     */
    @Test
    public void skalKunneLeggastIEitSetMedUnikeVerdiar() {
        final Set<AvtaleId> idar = new HashSet<>();
        idar.add(valueOf(1L));
        idar.add(valueOf(2L));
        idar.add(valueOf(3L));
        idar.add(valueOf(1L));
        idar.add(valueOf(2L));
        idar.add(valueOf(3L));
        assertThat(idar).hasSize(3).containsOnly(valueOf(1L), valueOf(2L), valueOf(3L));
    }
}