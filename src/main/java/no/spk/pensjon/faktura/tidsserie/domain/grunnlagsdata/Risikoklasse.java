package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link Risikoklasse} er ei kategorisering av avtalar som ligg til grunn
 * for nivået på yrkesskadepremien avtalen skal betale pr årsverk.
 * <br>
 * Yrkesskadepremien pr årsverk er høgst for dei øvre risikoklassene og lavast for dei nedre.
 *
 * @author Tarjei Skorgenes
 */
public final class Risikoklasse {
    private final String kode;

    /**
     * Konstruerer ei ny risikoklasse.
     *
     * @param kode koda som inneheld risikoklassa til avtalen
     * @throws NullPointerException     viss <code>kode</code> er <code>null</code>
     * @throws IllegalArgumentException viss <code>kode</code> ikkje er frå 1 til 3 tegn lang
     */
    public Risikoklasse(final String kode) {
        if (!requireNonNull(kode, "risikoklasse er påkrevd, men var null").matches("[0-9,]{1,3}")) {
            throw new IllegalArgumentException(
                    "risikoklasse kan kun vere 1-3 tegn lang, men inneholdt "
                            + kode.length()
                            + " tegn (var '"
                            + kode
                            + "')"
            );
        }
        this.kode = kode;
    }

    @Override
    public int hashCode() {
        return kode.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Risikoklasse) {
            final Risikoklasse other = (Risikoklasse) obj;
            return kode.equals(other.kode);
        }
        return false;
    }

    @Override
    public String toString() {
        return kode;
    }
}
