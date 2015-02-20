package no.spk.pensjon.faktura.tidsserie.storage.csv;

import java.util.List;

class Feilmeldingar {
    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei avtalekobling
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForAvtalekobling(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "avtalekobling",
                "typeindikator, fødselsdato, personnummer, stillingsforholdnummer, startdato, sluttdato, avtalenummer og ordning"
        );
    }

    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei stillingsendring
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForStillingsendring(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "stillingsendring",
                "typeindikator, fødselsdato, personnummer, stillingsforhold, aksjonskode, arbeidsgivar, permisjonsavtale, registreringsdato, lønnstrinn, lønn, faste tillegg, variable tillegg, funksjonstillegg og aksjonsdato"
        );
    }

    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei medregningsperiode
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForMedregningsperiode(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "medregningsperiode",
                "typeindikator, fødselsdato, personnummer, stillingsforhold, frå og med-dato, til og med-dato, medregningskode og lønn"
        );
    }


    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Omregningsperiode} har feila
     * fordi antall kolonner i <code>rad</code> ikkje var som forventa.
     *
     * @param rad input-rada som inneholdt feil antall kolonner for ei omregningsperiode
     * @return ei feilmelding som beskriv kva som er forventa format på rada og kva den faktisk inneholdt
     */
    public static String ugyldigAntallKolonnerForOmregningsperiode(final List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "stillingsendring",
                "typeindikator, frå og med-dato, til og med-dato, grunnbeløp"
        );
    }

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
