package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Aksjonskode {
    private static final Set<Aksjonskode> VALUES = new HashSet<>();

    /**
     * Nytilgang.
     */
    public static final Aksjonskode NYTILGANG = new Aksjonskode("011");

    /**
     * Endringsmelding.
     */
    public static final Aksjonskode ENDRINGSMELDING = new Aksjonskode("021");

    /**
     * Sluttmelding.
     */
    public static final Aksjonskode SLUTTMELDING = new Aksjonskode("031");

    /**
     * Permisjon utan lønn.
     */
    public static final Aksjonskode PERMISJON_UTAN_LOENN = new Aksjonskode("028");

    /**
     * Aksjonskode for stillingsendringar der aksjonskode manglar.
     */
    public static final Aksjonskode UKJENT = new Aksjonskode("UKJENT");

    private final String kode;

    private Aksjonskode(final String kode) {
        this.kode = requireNonNull(kode, () -> "aksjonskode er påkrevd, men var null");
        if (VALUES.size() < 100) {
            VALUES.add(this);
        }
    }

    /**
     * Aksjonskoda som identifiserer og skiller aksjonskoda frå andre aksjonskoder.
     *
     * @return aksjonskoda
     */
    public String kode() {
        return kode;
    }

    /**
     * Konverterer <code>kode</code> til ein ny eller tidligare definert aksjonskode.
     * <p>
     * Dersom det er predefinert ei aksjonskode med den angitte koda, vil denne bli returnert. Viss det ikkje
     * tidligare har blitt definert ei aksjonskode for den angitte koda blir det generert ein ny aksjonskode.
     * <p>
     * Dersom <code>kode</code> er tom, kun inneheld whitespace eller er <code>null</code> blir {@link #UKJENT}
     * alltid returnert.
     * <p>
     * Oppslaget vil cache opp til 100 forskjellige aksjonskoder, blir det forsøkt definert fleire enn det vil denne
     * metoda returnere ein ny instans kvar gang ei kode som ikkje er blant dei 100, blir forsøkt slått opp.
     *
     * @param kode ein streng som skal konverterast til ein aksjonskode
     * @return aksjonskoda for den aktuelle koda
     */
    public static Aksjonskode valueOf(final String kode) {
        if (kode == null || kode.trim().isEmpty()) {
            return UKJENT;
        }
        return VALUES
                .stream()
                .filter(status -> status.harKode(kode))
                .findFirst()
                .orElse(new Aksjonskode(kode));
    }

    @Override
    public int hashCode() {
        return kode.hashCode();
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
        final Aksjonskode other = (Aksjonskode) obj;
        return other.kode.equals(kode);
    }

    @Override
    public String toString() {
        return "aksjonskode " + kode;
    }

    private boolean harKode(final String kode) {
        return this.kode.equals(kode);
    }
}
