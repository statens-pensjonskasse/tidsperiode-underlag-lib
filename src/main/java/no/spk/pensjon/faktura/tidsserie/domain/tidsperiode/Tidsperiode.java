package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static java.time.LocalDate.MAX;
import static java.util.Comparator.comparing;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;

/**
 * {@link Tidsperiode} representerer ei tidsperiode.
 * <p>
 * Tidsperioda kan enten vere lukka eller løpande. Ei lukka tidsperioder er ei periode som har ein til og med-dato.
 * Ei løpande tidsperiode er ei periode som har ein tom til og med-dato.
 *
 * @author Tarjei Skorgenes
 */
public interface Tidsperiode<T extends Tidsperiode<T>> {
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
    default boolean overlapper(final Tidsperiode<?> other) {
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

    /**
     * Ei algoritme som sorterer tidsperioder kronologisk basert på frå og med- og til og med-dato.
     * <p>
     * Perioder som har ulik frå og med-dato blir sortert kun basert på denne.
     * <p>
     * Dersom periodene startar samtidig blir dei sortert på til og med-dato.
     * <p>
     * Perioder som er løpande blir sortert som om deira til og med-dato er lik {@link LocalDate#MAX}
     *
     * @return ei kronologisk sorteringsrekkefølge for tidsperioder
     * @see Tidsperiode#fraOgMed()
     * @see Tidsperiode#tilOgMed()
     */
    static Comparator<Tidsperiode<?>> kronologiskSorteringAvTidsperioder() {
        return comparing((Tidsperiode<?> p) -> p.fraOgMed())
                .thenComparing((Tidsperiode<?> p) -> p.tilOgMed().orElse(LocalDate.MAX));
    }
}
