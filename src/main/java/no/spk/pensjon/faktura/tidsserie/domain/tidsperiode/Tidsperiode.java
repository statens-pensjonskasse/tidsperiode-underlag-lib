package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static java.time.LocalDate.MAX;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

/**
 * {@link Tidsperiode} representerer ei tidsperiode.
 * <p>
 * Tidsperioda kan enten vere lukka eller l�pande. Ei lukka tidsperioder er ei periode som har ein til og med-dato.
 * Ei l�pande tidsperiode er ei periode som har ein tom til og med-dato.
 *
 * @author Tarjei Skorgenes
 */
public interface Tidsperiode<T extends Tidsperiode<T>> {
    /**
     * F�rste dag i tidsperioda.
     *
     * @return periodas fr� og med-dato
     */
    LocalDate fraOgMed();

    /**
     * Siste dag i tidsperioda viss den ikkje er l�pande.
     *
     * @return siste dag i tidsperioda viss den ikkje er l�pande,
     * ellers {@link java.util.Optional#empty()} for � indikere at den er l�pande
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
        return !(fraOgMed().isAfter(other.tilOgMed().orElse(MAX)) || tilOgMed().orElse(MAX).isBefore(other.fraOgMed()));
    }

    /**
     * Sjekkar om datoen er lik eller er mellom periodas fr� og med- og til og med-datoar.
     *
     * @param dato datoen som skal sjekkast om ligg innanfor perioda
     * @return <code>true</code> dersom datoen ligg innanfor perioda
     */
    default boolean overlapper(final LocalDate dato) {
        return !(dato.isBefore(fraOgMed()) || dato.isAfter(tilOgMed().orElse(MAX)));
    }

    /**
     * Sorterer tidsperiodene kronologisk basert p� periodenes fr� og med- og til og med-dato.
     * <p>
     * Dersom periodene har ulik fr� og med-dato blir perioda med lavast/eldste fr� og med-dato sortert f�rst.
     * <p>
     * Dersom periodene har lik fr� og med-dato, blir perioda med lavaste/eldste til og med-dato sortert f�rst. Dersom
     * ei av periodene er l�pande/manglar til og med-dato, blir den sortert sist.
     * <p>
     * Dersom periodene har lik til og med-dato eller begge er l�pande, blir sorteringsrekkef�lga tilfeldig.
     *
     * @param a f�rste tidsperiode
     * @param b andre tidsperiode
     * @return <code>&lt; 0</code> dersom perioda <code>a</code> blir sortert f�r periode <code>b</code>,
     * <code>0</code> dersom perioda <code>a</code> er lik periode <code>b</code>,
     * <code>&gt; 0</code> dersom perioda <code>a</code> blir sortert etter periode <code>b</code>
     * @since 1.1.2
     */
    static int compare(final Tidsperiode<?> a, final Tidsperiode<?> b) {
        final int resultat = a.fraOgMed().compareTo(b.fraOgMed());
        if (resultat == 0) {
            return a.tilOgMed().orElse(MAX).compareTo(b.tilOgMed().orElse(MAX));
        }
        return resultat;
    }
}
