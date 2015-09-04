package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Aksjonskode {
    private static final Map<Aksjonskode, Object> VALUES = new HashMap<>();

    /**
     * Nytilgang.
     */
    public static final Aksjonskode NYTILGANG = newAksjonskode("011");

    /**
     * Endringsmelding.
     */
    public static final Aksjonskode ENDRINGSMELDING = newAksjonskode("021");

    /**
     * Sluttmelding.
     */
    public static final Aksjonskode SLUTTMELDING = newAksjonskode("031");

    /**
     * Permisjon utan lønn.
     */
    public static final Aksjonskode PERMISJON_UTAN_LOENN = newAksjonskode("028");

    /**
     * Aksjonskode for stillingsendringar der aksjonskode manglar.
     */
    public static final Aksjonskode UKJENT = newAksjonskode("UKJENT");

    private static Aksjonskode newAksjonskode(String kode) {
        final Aksjonskode value = new Aksjonskode(kode);
        VALUES.put(value, value);
        return value;
    }

    private final String kode;

    private Aksjonskode(final String kode) {
        this.kode = requireNonNull(kode, () -> "aksjonskode er påkrevd, men var null");
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
                .keySet()
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
