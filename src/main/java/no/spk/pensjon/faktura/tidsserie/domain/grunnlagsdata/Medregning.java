package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Medregning} representerer eit beløp som medlemmet har tjent opp hos ein arbeidsgivar tilknytta ei anna
 * ordning enn SPK-ordninga og der den andre arbeidsgivaren er ein part i overføringsavtalen.
 * <p>
 * Medregning blir og benytta for bistillingar og pensjonsgivande inntekt hos annan arbeidsgivar. Dei to siste er dei
 * einaste typene medregning som blir tatt hensyn til og medregna i tillegg til lønn for fastsats faktureringa.
 *
 * @author Tarjei Skorgenes
 */
public class Medregning {
    private final Kroner beloep;

    /**
     * Konstruerer ei ny medregning
     *
     * @param beloep lønna som medlemmet skal få medregna
     * @throws NullPointerException dersom <code>beloep</code> er <code>null</code>
     */
    public Medregning(final Kroner beloep) {
        this.beloep = requireNonNull(beloep, () -> "beløp er påkrevd, men var null");
    }

    /**
     * Lønna medlemmet har tjent opp hos ein arbeidsgivar utanfor SPK-ordninga.
     *
     * @return eit kronebeløp som representerer lønna som skal medregnast
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
        final Medregning other = (Medregning) obj;
        return other.beloep.equals(beloep);
    }

    @Override
    public String toString() {
        return "medregning " + beloep;
    }
}
