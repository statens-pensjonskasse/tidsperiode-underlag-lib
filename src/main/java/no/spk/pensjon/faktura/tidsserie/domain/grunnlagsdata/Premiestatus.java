package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * {@link Premiestatus} representerer ei kode som blir brukt av avtalar for å gruppere saman avtalar med liknande
 * premierelaterte egenskapar.
 * <p>
 * Pr utgangen av 2014 eksisterer det totalt 17 forskjellige premiestatusar. Sidan tidsseriegenereringa kun
 * spesialbehandlar to av dei, grunnskuler og vidaregåande skular, er det kun desse to som er pre-definert som
 * konstantar.
 *
 * @author Tarjei Skorgenes
 */
public final class Premiestatus {
    private static final ConcurrentHashMap<Premiestatus, Object> VALUES = new ConcurrentHashMap<>(20);

    /**
     * Grunnskular.
     */
    public static final Premiestatus AAO_01 = new Premiestatus("AAO-01");

    /**
     * Vidaregåande skular.
     */
    public static final Premiestatus AAO_02 = new Premiestatus("AAO-02");

    /**
     * Premiestatus for avtaleversjonar som manglar premiestatus.
     */
    public static final Premiestatus UKJENT = new Premiestatus("UKJENT");

    private final String kode;

    private Premiestatus(final String kode) {
        this.kode = requireNonNull(kode, () -> "premiestatus er påkrevd, men var null");
        if (VALUES.size() < 100) {
            VALUES.put(this, this);
        }
    }

    /**
     * Premiestatuskoda som identifiserer og skiller premiestatusen frå andre premiestatusar.
     *
     * @return premiestatuskoda
     */
    public String kode() {
        return kode;
    }

    /**
     * Konverterer <code>kode</code> til ein ny eller tidligare definert premiestatus.
     * <p>
     * Dersom det er predefinert ein premiestatus med den angitte koda, vil denne bli returnert. Viss det ikkje
     * tidligare har blitt definert ein premiestatus for den angitte koda blir det generert ein ny premiestatus.
     * <p>
     * Dersom <code>kode</code> er tom, kun inneheld whitespace eller er <code>null</code> blir {@link #UKJENT}
     * alltid returnert.
     * <p>
     * Oppslaget vil cache opp til 100 forskjellige premiestatusar, blir det forsøkt definert fleire enn det vil denne
     * metoda returnere ein ny instans kvar gang ei kode som ikkje er blant dei 100, blir forsøkt slått opp.
     *
     * @param kode ein streng som skal konverterast til ein premiestatus
     * @return premiestatusen for den aktuelle koda
     */
    public static Premiestatus valueOf(final String kode) {
        if (kode == null || kode.trim().isEmpty()) {
            return UKJENT;
        }
        return VALUES
                .keySet()
                .stream()
                .filter(status -> status.harKode(kode))
                .findFirst()
                .orElse(new Premiestatus(kode));
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
        final Premiestatus other = (Premiestatus) obj;
        return other.kode.equals(kode);
    }

    @Override
    public String toString() {
        return "premiestatus " + kode;
    }

    private boolean harKode(final String kode) {
        return this.kode.equals(kode);
    }
}
