package no.spk.felles.tidsperiode;

import java.time.LocalDate;

class Validering {
    static void feilVissFraOgMedErEtterTilOgMedDato(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        if (fraOgMed.isAfter(tilOgMed)) {
            throw new IllegalArgumentException("fra og med-dato kan ikkje vere etter til og med-dato, men "
                    + fraOgMed + " er etter " + tilOgMed
            );
        }
    }
}
