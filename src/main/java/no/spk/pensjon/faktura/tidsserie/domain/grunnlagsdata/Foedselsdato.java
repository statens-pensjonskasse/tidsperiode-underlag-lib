package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

/**
 * {@link Foedselsdato} representerer datoen eit medlem vart f�dt p�.
 *
 * @author Tarjei Skorgenes
 */
public final class Foedselsdato {
    private final LocalDate dato;

    /**
     * Konstruerer ein ny f�dselsdato.
     * <br>
     * For � gjere det mulig � oppdage inkonsistens i grunnlagsdatane f�rer datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er f�dt f�r 1875 og det er vel
     * rimelig � anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart f�dt.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
     */
    public Foedselsdato(final LocalDate dato) {
        if (requireNonNull(dato, "f�dseldato er p�krevd, men var null").isBefore(LocalDate.of(1875, 1, 1))) {
            throw new IllegalArgumentException("f�dselsdatoar eldre enn 1875.01.01 er ikkje st�tta, var " + dato);
        }
        this.dato = dato;
    }

    /**
     * Konstruerer ein ny f�dselsdato.
     * <br>
     *
     * @param dato datoen medlemmet vart f�dt.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
     * @return ny f�dselsdato
     * @see Foedselsdato#Foedselsdato(java.time.LocalDate)
     */
    public static Foedselsdato foedselsdato(final LocalDate dato) {
        return new Foedselsdato(dato);
    }

    @Override
    public int hashCode() {
        return dato.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Foedselsdato)) {
            return false;
        }
        final Foedselsdato other = (Foedselsdato) obj;
        return dato.equals(other.dato);
    }

    /**
     * Konverterer datoen til eit tall.
     * <br>
     * Tallet blir generert basert p� f�lgjande formel:
     * <code>
     * abs(�rstall) x 10 000 + m�ned i �ret x 100 + dag
     * </code>
     *
     * @return f�dselsdato konvertert til eit tall
     */
    public String tilKode() {
        return Integer.toString(
                dato.getYear() * 10_000
                        + dato.getMonthValue() * 100
                        + dato.getDayOfMonth()
        );
    }

    @Override
    public String toString() {
        return "f�dt " + dato.toString();
    }

}
