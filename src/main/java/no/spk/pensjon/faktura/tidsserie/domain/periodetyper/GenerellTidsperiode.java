package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.GenerellTidsperiode} representerer ei vanlig
 * tidsperiode som ikkje er av nokon bestemt type.
 *
 * @author Tarjei Skorgenes
 */
public class GenerellTidsperiode extends AbstractTidsperiode<GenerellTidsperiode> {
    /**
     * Konstruerer ei ny tidsperiode som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed første dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     *                 indikerer det at tidsperioda ikkje er avslutta, dvs løpande
     */
    public GenerellTidsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s]", "", fraOgMed(), tilOgMed().map(d -> d.toString()).orElse(""));
    }
}
