package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FoedselsdatoTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKonvertereDatoTilTall() {
        assertFoedselsdatoSomTall("1979.08.06").isEqualTo("19790806");
        assertFoedselsdatoSomTall("1917.01.01").isEqualTo("19170101");
        assertFoedselsdatoSomTall("1875.01.01").isEqualTo("18750101");
        assertFoedselsdatoSomTall("9999.01.01").isEqualTo("99990101");
    }

    /**
     * Vi reknar med fødselsnummer har blitt utvida til meir enn dagens 13-siffer innen denne datoen, if not blir
     * problemet ein får i den fjerne framtid at fødselsnummer blir 14-siffer på grunn av årstallet.
     */
    @Test
    public void skalSkapeProblemForFoedselsnummerEtterAar10000() {
        assertFoedselsdatoSomTall(LocalDate.of(10_000, 1, 1)).isEqualTo("100000101");
    }

    @Test
    public void skalForkasteDatoarEldreEnn1875() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("fødselsdatoar eldre enn 1875.01.01 er ikkje støtta");
        e.expectMessage("var 1874-12-31");
        new Foedselsdato(dato("1874.12.31"));
    }

    private static AbstractCharSequenceAssert<?, String> assertFoedselsdatoSomTall(final String dato) {
        return assertFoedselsdatoSomTall(dato(dato));
    }

    private static AbstractCharSequenceAssert<?, String> assertFoedselsdatoSomTall(final LocalDate dato) {
        final Foedselsdato value = new Foedselsdato(dato);
        return assertThat(value.tilKode()).as("tall-representasjon av " + value);
    }

}