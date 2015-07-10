package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * {@link Foedselsdato} representerer datoen eit medlem vart f�dt p�.
 * <br>
 * Merk at for personar med D- eller H-nummer er ikkje f�dselsdatoen ein gyldig dato, ein m� i desse tilfella
 * manipulere p� tallverdien for � f� ut den reelle f�dselsdatoen.
 * <br>
 * Av denne grunn b�r ikkje {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato} brukast
 * i andre samanhengar enn situasjonar der ein treng � holde p� ein f�dselsdato som skal inng� som ein del av
 * eit personnummer.
 * <br>
 * <h6>Referansar</h6>
 * <ul>
 * <li><a href="https://no.wikipedia.org/wiki/F%C3%B8dselsnummer#D-nummer">F�dselsnummer</a></li>
 * <li><a href="http://www.fnrinfo.no/Info/Oppbygging.aspx">Hvilken informasjon finnes i et f�dselsnummer?</a></li>
 * </ul>
 *
 * @author Tarjei Skorgenes
 */
public final class Foedselsdato {
    private static final DateTimeFormatter yyyyMMddFormatUtenPunktum = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Nedre grense for datoar som er tillatt � bruke i FFF-sammenheng.
     * <br>
     * Datoen er semi-tilfeldig valgt for � sikre at vi er i stand til � beregne alle medlemmar som nokonsinne har
     * blitt registrert i kasper pr juli 2015.
     */
    private static final int NEDRE_DATOGRENSE = 18750101;

    private final Integer dato;

    /**
     * Konstruerer ein ny f�dselsdato.
     * <br>
     * For � gjere det mulig � oppdage inkonsistens i grunnlagsdatane f�rer datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er f�dt f�r 1875 og det er vel
     * rimelig � anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart f�dt.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
     * @deprecated St�ttar ikkje D- eller H-nummer, bruk heller {@link #foedselsdato(Integer)}
     */
    public Foedselsdato(final LocalDate dato) {
        this(tilNummer(dato));
    }

    /**
     * Konstruerer ein ny f�dselsdato.
     * <br>
     * For � gjere det mulig � oppdage inkonsistens i grunnlagsdatane f�rer datoar eldre enn 1. januar 1875 til at det
     * blir kasta ein feil. Det eksisterer pr 29. juni 2015 ingen medlemmar i kasper som er f�dt f�r 1875 og det er vel
     * rimelig � anta at det ikkje dukkar opp fleire medlemmar eldre enn dette nokon gang i framtida.
     *
     * @param dato datoen medlemmet vart f�dt.
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
     */
    private Foedselsdato(final Integer dato) {
        if (requireNonNull(dato, "f�dseldato er p�krevd, men var null") < NEDRE_DATOGRENSE) {
            throw new IllegalArgumentException(
                    "f�dselsdatoar eldre enn " + NEDRE_DATOGRENSE + " er ikkje st�tta, var " + dato
            );
        }
        this.dato = dato;
    }

    /**
     * Konstruerer ein ny f�dselsdato basert p� ein dato.
     * <br>
     * NB: Merk at denne konstruksjonsmetoda ikkje kan brukast i kombinasjon med dato-verdiar som inng�r i
     * eit D- eller H-nummer sidan desse ikkje inneheld gyldige datoar.
     *
     * @param dato datoen medlemmet vart f�dt.
     * @return ny f�dselsdato
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
     * @see Foedselsdato#Foedselsdato(Integer)
     * @deprecated St�ttar ikkje D- eller H-nummer, bruk heller {@link #foedselsdato(Integer)}
     */
    public static Foedselsdato foedselsdato(final LocalDate dato) {
        return new Foedselsdato(tilNummer(dato));
    }

    /**
     * Konstruerer ein ny f�dselsdato.
     * <br>
     * F�dselsdatoar oppretta via denne metoda st�ttar b�de D- og H-nummer. Desse datoverdiane er ikkje
     * gyldige datoar sidan dei inneheld dagar st�rre enn 31 eller m�nedar st�rre enn 12.
     *
     * @param dato datoen medlemmet vart f�dt.
     * @return ny f�dselsdato
     * @throws NullPointerException     viss <code>dato</code> var <code>null</code>
     * @throws IllegalArgumentException viss <code>dato</code> er f�r �r 1875 sidan det er
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
     * Tallet blir generert basert p� f�lgjande formel:
     * <code>
     * abs(�rstall) x 10 000 + m�ned i �ret x 100 + dag
     * </code>
     *
     * @return f�dselsdato konvertert til eit tall
     */
    public String tilKode() {
        return dato.toString();
    }

    @Override
    public String toString() {
        return "f�dt " + dato.toString();
    }

    private static Integer tilNummer(final LocalDate dato) {
        return Integer.valueOf(
                yyyyMMddFormatUtenPunktum.format(
                        requireNonNull(dato, "f�dseldato er p�krevd, men var null")
                )
        );
    }
}
