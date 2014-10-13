package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.time.LocalDate;

/**
 * Builder for {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}.
 */
public class UnderlagsperiodeBuilder {
    private LocalDate fraOgMed;
    private LocalDate tilOgMed;

    /**
     * Frå og med-datoen som underlagsperioder bygd av builderen skal benytte.
     *
     * @param dato den nye frå og med-datoen
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder fraOgMed(final LocalDate dato) {
        fraOgMed = dato;
        return this;
    }

    /**
     * Til og med-datoen som underlagsperioder bygd av builderen skal benytte.
     *
     * @param dato den nye til og med-datoen
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder tilOgMed(final LocalDate dato) {
        tilOgMed = dato;
        return this;
    }

    public Underlagsperiode bygg() {
        return new Underlagsperiode(fraOgMed, tilOgMed);
    }
}
