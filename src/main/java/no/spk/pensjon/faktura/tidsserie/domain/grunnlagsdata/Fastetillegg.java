package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Fastetillegg} representerer eit fast l�nnstillegg som
 * inng�r som ein del av den totale, utbetalte �rsl�nna for stillinga den er innrapportert p�.
 * <p>
 * I likheit med grunnl�nna forventast faste tillegg � bli innrapportert deltidsjustert slik at ein ikkje treng
 * � foreta nokon vidare justering av dei faste tillegga basert p� stillingsprosenten til stillingsendringa.
 * <p>
 * Faste tillegg blir innrapportert som ein �rsverdi, p� samme m�te som deltidsjustert l�nn.
 *
 * @author Tarjei Skorgenes
 */
public class Fastetillegg {
    private final Kroner beloep;

    /**
     * Konstruerer eit nytt fast tillegg i l�nn.
     *
     * @param beloep kronebel�pet som kjem som eit fast tillegg i l�nn
     * @throws NullPointerException dersom <code>beloep</code> er <code>null</code>
     */
    public Fastetillegg(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "bel�p er p�krevd, men var null");
    }

    /**
     * Returnerer det faste, �rlige l�nnstillegget.
     *
     * @return devt faste, �rlige l�nnstillegget
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
