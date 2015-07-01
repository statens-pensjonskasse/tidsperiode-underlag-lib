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
     * Konstruerer eit nytt avtalenummer.
     *
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
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
     * @param text er ein String som kan konverteres til ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @return eit nytt avtalenummer.
     */
    public static Orgnummer valueOf(String text) {
        return valueOf(Long.parseLong(text));
    }

    /**
     * @see #AvtaleId(Long)
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @return eit nytt avtalenummer.
     */
    public static Orgnummer valueOf(long id) {
        return new Orgnummer(id);
    }

    public Long id() {
        return id;
    }
}
