package no.spk.pensjon.faktura.tidsserie;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.Datoar}.
 *
 * @author Tarjei Skorgenes
 */
public class DatoarTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKonvertere10SifraDatoarTilKorrektDato() {
        assertThat(dato("1970.05.04")).isEqualTo(LocalDate.of(1970, 05, 04));
    }

    @Test
    public void skalKonvertere8SifraDatoarTilKorrektDato() {
        assertThat(dato("19710731")).isEqualTo(LocalDate.of(1971, 07, 31));
    }

    /**
     * Verifiserer at tom streng eller <code>null</code> fører til at <code>null</code> blir returnert.
     * <br>
     * Dette er omtrent den einaste jævla plassen i kodebasen der vi brukar null som returverdi, i etterpåklokskapens
     * lys var kanskje ikkje det den beste ideen ever.
     */
    @Test
    public void skalBehandleTomStrengSomNull() {
        assertThat(dato(null)).isNull();
        assertThat(dato(" ")).isNull();
        assertThat(dato("        ")).isNull();
        assertThat(dato("          ")).isNull();
    }

    /**
     * Verifiserer at whitespace ikkje påvirkar parsinga på noko vis, spesifikt at
     * leading/trailing whitespace ikkje endrar handteringa slik at det som skulle blitt parsa som ein
     * 8-tegns verdi blir forsøkt parsa med 10-tegns format på grunn av whitespace.
     */
    @Test
    public void skalIkkjeBliPaaVirkaAvWhitespace() {
        assertThat(dato(" 19790807 ")).isEqualTo(LocalDate.of(1979, 8, 7));
        assertThat(dato(" 1979.08.07 ")).isEqualTo(LocalDate.of(1979, 8, 7));
    }

    @Test
    public void skalFeileVissDatoErAvUkjentLengde() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Teksten '197001010' inneheld ikkje ein gyldig dato");
        e.expectMessage("yyyyMMdd");
        e.expectMessage("yyyy.MM.dd");
        dato("197001010");
    }

    @Test
    public void skalFeileVissDatoErUgyldigFormatert() {
        e.expect(DateTimeParseException.class);
        dato("1970AABB");
    }
}