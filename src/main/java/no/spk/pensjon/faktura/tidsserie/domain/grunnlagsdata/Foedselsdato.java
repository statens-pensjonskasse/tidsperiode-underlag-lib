package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * {@link Foedselsdato} representerer datoen eit medlem vart født på.
 * <br>
 * Merk at for personar med D- eller H-nummer er ikkje fødselsdatoen ein gyldig dato, ein må i desse tilfella
 * manipulere på tallverdien for å få ut den reelle fødselsdatoen.
 * <br>
 * Av denne grunn bør ikkje {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato} brukast
 * i andre samanhengar enn situasjonar der ein treng å holde på ein fødselsdato som skal inngå som ein del av
 * eit personnummer.
 * <br>
 * <h6>Referansar</h6>
 * <ul>
 * <li><a href="https://no.wikipedia.org/wiki/F%C3%B8dselsnummer#D-nummer">Fødselsnummer</a></li>
 * <li><a href="http://www.fnrinfo.no/Info/Oppbygging.aspx">Hvilken informasjon finnes i et fødselsnummer?</a></li>
 * </ul>
 *
 * @author Tarjei Skorgenes
 */
public final class Foedselsdato {
    private static final DateTimeFormatter yyyyMMddFormatUtenPunktum = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Nedre grense for datoar som er tillatt å bruke i FFF-sammenheng.
     * <br>
     * Datoen er semi-tilfeldig valgt for å sikre at vi er i stand til å beregne alle medlemmar som nokonsinne har
     * blitt registrert i kasper pr juli 2015.
     */
    private static final int NEDRE_DATOGRENSE = 18750101;

    private final Integer dato;

    /**
     * Konstruerer ein ny fødselsdato.
     * <br>
     * For å gjere det mulig å oppdage inkonsistens i grunnlagsdatane fører datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er født før 1875 og det er vel
     * rimelig å anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart født.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     * @deprecated Støttar ikkje D- eller H-nummer, bruk heller {@link #foedselsdato(Integer)}
     */
    public Foedselsdato(final LocalDate dato) {
        this(tilNummer(dato));
    }

    /**
     * Konstruerer ein ny fødselsdato.
     * <br>
     * For å gjere det mulig å oppdage inkonsistens i grunnlagsdatane fører datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er født før 1875 og det er vel
     * rimelig å anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart født.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     */
    private Foedselsdato(final Integer dato) {
        if (requireNonNull(dato, "fødseldato er påkrevd, men var null") < NEDRE_DATOGRENSE) {
            throw new IllegalArgumentException(
                    "fødselsdatoar eldre enn " + NEDRE_DATOGRENSE + " er ikkje støtta, var " + dato
            );
        }
        this.dato = dato;
    }

    /**
     * Konstruerer ein ny fødselsdato basert på ein dato.
     * <br>
     * NB: Merk at denne konstruksjonsmetoda ikkje kan brukast i kombinasjon med dato-verdiar som inngår i
     * eit D- eller H-nummer sidan desse ikkje inneheld gyldige datoar.
     *
     * @param dato datoen medlemmet vart født.
     * @return ny fødselsdato
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     * @see Foedselsdato#Foedselsdato(Integer)
     * @deprecated Støttar ikkje D- eller H-nummer, bruk heller {@link #foedselsdato(Integer)}
     */
    public static Foedselsdato foedselsdato(final LocalDate dato) {
        return new Foedselsdato(tilNummer(dato));
    }

    /**
     * Konstruerer ein ny fødselsdato.
     * <br>
     * Fødselsdatoar oppretta via denne metoda støttar både D- og H-nummer. Desse datoverdiane er ikkje
     * gyldige datoar sidan dei inneheld dagar større enn 31 eller månedar større enn 12.
     *
     * @param dato datoen medlemmet vart født.
     * @return ny fødselsdato
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er før år 1875 sidan det er
     * @see Foedselsdato#Foedselsdato(Integer)
     */
    public static Foedselsdato foedselsdato(final Integer dato) {
        return new Foedselsdato(dato);
    }

    @Override
    public int hashCode() {
        return dato.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Foedselsdato)) {
            return false;
        }
        final Foedselsdato other = (Foedselsdato) obj;
        return dato.equals(other.dato);
    }

    /**
     * Konverterer datoen til eit tall.
     * <br>
     * Tallet blir generert basert på følgjande formel:
     * <code>
     * abs(årstall) x 10 000 + måned i året x 100 + dag
     * </code>
     *
     * @return fødselsdato konvertert til eit tall
     */
    public String tilKode() {
        return dato.toString();
    }

    @Override
    public String toString() {
        return "født " + dato.toString();
    }

    private static Integer tilNummer(final LocalDate dato) {
        return Integer.valueOf(
                yyyyMMddFormatUtenPunktum.format(
                        requireNonNull(dato, "fødseldato er påkrevd, men var null")
                )
        );
    }
}
