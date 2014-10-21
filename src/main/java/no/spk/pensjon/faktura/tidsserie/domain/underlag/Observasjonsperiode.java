package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.LocalDate.MAX;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * {@link Observasjonsperiode} representerer
 * nedre grense for fr� og med-datoen og �vre grense for til og med-datoen til eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}.
 * <p>
 * Hovedintensjonen ved dette konseptet er � kunne avgrense st�rrelsen fram i tid p� underlaget i situasjonar der
 * ei eller fleire av input-periodene til underlaget er l�pande og dermed er varig evig langt inn i framtida.
 *
 * @author Tarjei Skorgenes
 */
public class Observasjonsperiode {
    private final LocalDate fraOgMed;
    private final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny grenser.
     *
     * @param fraOgMed nedre grense for fr� og med-dato til f�rste underlagsperiode i eit underlag
     * @param tilOgMed �vre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        requireNonNull(fraOgMed, () -> "fra og med-dato er p�krevd, men var null");
        requireNonNull(tilOgMed, () -> "til og med-dato er p�krevd, men var null");
        this.fraOgMed = fraOgMed;
        this.tilOgMed = of(tilOgMed);
    }

    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }

    /**
     * Sjekkar om datoen ligg p� eller mellom periodas fr� og med- og til og med-datoar.
     *
     * @param dato datoen som skal sjekkast om ligg innanfor perioda
     * @return <code>true</code> dersom datoen ligg innanfor perioda
     */
    public boolean overlapper(final LocalDate dato) {
        return !(dato.isBefore(fraOgMed()) || dato.isAfter(tilOgMed().orElse(MAX)));
    }
}
