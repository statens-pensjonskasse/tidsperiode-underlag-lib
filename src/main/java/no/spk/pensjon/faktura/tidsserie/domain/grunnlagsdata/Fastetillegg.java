package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Fastetillegg} representerer eit fast lønnstillegg som
 * inngår som ein del av den totale, utbetalte årslønna for stillinga den er innrapportert på.
 * <p>
 * I likheit med grunnlønna forventast faste tillegg å bli innrapportert deltidsjustert slik at ein ikkje treng
 * å foreta nokon vidare justering av dei faste tillegga basert på stillingsprosenten til stillingsendringa.
 * <p>
 * Faste tillegg blir innrapportert som ein årsverdi, på samme måte som deltidsjustert lønn.
 *
 * @author Tarjei Skorgenes
 */
public class Fastetillegg {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt fast tillegg i lønn.
     *
     * @param beloep kronebeløpet som kjem som eit fast tillegg i lønn
     * @throws NullPointerException dersom <code>beloep</code> er <code>null</code>
     */
    public Fastetillegg(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "beløp er påkrevd, men var null");
    }

    /**
     * Returnerer det faste, årlige lønnstillegget.
     *
     * @return devt faste, årlige lønnstillegget
     */
    public Kroner beloep() {
        return beloep;
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
        if (obj.getClass() != getClass()) {
            return false;
        }
        final Fastetillegg other = (Fastetillegg) obj;
        return beloep.equals(other.beloep);
    }

    @Override
    public String toString() {
        return "fast tillegg " + beloep;
    }
}
