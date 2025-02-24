package no.spk.tidsserie.tidsperiode;

import static java.time.temporal.TemporalQueries.localDate;
import static java.util.Objects.requireNonNull;

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
     * @return den konverterte datoen
     * @throws NullPointerException dersom <code>text</code> er <code>null</code>
     * @throws IllegalArgumentException dersom <code>text</code> ikkje inneheld ein dato på eit av dei to støtta formata
     */
    public static LocalDate dato(final String text) {
        final String trimmed = requireNonNull(text).trim();
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
