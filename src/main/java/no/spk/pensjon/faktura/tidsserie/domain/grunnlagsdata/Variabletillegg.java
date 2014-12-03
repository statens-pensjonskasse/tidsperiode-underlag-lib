package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Variabletillegg} representerer eit variabelt tillegg som inng�r som ein del av den totale, utbetalte �rsl�nna
 * for stillinga den er innrapportert p�.
 * <p>
 * I likheit med grunnl�nna forventast variable tillegg � vere innrapportert deltidsjustert slik at ein ikkje treng �
 * foreta nokon vidare justering av dei variable tillegga basert p� stillingsprosenten til stillingsendringa.
 * <p>
 * Variable tillegg blir innrapportert som ein �rsverdi, p� samme m�te som deltidsjustert l�nn.
 *
 * @author Tarjei Skorgenes
 */
public class Variabletillegg {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt variabelt tillegg i �rsl�nn.
     *
     * @param beloep kronebel�pet som kjem som eit variabelt tillegg i l�nn
     * @throws NullPointerException viss <code>beloep</code> er <code>null</code>
     */
    public Variabletillegg(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "bel�p er p�krevd, men var null");
    }

    /**
     * Returnerer det variable, �rlige l�nnstillegget.
     *
     * @return det variable tillegget i �rsl�nn
     */
    public Kroner beloep() {
        return beloep;
    }

    @Override
    public int hashCode() {
        return beloep.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final Variabletillegg other = (Variabletillegg) obj;
        return beloep.equals(other.beloep);
    }

    @Override
    public String toString() {
        return "variable tillegg " + beloep;
    }
}
