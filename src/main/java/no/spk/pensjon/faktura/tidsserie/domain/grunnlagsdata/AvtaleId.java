package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link AvtaleId} representerer nummeret som unikt identifiserer og skiller ein avtale frå alle andre avtalar.
 *
 * @author Tarjei Skorgenes
 */
public class AvtaleId {
    private final Long id;

    /**
     * Konstruerer eit nytt avtalenummer.
     *
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @throws NullPointerException viss <code>id</code> er <code>null</code>
     */
    public AvtaleId(final Long id) {
        this.id = requireNonNull(id, () -> "avtalenummer er påkrevd, men var null");
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
        final AvtaleId other = (AvtaleId) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "avtale " + id;
    }

    /**
     * @see #valueOf(long)
     * @see Long#parseLong(String)
     * @param text er ein String som kan konverteres til ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @return eit nytt avtalenummer.
     */
    public static AvtaleId valueOf(String text) {
        return valueOf(Long.parseLong(text));
    }

    /**
     * @see #valueOf(long)
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @return eit nytt avtalenummer.
     */
    public static AvtaleId avtaleId(final long id) {
        return valueOf(id);
    }

    /**
     * @see #AvtaleId(Long)
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @return eit nytt avtalenummer.
     */
    public static AvtaleId valueOf(long id) {
        return new AvtaleId(id);
    }

    public Long id() {
        return id;
    }
}
