package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

/**
 * Markør-annotasjon for fiktive perioder generert av observasjonsunderlaget for perioder etter
 * underlagets observasjonsdato.
 *
 * @author Tarjei Skorgenes
 */
final class FiktivPeriode {
    public static final FiktivPeriode FIKTIV = new FiktivPeriode();

    private FiktivPeriode() {
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
        return "fiktiv periode";
    }
}
