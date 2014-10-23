package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.TemporalQueries.localDate;

/**
 * {@link Stillingsendring}
 * representerer en tilstandsendring i et stillingsforhold der et eller flere stillingsrelaterte verdier
 * endres.
 * <p>
 * Stillingsendringer kommer fra stillingshistorikken til medlemmet og hver endring tilhører et bestemt stillingsforhold.
 * Stillingsendringer som ikke er tilknyttet et stillingsforhold tas ikke hensyn til i tidsseriegenereringen og kan
 * derfor ses bort fra.
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsendring {
    private static DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private LocalDate aksjonsdato;
    private String aksjonskode;

    /**
     * Datoen som stillingsendringen gjelder fra og med.
     *
     * @param dato en tekstlig representasjon av en dato på formatet yyyy.MM.dd
     * @return <code>this</code>
     */
    public Stillingsendring aksjonsdato(final String dato) {
        this.aksjonsdato = dato(dato);
        return this;
    }

    /**
     * Aksjonskoden som indikerer hvilken type stillingsendring det er snakk om.
     *
     * @param text en tekstlig representasjon av en 3-sifra tallkode som representerer endringstypen
     * @return <code>this</code>
     */
    public Stillingsendring aksjonskode(final String text) {
        this.aksjonskode = text;
        return this;
    }

    /**
     * Oppretter en ny, tom stillingsendring.
     *
     * @return en ny og tom stillingsendring
     */
    public static Stillingsendring stillingsendring() {
        return new Stillingsendring();
    }

    /**
     * Datoen som stillingsendringen gjelder fra og med.
     *
     * @return datoen stillingsendringen inntreffer på
     */
    public LocalDate aksjonsdato() {
        return aksjonsdato;
    }

    /**
     * Konverterer datoar på formatet yyyy.MM.dd frå tekst til {@link java.time.LocalDate}.
     *
     * @param text tekstlig representasjon av datoen som skal konverterast
     * @return den konverterte datoen, eller null viss <code>text</code> er <code>null</code>
     */
    public static LocalDate dato(final String text) {
        if (text == null) {
            return null;
        }
        return yyyyMMddFormat.parse(text).query(localDate());
    }

    /**
     * Representerer stillingsendringen en sluttmelding?
     *
     * @return <code>true</code> dersom endringen er en sluttmelding,
     * <code>false</code> ellers
     */
    public boolean erSluttmelding() {
        return "031".equals(aksjonskode);
    }
}
