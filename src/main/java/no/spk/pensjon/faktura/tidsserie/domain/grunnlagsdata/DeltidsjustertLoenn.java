package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@link DeltidsjustertLoenn} representerer l�nn som er justert
 * ut fr� stillingsprosenten i perioda l�nna gjeld for.
 * <br>
 * Eksempel:
 * <br>
 * K�re har utbetalt kr 250 000 i �ret og jobbar i 50% stilling, deltidsjustert l�nn blir her kr 250 000.
 * <br>
 * Else har utbetalt kr 440 000 i �ret og jobbar i 100% stilling, deltidjustert l�nn blir her kr 440 000.
 * <br>
 * John jobbar i stilling med l�nnstrinn 48 i 80% stilling, l�nnstrinn 48 tilsvarar kr 400 000 i 100% stilling,
 * deltidsjustert l�nn blir her kr 320 000.
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoenn {
    private static final Supplier<String> VALIDER_BELOEP = () -> "bel�p er p�krevd, men var null";

    private final Kroner beloep;

    /**
     * Konstruerer ei ny instans som representerer den deltidsjusterte l�nna i kroner.
     *
     * @param beloep kronebel�pet som inneheld l�nnsverdien
     * @throws java.lang.NullPointerException if <code>beloep</code> er <code>null</code>
     */
    public DeltidsjustertLoenn(final Kroner beloep) {
        requireNonNull(beloep, VALIDER_BELOEP);
        this.beloep = beloep;
    }

    /**
     * Kronebel�pet som representerer den deltidsjusterte �rsl�nna.
     *
     * @return kronebel�pet for den deltidsjusterte �rsl�nna
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeltidsjustertLoenn other = (DeltidsjustertLoenn) obj;
        return beloep.equals(other.beloep);
    }

    @Override
    public String toString() {
        return beloep.toString();
    }
}
