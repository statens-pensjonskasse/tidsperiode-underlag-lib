package no.spk.felles.tidsperiode;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

import no.spk.felles.tidsperiode.Tidsperiode;
import no.spk.felles.tidsperiode.Validering;

public abstract class AbstractTidsperiode<T extends Tidsperiode<T>> implements Tidsperiode<T> {
    protected final LocalDate fraOgMed;
    protected final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny tidsperiode som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed første dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @throws NullPointerException     viss <code>fraOgMed</code>
     *                                  eller <code>tilOgMed</code> er <code>null</code>
     * @throws IllegalArgumentException dersom <code>fraOgMed</code> er
     *                                  {@link LocalDate#isAfter(ChronoLocalDate) etter} <code>tilOgMed</code>
     */
    protected AbstractTidsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        requireNonNull(fraOgMed, "fra og med-dato er påkrevd, men var null");
        requireNonNull(tilOgMed, "til og med-dato er påkrevd, men var null");
        tilOgMed.ifPresent(tilDato -> {
            Validering.feilVissFraOgMedErEtterTilOgMedDato(fraOgMed, tilDato);
        });
        this.tilOgMed = tilOgMed;
        this.fraOgMed = fraOgMed;
    }

    @Override
    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    @Override
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }
}
