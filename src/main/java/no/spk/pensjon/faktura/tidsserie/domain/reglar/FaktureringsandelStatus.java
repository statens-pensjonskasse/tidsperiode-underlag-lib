package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Objects.requireNonNull;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

/**
 * {@link FaktureringsandelStatus} indikerer korvidt eit stillingsforhold skal bli fakturert for gruppeliv.
 *
 * @author Tarjei Skorgenes
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel
 */
public class FaktureringsandelStatus {
    public static Prosent MIN_ANDEL = Prosent.ZERO;
    public static Prosent MAX_ANDEL = Prosent.prosent("100%");

    private final StillingsforholdId id;
    private final Prosent andel;

    /**
     * Konstruerer ein ny status som skal indikere korvidt stillingsforholdet kan fakturerast for et produkt.
     *
     * @param stillingsforhold stillingsforholdet statusen gjeld for
     * @param andel prosentandelen av premien som stillingsforholdet sin avtale skal betale
     */
    public FaktureringsandelStatus(final StillingsforholdId stillingsforhold, final Prosent andel) {
        this.id = requireNonNull(stillingsforhold, "stillingsforhold var null, men er påkrevd");
        this.andel = requireNonNull(andel, "andel var null, men er påkrevd");
        validerStoerreEllerLik0prosent(andel);
        validerMindreEllerLik100prosent(andel);
    }

    private void validerStoerreEllerLik0prosent(Prosent andel) {
        if (MIN_ANDEL.isGreaterThan(andel)) {
            throw new IllegalArgumentException("Andel kan ikke være mindre enn " + MIN_ANDEL + " men var " + andel);
        }
    }

    private void validerMindreEllerLik100prosent(Prosent andel) {
        if (andel.isGreaterThan(MAX_ANDEL)) {
            throw new IllegalArgumentException("Andel kan ikke større enn " + MAX_ANDEL + " men var " + andel);
        }
    }

    /**
     * Indikerer korvidt stillingsforholdet skal bli fakturert for produktet.
     *
     * @return <code>true</code> viss stillingsforholdet skal bli fakturert for produktet, <code>false</code> ellers
     */
    public boolean erFakturerbar() {
        return andel.isGreaterThan(Prosent.ZERO);
    }

    /**
     * Stillingsforholdet statusen gjeld for.
     *
     * @return stillingsforholdet statusen gjeld for.
     */
    public StillingsforholdId stillingsforhold() {
        return id;
    }

    public Prosent andel() {
        return andel;
    }

    @Override
    public String toString() {
        return stillingsforhold()
                + (erFakturerbar() ? " skal " : " skal ikkje ")
                + "betale premie";
    }
}
