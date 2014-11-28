package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

/**
 * Markør-annotasjon for underlagsperioda som løper fram til og med stillingsforholdets til og med-dato.
 * <p>
 * Underlagsperioder som er markert med denne annotasjonen blir forventa å vere siste underlagsperiode i underlaget
 * dei inngår i.
 *
 * @author Tarjei Skorgenes
 */
public final class SistePeriode {
    public static final SistePeriode INSTANCE = new SistePeriode();

    private SistePeriode() {
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return "siste periode";
    }
}
