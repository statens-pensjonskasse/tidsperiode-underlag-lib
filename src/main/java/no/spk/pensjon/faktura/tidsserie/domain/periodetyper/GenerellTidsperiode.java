package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.LocalDate.MAX;
import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Feilmeldingar.FRA_OG_MED_PAAKREVD;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Feilmeldingar.TIL_OG_MED_PAAKREVD;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.GenerellTidsperiode} representerer ei vanlig
 * tidsperiode som ikkje er av nokon bestemt type.
 *
 * @author Tarjei Skorgenes
 */
public class GenerellTidsperiode {
    protected final LocalDate fraOgMed;
    protected final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny tidsperiode som har ein fr� og med-dato og som kan ha
     * ein sluttdato eller som kan vere l�pande.
     *
     * @param fraOgMed f�rste dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     *                 indikerer det at tidsperioda ikkje er avslutta, ogs� kalla l�pande
     */
    public GenerellTidsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        requireNonNull(fraOgMed, FRA_OG_MED_PAAKREVD);
        requireNonNull(tilOgMed, TIL_OG_MED_PAAKREVD);
        tilOgMed.ifPresent(tilDato -> {
            sjekkForVrengteDatoar(fraOgMed, tilDato);
        });
        this.tilOgMed = tilOgMed;
        this.fraOgMed = fraOgMed;
    }

    /**
     * F�rste dag i tidsperioda.
     *
     * @return periodas fr� og med-dato
     */
    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    /**
     * Siste dag i tidsperioda viss den ikkje er l�pande.
     *
     * @return siste dag i tidsperioda viss den ikkje er l�pande,
     * ellers {@link java.util.Optional#empty()} for � indikere at den er l�pande
     */

    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }

    /**
     * Overlappar dei to periodene kvarandre?
     *
     * @param other den andre perioda som vi skal sjekke om vi overlappar
     * @return <code>true</code> dersom dei to periodene har minst ein felles dato som begge overlappar,
     * <code>false</code> ellers
     */
    public boolean overlapper(final GenerellTidsperiode other) {
        return overlapper(other.fraOgMed()) || other.overlapper(fraOgMed());
    }

    /**
     * Sjekkar om datoen er lik eller er mellom periodas fr� og med- og til og med-datoar.
     *
     * @param dato datoen som skal sjekkast om ligg innanfor perioda
     * @return <code>true</code> dersom datoen ligg innanfor perioda
     */
    public boolean overlapper(final LocalDate dato) {
        return !(dato.isBefore(fraOgMed()) || dato.isAfter(tilOgMed().orElse(MAX)));
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s]", "", fraOgMed(), tilOgMed().map(d -> d.toString()).orElse(""));
    }

    private void sjekkForVrengteDatoar(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        if (fraOgMed.isAfter(tilOgMed)) {
            throw new IllegalArgumentException("fra og med-dato kan ikkje vere etter til og med-dato, men "
                    + fraOgMed + " er etter " + tilOgMed
            );
        }
    }
}
