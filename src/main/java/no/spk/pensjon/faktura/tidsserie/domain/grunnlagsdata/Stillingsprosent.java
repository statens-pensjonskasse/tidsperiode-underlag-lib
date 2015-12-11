package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent} representerer ein andel av ei
 * fulltidsstilling målt i prosent.
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsprosent {
    private final Prosent prosent;

    /**
     * Konstruerer ein ny stillingsprosent.
     *
     * @param prosent ein positiv prosentsats
     * @throws NullPointerException viss <code>prosent</code> er <code>null</code>
     */
    public Stillingsprosent(final Prosent prosent) throws NullPointerException {
        this.prosent = requireNonNull(prosent, () -> "stillingsprosent er påkrevd, men var null");
        if (prosent.toDouble() < 0.0) {
            throw new IllegalArgumentException("stillingsprosent må vere positiv, men var " + prosent);
        }
    }

    /**
     * Stillingsstørrelsen i prosent.
     *
     * @return stillingsstørrelsen i prosent.
     */
    public Prosent prosent() {
        return prosent;
    }

    @Override
    public int hashCode() {
        // Avrundar her til heiltal for å sikre at variasjonar i desimalverdiane ikkje påvirkar resultatet for
        // stillingsprosentar som grunna avrunding vil bli evaluert som like av equals-metoda
        return Long.hashCode(Math.round(prosent.toDouble()));
    }

    /**
     * Sjekkar om <code>obj</code> er ein <code>Stillingsprosent</code>
     * og sjekkar om dei to har lik prosentverdi, etter avrunding til to desimalar.
     *
     * @param obj objektet vi skal samanlikne mot
     * @return <code>true</code> viss <code>obj</code> er av type stillingsprosent og har samme avrunda prosentverdi
     * med opp til to desimalar, <code>false</code> i alle andre situasjonar
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Stillingsprosent other = (Stillingsprosent) obj;
        return prosent.equals(other.prosent, 2);
    }

    @Override
    public String toString() {
        return prosent + " stilling";
    }

    /**
     * Konstruerer ein ny stillingsprosent for ei 100%-stilling.
     *
     * @return 100% stillingsprosent
     */
    public static Stillingsprosent fulltid() {
        return new Stillingsprosent(new Prosent("100%"));
    }
}
