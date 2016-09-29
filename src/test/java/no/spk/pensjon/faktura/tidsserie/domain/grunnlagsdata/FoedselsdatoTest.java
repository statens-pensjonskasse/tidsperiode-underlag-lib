package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FoedselsdatoTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at fødselsdatoar henta frå personnummeret til personar som har D-nummer, blir godtatt
     * som gyldige fødselsdatoar.
     */
    @Test
    public void skalGodtaFiktiveDagsverdiarFraDNummer() {
        final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

        final Year aar = Year.of(1980);
        IntStream
                .rangeClosed(1, 366)
                .mapToObj(dag -> LocalDate.ofYearDay(aar.getValue(), dag))
                .map(format::format)
                .forEach(datoSomTekst -> {
                    final int dNummer = Integer.parseInt(datoSomTekst) + 40;
                    assertFoedselsdatoSomTall(dNummer)
                            .isEqualTo(Integer.toString(dNummer));
                });
    }

    /**
     * Verifiserer at fødselsdatoar henta frå personnummeret til personar som har H-nummer, blir godtatt
     * som gyldige fødselsdatoar.
     */
    @Test
    public void skalGodtaFiktiveMaanedsverdiarFraHNummer() {
        final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

        final Year aar = Year.of(1980);
        IntStream
                .rangeClosed(1, 366)
                .mapToObj(dag -> LocalDate.ofYearDay(aar.getValue(), dag))
                .map(format::format)
                .forEach(datoSomTekst -> {
                    final int hNummer = Integer.parseInt(datoSomTekst) + 4000;
                    assertFoedselsdatoSomTall(hNummer)
                            .isEqualTo(Integer.toString(hNummer));
                });
    }

    @Test
    public void skalKonvertereDatoTilTall() {
        assertFoedselsdatoSomTall(19790806).isEqualTo("19790806");
        assertFoedselsdatoSomTall(19170101).isEqualTo("19170101");
        assertFoedselsdatoSomTall(18750101).isEqualTo("18750101");
        assertFoedselsdatoSomTall(99990101).isEqualTo("99990101");
    }

    /**
     * Vi reknar med fødselsnummer har blitt utvida til meir enn dagens 13-siffer innen denne datoen, if not blir
     * problemet ein får i den fjerne framtid at fødselsnummer blir 14-siffer på grunn av årstallet.
     */
    @Test
    public void skalSkapeProblemForFoedselsnummerEtterAar10000() {
        assertFoedselsdatoSomTall(100000101).isEqualTo("100000101");
    }

    @Test
    public void skalForkasteDatoarEldreEnn1875() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("fødselsdatoar eldre enn 18750101 er ikkje støtta");
        e.expectMessage("var 18741231");
        foedselsdato(18741231);
    }

    private AbstractCharSequenceAssert<?, String> assertFoedselsdatoSomTall(final int tall) {
        return assertFoedselsdatoSomTall(foedselsdato(tall));
    }

    private static AbstractCharSequenceAssert<?, String> assertFoedselsdatoSomTall(Foedselsdato value) {
        return assertThat(value.tilKode()).as("tall-representasjon av " + value);
    }
}