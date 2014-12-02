package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Variabletillegg} representerer eit variabelt tillegg som inngår som ein del av den totale, utbetalte årslønna
 * for stillinga den er innrapportert på.
 * <p>
 * I likheit med grunnlønna forventast variable tillegg å vere innrapportert deltidsjustert slik at ein ikkje treng å
 * foreta nokon vidare justering av dei variable tillegga basert på stillingsprosenten til stillingsendringa.
 * <p>
 * Variable tillegg blir innrapportert som ein årsverdi, på samme måte som deltidsjustert lønn.
 *
 * @author Tarjei Skorgenes
 */
public class Variabletillegg {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt variabelt tillegg i årslønn.
     *
     * @param beloep kronebeløpet som kjem som eit variabelt tillegg i lønn
     * @throws NullPointerException viss <code>beloep</code> er <code>null</code>
     */
    public Variabletillegg(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "beløp er påkrevd, men var null");
    }

    /**
     * Returnerer det variable, årlige lønnstillegget.
     *
     * @return det variable tillegget i årslønn
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
