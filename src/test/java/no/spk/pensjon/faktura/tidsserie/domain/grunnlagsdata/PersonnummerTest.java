package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PersonnummerTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at personnummer alltid blir representert som ei 5-sifra kode sjølv om tallverdien til personnummeret
     * tilfeldigvis kun har 1-4 siffer i seg.
     */
    @Test
    public void skalAlltidRepresenterePersonnummerMed5Siffer() {
        assertPersonnummer(1).isEqualTo("00001");
        assertPersonnummer(10).isEqualTo("00010");
        assertPersonnummer(100).isEqualTo("00100");
        assertPersonnummer(1000).isEqualTo("01000");
        assertPersonnummer(10000).isEqualTo("10000");
    }

    @Test
    public void skalAlltidVereEitPositivtHeiltal() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("personnummer må vere eit positivt heiltal");
        e.expectMessage("var -1");
        new Personnummer(-1);
    }

    @Test
    public void skalIkkjeGodtaVerdiarLengreEnn5Siffer() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("personnummer kan ikkje vere meir enn 5 siffer langt");
        e.expectMessage("var 100000");
        new Personnummer(1000000);
    }

    @Test
    public void skalGodtaAlle1Til5SifraTall() {
        for (int i = 0; i < 99999; i++) {
            new Personnummer(i);
        }
    }

    private static AbstractCharSequenceAssert<?, String> assertPersonnummer(final int value) {
        return assertThat(new Personnummer(value).toString());
    }
}