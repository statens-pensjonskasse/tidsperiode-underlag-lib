package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner} representerer eit beløp angitt i norske kroner.
 * <br>
 * Kronebeløpet blir avrunda til nærmaste heile krone ved utlisting og samanlikning med andre kronebeløp, men blir
 * internt behandla som eit desimaltall.
 *
 * @author Tarjei Skorgenes
 */
public final class Kroner implements Comparable<Kroner> {
    private static final Supplier<String> VALIDER_BELOEP = () -> "beløp er påkrevd, men var null";

    private final double beloep;

    /**
     * Konstruerer eit nytt kronebeløp basert på den angitte beløpet.
     * <br>
     * Både positive, negative og beløp lik kr 0 er støtta, inkludert desimalverdiar.
     *
     * @param beloep beløpet i kroner
     * @throws java.lang.NullPointerException viss <code>beloep</code> er <code>null</code>
     */
    public Kroner(final Number beloep) {
        requireNonNull(beloep, VALIDER_BELOEP);
        this.beloep = beloep.doubleValue();
    }

    /**
     * Multipliserer opp beløpet med <code>verdi</code>
     * og returnerer eit nytt beløp.
     *
     * @param verdi tallet beløpet skal gangast med
     * @return eit nytt kronebeløp med resultatet av multiplikasjonen
     */
    public Kroner multiply(final double verdi) {
        return new Kroner(beloep * verdi);
    }

    /**
     * Multipliserer opp beløpet med <code>prosent</code>
     * og returnerer det nye beløpet.
     *
     * @param prosent prosentsatsen beløpet skal gangast med
     * @return eit nytt kronebeløp med resultatet av multiplikasjonen
     */
    public Kroner multiply(final Prosent prosent) {
        return multiply(prosent.toDouble());
    }

    /**
     * Samanliknar dei avrunda kronebeløpa numerisk.
     * <br>
     * Merk at samanlikninga blir gjort basert på avrunda kronebeløp, to beløp med forskjellige desimalverdiar men
     * som avrundast til samme tall er definert som like.
     *
     * @param other kronebeløpet vi skal samanlikne mot
     * @return ein verdi mindre enn 0 viss det andre kronebeløpet har ein større numerisk verdi,
     * ein verdi større enn 0 viss det andre kronebeløpet har ein lavare numerisk verdi
     * eller 0 dersom dei to beløpa har samme numerisk verdi når avrunda til nærmaste heile krone
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
     * Listar ut kronebeløpet som tekst med 0 desimalar, utan tusen-separator og med kr som prefix.
     * <br>
     * NB: Dette er ein potensielt dyr operasjon då formateringa og avrundinga blir utført via ein ny
     * {@link java.text.NumberFormat} pr kall.
     */
    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("no", "NO"));
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(false);
        return format.format(beloep);
    }

    private Kroner rundAv(final Kroner other) {
        return new Kroner(Math.round(other.beloep));
    }
}
