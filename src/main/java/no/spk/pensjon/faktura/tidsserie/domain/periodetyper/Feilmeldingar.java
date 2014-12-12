package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.util.List;
import java.util.function.Supplier;

/**
 * Feilmeldingskonstantar og lambda-metoder for bruk ved feila validering av input.
 */
class Feilmeldingar {
    static final Supplier<String> FRA_OG_MED_PAAKREVD = () -> "fra og med-dato er påkrevd, men var null";
    static final Supplier<String> TIL_OG_MED_PAAKREVD = () -> "til og med-dato er påkrevd, men var null";

    static final Supplier<String> AARSTALL_PAAKREVD = () -> "årstall er påkrevd, men var null";
    static final Supplier<String> MAANED_PAAKREVD = () -> "måned er påkrevd, men var null";

    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StatligLoennstrinnperiode} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei statlig lønnstrinnperiode
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForStatligLoennstrinn(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "stillingsendring",
                "typeindikator, lønnstrinn, frå og med-dato, til og med-dato, beløp"
        );
    }

    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.ApotekLoennstrinnperiode} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei statlig lønnstrinnperiode
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForApotekLoennstrinn(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "stillingsendring",
                "typeindikator, frå og med-dato, til og med-dato, lønnstrinn, stillingskode, beløp"
        );
    }

    private static String ugyldigAntallKolonner(List<String> rad, String type, String kolonner) {
        return "Rada inneheldt ikkje forventa antall kolonner.\n"
                + "Ei " + type
                + " må inneholde følgjande kolonner i angitt rekkefølge:\n"
                + kolonner + ".\n"
                + "Rada som feila: " + rad;
    }
}
