package no.spk.pensjon.faktura.tidsserie.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.TemporalQueries.localDate;

/**
 * Ymse tidsrelatert funksjonalitet brukt av enheits- og integrasjonstestane til modulen.
 */
public class Tid {
    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Konverterer datoar p� formatet yyyy.MM.dd fr� tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code>
     */
    public static LocalDate dato(final String text) {
        if (text == null) {
            return null;
        }
        return yyyyMMddFormat.parse(text).query(localDate());
    }
}
