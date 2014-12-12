package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Grunnbeloep} representerer grunnbel�pet i folketrygda.
 *
 * @author Tarjei Skorgenes
 */
public class Grunnbeloep {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt grunnbel�p med angitt kroneverdi.
     *
     * @param beloep kroneverdien til grunnbel�pet
     */
    public Grunnbeloep(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "grunnbel�p er p�krevd, men var null");
    }

    /**
     * Genererer eit nytt kronebel�p som inneheld kroneverdien av grunnbel�pet oppjustert i henhold til
     * <code>faktor</code>.
     *
     * @param faktor eit heiltall som grunnbel�pet som kroneverdi skal gangast opp med
     * @return eit nytt oppjustert kronebel�p basert p� grunnbel�pet og faktoren
     */
    public Kroner multiply(final int faktor) {
        return beloep.multiply(faktor);
    }

    @Override
    public int hashCode() {
        return beloep.hashCode();
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
        final Grunnbeloep other = (Grunnbeloep) obj;
        return beloep.equals(other.beloep);
    }

    @Override
    public String toString() {
        return "grunnbel�p " + beloep;
    }
}
