package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner} representerer eit bel�p angitt i norske kroner.
 * <br>
 * Kronebel�pet blir avrunda til n�rmaste heile krone ved utlisting og samanlikning med andre kronebel�p, men blir
 * internt behandla som eit desimaltall.
 *
 * @author Tarjei Skorgenes
 */
public final class Kroner implements Comparable<Kroner> {
    /**
     * Eit kronebel�p med verdi lik kr 0.
     */
    public static final Kroner ZERO = new Kroner(0);

    private static final Supplier<String> VALIDER_BELOEP = () -> "bel�p er p�krevd, men var null";

    private final double beloep;

    /**
     * Konstruerer eit nytt kronebel�p basert p� den angitte bel�pet.
     * <br>
     * B�de positive, negative og bel�p lik kr 0 er st�tta, inkludert desimalverdiar.
     *
     * @param beloep bel�pet i kroner
     * @throws java.lang.NullPointerException viss <code>beloep</code> er <code>null</code>
     */
    public Kroner(final Number beloep) {
        requireNonNull(beloep, VALIDER_BELOEP);
        this.beloep = beloep.doubleValue();
    }

    /**
     * Hentar ut verdien av bel�pet, avrunda til n�rmaste heile heiltal.
     *
     * @return kronebel�pet avrunda til n�rmaste heile krone.
     */
    public long verdi() {
        return Math.round(beloep);
    }

    /**
     * Konstruerer ein ny kronerepresentasjon for eit <code>beloep</code>.
     *
     * @param beloep bel�pet som skal representerast som eit kronebel�p
     * @return eit nytt kronebel�p
     */
    public static Kroner kroner(final int beloep) {
        if (beloep == 0) {
            return ZERO;
        }
        return new Kroner(beloep);
    }

    /**
     * Returnerer minste bel�p av dei to kronebel�pa.
     *
     * @param a f�rste kronebel�p
     * @param b andre kronebel�p
     * @return eit nytt kronebel�p som inneheld bel�pet til den av a og b som inneheld det minste kronebel�pet
     */
    public static Kroner min(final Kroner a, final Kroner b) {
        return new Kroner(Math.min(a.beloep, b.beloep));
    }

    /**
     * Multipliserer opp bel�pet med <code>verdi</code>
     * og returnerer eit nytt bel�p.
     *
     * @param verdi tallet bel�pet skal gangast med
     * @return eit nytt kronebel�p med resultatet av multiplikasjonen
     */
    public Kroner multiply(final double verdi) {
        return new Kroner(beloep * verdi);
    }

    /**
     * Multipliserer opp bel�pet med <code>prosent</code>
     * og returnerer det nye bel�pet.
     *
     * @param prosent prosentsatsen bel�pet skal gangast med
     * @return eit nytt kronebel�p med resultatet av multiplikasjonen
     */
    public Kroner multiply(final Prosent prosent) {
        return multiply(prosent.toDouble());
    }

    /**
     * Legger saman gjeldande kronebel�p med det andre kronebel�pet
     * og returnerer eit nytt kronebel�p med summen av dei to.
     *
     * @param other det andre kronebel�pet
     * @return eit nytt kronebel�p som inneheld summen av dei to kronebel�pa
     */
    public Kroner plus(final Kroner other) {
        return new Kroner(beloep + other.beloep);
    }

    /**
     * Samanliknar dei avrunda kronebel�pa numerisk.
     * <br>
     * Merk at samanlikninga blir gjort basert p� avrunda kronebel�p, to bel�p med forskjellige desimalverdiar men
     * som avrundast til samme tall er definert som like.
     *
     * @param other kronebel�pet vi skal samanlikne mot
     * @return ein verdi mindre enn 0 viss det andre kronebel�pet har ein st�rre numerisk verdi,
     * ein verdi st�rre enn 0 viss det andre kronebel�pet har ein lavare numerisk verdi
     * eller 0 dersom dei to bel�pa har samme numerisk verdi n�r avrunda til n�rmaste heile krone
     */
    public int compareTo(final Kroner other) {
        return Double.compare(
                rundAv(this).beloep,
                rundAv(other).beloep
        );
    }

    @Override
    public int hashCode() {
        return Double.hashCode(beloep);
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
        final Kroner other = (Kroner) obj;
        return rundAv(this).beloep == rundAv(other).beloep;
    }

    /**
     * Listar ut kronebel�pet som tekst med 0 desimalar, utan tusen-separator og med kr som prefix.
     * <br>
     * NB: Dette er ein potensielt dyr operasjon d� formateringa og avrundinga blir utf�rt via ein ny
     * {@link java.text.NumberFormat} pr kall.
     */
    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("no", "NO"));
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(false);
        return format.format(beloep);
    }

    private Kroner rundAv(final Kroner that) {
        return new Kroner(that.verdi());
    }
}
