package no.spk.pensjon.faktura.tidsserie;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.TemporalQueries.localDate;

/**
 * {@link Datoar} representerer ein høgnivå API for ofte brukte datorelaterte metoder som ikkje eksisterer
 * direkte på {@link java.time.LocalDate}.
 *
 * @author Tarjei Skorgenes
 */
public class Datoar {
    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Konverterer datoar på formatet yyyy.MM.dd frå tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code> eller kun
     * består av whitespace
     */
    public static LocalDate dato(final String text) {
        if (text == null) {
            return null;
        }
        if (text.trim().isEmpty()) {
            return null;
        }
        return yyyyMMddFormat.parse(text).query(localDate());
    }
}
