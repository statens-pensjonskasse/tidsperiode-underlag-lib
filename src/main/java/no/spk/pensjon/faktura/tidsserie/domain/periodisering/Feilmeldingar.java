package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import java.util.List;

class Feilmeldingar {
    /**
     * Oversetting frå <code>rad</code> til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode} har feila
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
    public static String ugyldigAntallKolonnerForStillingsendring(List<String> rad) {
        return ugyldigAntallKolonner(
                rad,
                "stillingsendring",
                "typeindikator, fødselsdato, personnummer, stillingsforhold, aksjonskode, arbeidsgivar, permisjonsavtale, registreringsdato, lønnstrinn, lønn, faste tillegg, variable tillegg, funksjonstillegg og aksjonsdato"
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
