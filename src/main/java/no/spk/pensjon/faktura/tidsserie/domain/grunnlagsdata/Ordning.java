package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning} representerer ein identifikator
 * som skiller dei forskjellge pensjonsordningane som Statens Pensjonskasse administrerer, frå kvarandre.
 * <p>
 * For fastsats er det kun 3 ordningar som er støtta, den statlige tjenestepensjonsordninga ({@link #SPK}),
 * Apotekordninga ({@link #POA} og ordninga for Den Norske Opera ({@link #OPERA}).
 *
 * @author Tarjei Skorgenes
 */
public class Ordning {
    private static final Set<Ordning> VALUES = new HashSet<>();

    /**
     * Den statlige tjenestepensjonsordninga som dei aller fleste SPK-kundane er tilknytta.
     */
    public static final Ordning SPK = new Ordning(3010);

    /**
     * Pensjonsordninga for Apotek.
     */
    public static final Ordning POA = new Ordning(3060);

    /**
     * Pensjonsordninga for Den Norske Opera.
     */
    public static final Ordning OPERA = new Ordning(3035);

    private final Integer id;

    private Ordning(final int id) {
        this.id = id;
        VALUES.add(this);
    }

    /**
     * Ordningsnummer.
     *
     * @return den unike numeriske identifikatoren for ordninga
     */
    public Integer kode() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ordning other = (Ordning) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "ordning " + id;
    }

    /**
     * @see #valueOf(int)
     */
    public static Ordning valueOf(final String text) {
        return valueOf(Integer.parseInt(text));
    }

    /**
     * Slår opp ein av dei predefinerte ordningane som har det angitte ordningsnummeret eller opprettar ein ny instans
     * med den angitte verdien.
     *
     * @param id eit tall som representerere ordningas unike identifikator
     * @return ein ny instans, eller ein av dei predefinerte viss dei har samme numerisk verdi som <code>id</code>
     */
    public static Ordning valueOf(final int id) {
        return VALUES
                .stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElse(new Ordning(id));
    }
}
