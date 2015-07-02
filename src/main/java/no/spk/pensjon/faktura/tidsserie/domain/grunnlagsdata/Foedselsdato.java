package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

/**
 * {@link Foedselsdato} representerer datoen eit medlem vart født på.
 *
 * @author Tarjei Skorgenes
 */
public final class Foedselsdato {
    private final LocalDate dato;

    /**
     * Konstruerer ein ny fødselsdato.
     * <br>
     * For å gjere det mulig å oppdage inkonsistens i grunnlagsdatane fører datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er født før 1875 og det er vel
     * rimelig å anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart født.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     */
    public Foedselsdato(final LocalDate dato) {
        if (requireNonNull(dato, "fødseldato er påkrevd, men var null").isBefore(LocalDate.of(1875, 1, 1))) {
            throw new IllegalArgumentException("fødselsdatoar eldre enn 1875.01.01 er ikkje støtta, var " + dato);
        }
        this.dato = dato;
    }

    /**
     * Konstruerer ein ny fødselsdato.
     * <br>
     *
     * @param dato datoen medlemmet vart født.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     * @return ny fødselsdato
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
     * Tallet blir generert basert på følgjande formel:
     * <code>
     * abs(årstall) x 10 000 + måned i året x 100 + dag
     * </code>
     *
     * @return fødselsdato konvertert til eit tall
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
        return "født " + dato.toString();
    }

}
