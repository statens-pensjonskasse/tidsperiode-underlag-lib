package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Medlemslinjenummer} representerer eit linjenummer som i kombinasjon med {@link Foedselsnummer}
 * unikt identifiserer ei stillingsendring.
 * <br>
 * Eit linjenummer m� vere eit positivt heiltall st�rre enn eller lik 1 og har som hensikt � gjere ein i stand til �
 * skille to stillingsendringar tilh�yrande samme medlem fr� kvarandre.
 *
 * @author Tarjei Skorgenes
 * @since 1.1.1
 */
public final class Medlemslinjenummer {
    private final Integer value;

    private Medlemslinjenummer(final Integer value) {
        if (requireNonNull(value, "linjenummer er p�krevd, men var null") < 1) {
            throw new IllegalArgumentException("linjenummer m� vere eit positivt heiltal, men var " + value);
        }
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Medlemslinjenummer) {
            return value.equals(((Medlemslinjenummer) obj).value);
        }
        return false;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Konstruerer eit nytt linjenummer.
     *
     * @param value eit positivt heiltal som inneheld verdien linjenummeret representerer
     * @return {@link Medlemslinjenummer} konsturert fra  {@code value}
     * @throws IllegalArgumentException dersom <code>value</code> er mindre enn 1
     */
    public static Medlemslinjenummer linjenummer(final Integer value) {
        return new Medlemslinjenummer(value);
    }
}
