package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdIdTest {
    /**
     * Kort fortalt: Grovverifiserer at equal og hashcode er implementert korrekt slik at
     * ein kan legge inn stillingsforholdid i eit set og vere sikker på at det ikkje vil inneholde nokon duplikat.
     */
    @Test
    public void skalKunneLeggastIEitSetMedUnikeVerdiar() {
        final Set<StillingsforholdId> idar = new HashSet<>();
        idar.add(valueOf(1L));
        idar.add(valueOf(2L));
        idar.add(valueOf(3L));
        idar.add(valueOf(1L));
        idar.add(valueOf(2L));
        idar.add(valueOf(3L));
        assertThat(idar).hasSize(3).containsOnly(valueOf(1L), valueOf(2L), valueOf(3L));
    }
}