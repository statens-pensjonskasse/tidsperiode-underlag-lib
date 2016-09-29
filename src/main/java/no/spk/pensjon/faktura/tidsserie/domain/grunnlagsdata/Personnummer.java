package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.stream.Stream;

/**
 * {@link Personnummer} representerer den 5-sifra koda som i kombinasjon med fødselsdato
 * unikt identifiserer eit medlem.
 *
 * @author Tarjei Skorgenes
 */
public final class Personnummer {
    private final static char[] ZERO_PADDING = "0000".toCharArray();

    private final String value;

    /**
     * Konstruerer eit nytt personnummer basert på den angitte, numeriske verdien.
     *
     * @param value ein numerisk representasjon av personnummeret
     * @throws IllegalArgumentException viss <code>value</code> er mindre enn 0
     */
    public Personnummer(final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("personnummer må vere eit positivt heiltall, verdien var " + value);
        }
        if (value > 99999) {
            throw new IllegalArgumentException("personnummer kan ikkje vere meir enn 5 siffer langt, verdien var " + value);
        }
        this.value = padWithZero(Integer.toString(value));
    }

    /**
     * Konstruerer eit nytt personnummer basert på den angitte, numeriske verdien.
     *
     * @param value ein numerisk representasjon av personnummeret
     * @return et nytt personnummer
     * @throws IllegalArgumentException viss <code>value</code> er mindre enn 0
     */
    public static Personnummer personnummer(final int value) {
        return new Personnummer(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Personnummer)) {
            return false;
        }
        final Personnummer other = (Personnummer) obj;
        return value.equals(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static String padWithZero(final String kode) {
        return String.valueOf(ZERO_PADDING, 0, 5 - kode.length()) + kode;
    }
}
