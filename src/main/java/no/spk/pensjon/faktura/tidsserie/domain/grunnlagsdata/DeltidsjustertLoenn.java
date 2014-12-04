package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@link DeltidsjustertLoenn} representerer lønn som er justert
 * ut frå stillingsprosenten i perioda lønna gjeld for.
 * <br>
 * Eksempel:
 * <br>
 * Kåre har utbetalt kr 250 000 i året og jobbar i 50% stilling, deltidsjustert lønn blir her kr 250 000.
 * <br>
 * Else har utbetalt kr 440 000 i året og jobbar i 100% stilling, deltidjustert lønn blir her kr 440 000.
 * <br>
 * John jobbar i stilling med lønnstrinn 48 i 80% stilling, lønnstrinn 48 tilsvarar kr 400 000 i 100% stilling,
 * deltidsjustert lønn blir her kr 320 000.
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoenn {
    private static final Supplier<String> VALIDER_BELOEP = () -> "beløp er påkrevd, men var null";

    private final Kroner beloep;

    /**
     * Konstruerer ei ny instans som representerer den deltidsjusterte lønna i kroner.
     *
     * @param beloep kronebeløpet som inneheld lønnsverdien
     * @throws java.lang.NullPointerException if <code>beloep</code> er <code>null</code>
     */
    public DeltidsjustertLoenn(final Kroner beloep) {
        requireNonNull(beloep, VALIDER_BELOEP);
        this.beloep = beloep;
    }

    /**
     * Kronebeløpet som representerer den deltidsjusterte årslønna.
     *
     * @return kronebeløpet for den deltidsjusterte årslønna
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
