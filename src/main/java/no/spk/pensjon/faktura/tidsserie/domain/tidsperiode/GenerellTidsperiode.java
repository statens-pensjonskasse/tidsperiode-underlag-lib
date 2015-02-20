package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link GenerellTidsperiode} representerer ei vanlig
 * tidsperiode som ikkje er av nokon bestemt type.
 *
 * @author Tarjei Skorgenes
 */
public class GenerellTidsperiode extends AbstractTidsperiode<GenerellTidsperiode> {
    /**
     * Konstruerer ei ny tidsperiode som har ein fr� og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere l�pande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed f�rste dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     *                 indikerer det at tidsperioda ikkje er avslutta, dvs l�pande
     */
    public GenerellTidsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
    }

    /**
     * Konstruerer ei ny tidsperiode der fr� og med- og til og med-dato er lik
     * <code>kilde</code> sine datoar.
     *
     * @param kilde tidsperioda som fr� og med- og til og med-dato skal kopierast fr�
     * @throws NullPointerException viss <code>kilde</code> er <code>null</code>
     */
    public GenerellTidsperiode(final Tidsperiode<?> kilde) {
        super(requireNonNull(kilde, () -> "kilde er p�krevd, men var null").fraOgMed(), kilde.tilOgMed());
    }

    /**
     * Genererer ei hashkode basert p� periodas fr� og med- og til og med-dato.
     *
     * @return ei hashcode basert p� fr� og med- og til og med-datoen til perioda
     */
    @Override
    public int hashCode() {
        return Objects.hash(fraOgMed, tilOgMed);
    }

    /**
     * Sjekkar om <code>obj</code> er ei {@link GenerellTidsperiode} med lik
     * fr� og med- og til og med-dato.
     *
     * @param obj eit anna objekt som tidsperioda skal samanliknast med
     * @return <code>true</code> dersom <code>obj</code> er ei generell tidsperiode med lik fr� og med- og
     * til og med-dato, <code>false</code> ellers
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
        final GenerellTidsperiode other = (GenerellTidsperiode) obj;
        return fraOgMed.equals(other.fraOgMed) && tilOgMed.equals(other.tilOgMed);
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s]", "", fraOgMed(), tilOgMed().map(d -> d.toString()).orElse(""));
    }
}
