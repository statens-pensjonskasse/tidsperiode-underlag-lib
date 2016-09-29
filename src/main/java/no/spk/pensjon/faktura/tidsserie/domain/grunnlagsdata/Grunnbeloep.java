package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Grunnbeloep} representerer grunnbeløpet i folketrygda.
 *
 * @author Tarjei Skorgenes
 */
public class Grunnbeloep {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt grunnbeløp med angitt kroneverdi.
     *
     * @param beloep kroneverdien til grunnbeløpet
     */
    public Grunnbeloep(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "grunnbeløp er påkrevd, men var null");
    }

    /**
     * Genererer eit nytt kronebeløp som inneheld kroneverdien av grunnbeløpet oppjustert i henhold til
     * <code>faktor</code>.
     *
     * @param faktor eit heiltall som grunnbeløpet som kroneverdi skal gangast opp med
     * @return eit nytt oppjustert kronebeløp basert på grunnbeløpet og faktoren
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
        return "grunnbeløp " + beloep;
    }

    public Kroner beloep() {
        return beloep;
    }
}
