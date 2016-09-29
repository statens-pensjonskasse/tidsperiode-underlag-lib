package no.spk.pensjon.faktura.tidsserie;

import static java.time.temporal.TemporalQueries.localDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * {@link Datoar} representerer ein høgnivå API for ofte brukte datorelaterte metoder som ikkje eksisterer
 * direkte på {@link java.time.LocalDate}.
 *
 * @author Tarjei Skorgenes
 */
public class Datoar {
    private static DateTimeFormatter yyyyMMddFormatUtenPunktum = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Konverterer datoar på formatet yyyy.MM.dd / yyyyMMdd frå tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code> eller kun
     * består av whitespace
     */
    public static LocalDate dato(final String text) {
        if (text == null) {
            return null;
        }
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        switch (trimmed.length()) {
            case 8:
                return yyyyMMddFormatUtenPunktum.parse(trimmed).query(localDate());
            case 10:
                return yyyyMMddFormat.parse(trimmed).query(localDate());
        }
        throw new IllegalArgumentException(
                "Teksten '" +
                        trimmed +
                        "' inneheld ikkje ein gyldig dato, " +
                        "det er kun datoar på formata yyyy.MM.dd / yyyyMMdd som er støtta."
        );
    }
}
