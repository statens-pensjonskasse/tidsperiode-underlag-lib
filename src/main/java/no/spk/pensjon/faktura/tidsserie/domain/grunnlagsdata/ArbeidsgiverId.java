package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link ArbeidsgiverId} representerer nummeret som unikt identifiserer og skiller en arbeidsgiver fra en annen.
 *
 * @author Snorre E. Brekke - Computas
 */
public class ArbeidsgiverId {
    private final Long id;

    /**
     * Konstruerer eit nytt avtalenummer.
     *
     * @param id ein <code>Long</code> som innehelt avtalenummeret som unikt identifiserer avtalen
     * @throws NullPointerException viss <code>id</code> er <code>null</code>
     */
    public ArbeidsgiverId(final Long id) {
        this.id = requireNonNull(id, () -> "arbeidsgiverid er påkrevd, men var null");
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
        final ArbeidsgiverId other = (ArbeidsgiverId) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "arbeidsgiver " + id;
    }

    /**
     * @see #valueOf(long)
     * @see Long#parseLong(String)
     * @param text er en String som kan konverteres til en <code>Long</code> som inneholder arbeidsgiverid som unikt identifiserer arbeidsgiveren
     * @return en ny arbeidsgiverid.
     */
    public static ArbeidsgiverId valueOf(String text) {
        return valueOf(Long.parseLong(text));
    }

    /**
     * @see #ArbeidsgiverId(Long)
     * @param id en <code>Long</code> som inneholder arbeidsgiverid som unikt identifiserer arbeidsgiveren
     *@return en ny arbeidsgiverid.
     */
    public static ArbeidsgiverId valueOf(long id) {
        return new ArbeidsgiverId(id);
    }

    public Long id() {
        return id;
    }
}
