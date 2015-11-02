package no.spk.pensjon.faktura.tidsserie.domain.avregning;

/**
 * Versjonsnummer representerer versjonsnummeret som blir brukt for � funksjonelt skille
 * ein avregningsversjon fr� ein annan avregningsversjon.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class Avregningsversjon implements Comparable<Avregningsversjon> {
    private final Integer nummer;

    private Avregningsversjon(final int nummer) {
        if (nummer < 1) {
            throw new IllegalArgumentException(
                    "versjonsnummer m� v�re st�rre enn eller lik 1, men var "
                            + nummer
            );
        }
        this.nummer = nummer;
    }

    public static Avregningsversjon avregningsversjon(final int versjonsnummer) {
        return new Avregningsversjon(versjonsnummer);
    }

    public int verdi() {
        return nummer;
    }

    @Override
    public int hashCode() {
        return nummer.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Avregningsversjon other = (Avregningsversjon) obj;
        return nummer.equals(other.nummer);
    }

    @Override
    public String toString() {
        return nummer.toString();
    }

    @Override
    public int compareTo(Avregningsversjon o) {
        return nummer.compareTo(o.nummer);
    }
}

