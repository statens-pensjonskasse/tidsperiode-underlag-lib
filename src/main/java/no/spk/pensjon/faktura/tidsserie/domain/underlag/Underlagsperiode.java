package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * Ei tidsperiode som inngår som ein del av eit underlag.
 * <p>
 * Underlagsperioder har som funksjon å representere den minste tidsperioda der ingen av underlagsperiodas
 * tilknytta tidsperioder endrar innhold. Underlagsperiodas hensikt er altså å inngå som ein del av eit
 * underlag som kan benyttast for å finne ut og beregne verdiar som baserer seg på grunnlagsdata som er periodiserte
 * og som kan endre verdi eller betydning over tid.
 *
 * @author Tarjei Skorgenes
 */
public class Underlagsperiode {
    private final LocalDate fraOgMed;
    private final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny underlagsperiode som har ein frå og med- og ein til og med-dato ulik <code>null</code>.
     *
     * @param fraOgMed frå og med-dato for underlagsperioda
     * @param tilOgMed til og med-dato for underlagsperioda
     * @throws NullPointerException     viss <code>fraOgMed</code> eller <code>tilOgMed</code> er
     *                                  <code>null</code>
     * @throws IllegalArgumentException dersom fra og med-dato er etter til og med-dato
     */
    public Underlagsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        requireNonNull(fraOgMed, () -> "fra og med-dato er påkrevd, men var null");
        requireNonNull(tilOgMed, () -> "til og med-dato er påkrevd, men var null");
        if (fraOgMed.isAfter(tilOgMed)) {
            throw new IllegalArgumentException("fra og med-dato kan ikkje vere etter til og med-dato, men "
                    + fraOgMed + " er etter " + tilOgMed
            );
        }
        this.fraOgMed = fraOgMed;
        this.tilOgMed = of(tilOgMed);
    }
}
