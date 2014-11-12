package no.spk.pensjon.faktura.tidsserie;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.TemporalQueries.localDate;

/**
 * {@link Datoar} representerer ein h�gniv� API for ofte brukte datorelaterte metoder som ikkje eksisterer
 * direkte p� {@link java.time.LocalDate}.
 *
 * @author Tarjei Skorgenes
 */
public class Datoar {
    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Konverterer datoar p� formatet yyyy.MM.dd fr� tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code> eller kun
     * best�r av whitespace
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

    /**
     * Verifiserer at fr� og med-dato aldri kan vere etter til og med-dato.
     *
     * @param fraOgMed fr� og med-dato
     * @param tilOgMed til og med-dato
     * @throws IllegalArgumentException viss <code>fraOgMed</code> er etter <code>tilOgMed</code>
     */
    public static void sjekkForVrengteDatoar(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        if (fraOgMed.isAfter(tilOgMed)) {
            throw new IllegalArgumentException("fra og med-dato kan ikkje vere etter til og med-dato, men "
                    + fraOgMed + " er etter " + tilOgMed
            );
        }
    }
}
