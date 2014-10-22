package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.util.function.Supplier;

/**
 * Feilmeldingskonstantar og lambda-metoder for bruk ved feila validering av input.
 */
class Feilmeldingar {
    static final Supplier<String> FRA_OG_MED_PAAKREVD = () -> "fra og med-dato er påkrevd, men var null";
    static final Supplier<String> TIL_OG_MED_PAAKREVD = () -> "til og med-dato er påkrevd, men var null";
}
