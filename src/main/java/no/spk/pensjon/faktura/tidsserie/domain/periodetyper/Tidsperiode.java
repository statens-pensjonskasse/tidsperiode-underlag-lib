package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.LocalDate.MAX;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode} representerer ei tidsperiode.
 * <p>
 * Tidsperioda kan enten vere lukka eller løpande. Ei lukka tidsperioder er ei periode som har ein til og med-dato.
 * Ei løpande tidsperiode er ei periode som har ein tom til og med-dato.
 *
 * @author Tarjei Skorgenes
 */
public interface Tidsperiode {
    /**
     * Første dag i tidsperioda.
     *
     * @return periodas frå og med-dato
     */
    LocalDate fraOgMed();

    /**
     * Siste dag i tidsperioda viss den ikkje er løpande.
     *
     * @return siste dag i tidsperioda viss den ikkje er løpande,
     * ellers {@link java.util.Optional#empty()} for å indikere at den er løpande
     */

    Optional<LocalDate> tilOgMed();

    /**
     * Overlappar dei to periodene kvarandre?
     *
     * @param other den andre perioda som vi skal sjekke om vi overlappar
     * @return <code>true</code> dersom dei to periodene har minst ein felles dato som begge overlappar,
     * <code>false</code> ellers
     */
    default boolean overlapper(final Tidsperiode other) {
        return overlapper(other.fraOgMed()) || other.overlapper(fraOgMed());
    }

    /**
     * Sjekkar om datoen er lik eller er mellom periodas frå og med- og til og med-datoar.
     *
     * @param dato datoen som skal sjekkast om ligg innanfor perioda
     * @return <code>true</code> dersom datoen ligg innanfor perioda
     */
    default boolean overlapper(final LocalDate dato) {
        return !(dato.isBefore(fraOgMed()) || dato.isAfter(tilOgMed().orElse(MAX)));
    }
}
