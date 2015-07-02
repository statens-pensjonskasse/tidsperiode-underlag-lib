package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Orgnummer} er organisasjonsnummeret til en arbeidsgiver.
 *
 * @author Tarjei Skorgenes
 */
public class Orgnummer {
    private final Long id;

    /**
     * Konstruerer et nytt orgnummer
     *
     * @param id en <code>Long</code> som representerer orgnummeret
     * @throws NullPointerException viss <code>id</code> er <code>null</code>
     */
    public Orgnummer(final Long id) {
        this.id = requireNonNull(id, () -> "orgnummer er påkrevd, men var null");
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Orgnummer other = (Orgnummer) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "orgnummer " + id;
    }

    /**
     * @see #valueOf(long)
     * @see Long#parseLong(String)
     * @param text er en string som kan konverteres til en <code>Long</code> som representerer orgnummeret
     * @return eit nytt avtalenummer.
     */
    public static Orgnummer valueOf(String text) {
        return valueOf(Long.parseLong(text));
    }

    /**
     * @see #Orgnummer(Long) (Long)
     * @param id er en <code>Long</code> som representerer orgnummeret
     * @return eit nytt avtalenummer.
     */
    public static Orgnummer valueOf(long id) {
        return new Orgnummer(id);
    }

    public Long id() {
        return id;
    }
}
