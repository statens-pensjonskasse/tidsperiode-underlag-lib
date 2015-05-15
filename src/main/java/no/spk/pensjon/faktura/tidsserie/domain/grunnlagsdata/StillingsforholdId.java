package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId} representerer nummeret som unikt
 * identifiserer og skiller eit stillingsforhold frå alle andre stillingsforhold.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdId {
    private static final Supplier<String> stillingsforholdPåkrevd = () -> "Stillingsforhold ID er påkrevd, men var tomt";

    private final Long id;

    /**
     * Konstruerer eit nytt stillingsforholdnummer.
     *
     * @param id ein <code>Long</code> som inneheld det unike nummeret som identifiserer stillingsforholdet
     * @throws java.lang.NullPointerException viss <code>id</code> er <code>null</code>
     */
    public StillingsforholdId(final Long id) {
        requireNonNull(id, stillingsforholdPåkrevd);
        this.id = id;
    }

    /**
     * Det unike nummeret som identifiserer stillingsforholdet.
     *
     * @return stillingsforholdnummeret
     */
    public Long id() {
        return id;
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
        return ((StillingsforholdId) obj).id.equals(id);
    }

    @Override
    public String toString() {
        return "stillingsforhold " + id.toString();
    }

    /**
     * @see #valueOf(long)
     */
    public static StillingsforholdId stillingsforhold(final long id) {
        return new StillingsforholdId(id);
    }

    /**
     * @see #StillingsforholdId(Long)
     */
    public static StillingsforholdId valueOf(final long id) {
        return new StillingsforholdId(id);
    }

    /**
     * @see #valueOf(long)
     * @see Long#parseLong(String)
     */
    public static StillingsforholdId valueOf(final String text) {
        return valueOf(Long.parseLong(text));
    }
}
