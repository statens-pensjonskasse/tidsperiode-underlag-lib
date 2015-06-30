package no.spk.pensjon.faktura.tidsserie;

import static java.time.temporal.TemporalQueries.localDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * {@link Datoar} representerer ein h�gniv� API for ofte brukte datorelaterte metoder som ikkje eksisterer
 * direkte p� {@link java.time.LocalDate}.
 *
 * @author Tarjei Skorgenes
 */
public class Datoar {
    private static DateTimeFormatter yyyyMMddFormatUtenPunktum = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Konverterer datoar p� formatet yyyy.MM.dd / yyyyMMdd fr� tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code> eller kun
     * best�r av whitespace
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
                        "det er kun datoar p� formata yyyy.MM.dd / yyyyMMdd som er st�tta."
        );
    }
}
