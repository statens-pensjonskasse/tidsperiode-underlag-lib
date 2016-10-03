package no.spk.felles.tidsperiode.underlag;

import java.time.LocalDate;

import no.spk.felles.tidsperiode.AntallDagar;
import no.spk.felles.tidsperiode.Tidsperiode;

/**
 * Rolle-grensesnitt for tidsperioder som er tilrettelagt for å gjere utrekningar på.
 * <p>
 * Ei beregningsperiode består av ei tidsperiode og eit sett med annotasjonar for verdiane som er gyldige innanfor
 * tidsperioda. Periodiseringa som bygger opp beregningsperiodene sikrar at {@link BeregningsRegel beregningsreglane}
 * som gjennomfører utrekningar, ikkje treng å ta hensyn til om verdiane dei benyttar er gyldige eller ikkje,
 * om det er +/- 1 dag for utrekningar som angår frå og med- eller til og med-datoar, om det er mulig å ha gap eller
 * overlapp mellom periodene dei skal hente verdiar frå osv osv.
 *
 * @param <T> periodetypa som implementerer grensesnittet
 * @author Tarjei Skorgenes
 */
public interface Beregningsperiode<T extends Tidsperiode<T>> extends Tidsperiode<T>, HarAnnotasjonar {
    /**
     * Slår opp ein beregningsregel av ei bestemt type og brukar den for å gjennomføre ei bestemt type utrekning
     * basert på periodas annoterte verdiar.
     * <br>
     * Resultatet av ei utrekning skal vere ikkje-muterbart over tid slik at fleire kall til samme regel på ei og samme
     * periodeinstans, vil returnere eksakt samme verdi for kvart kall.
     * <br>
     * Periodeimplementasjonen står fritt til å bruke denne egenskapen til å implementere caching av tidligare
     * beregna verdiar for å forbetre ytelsen til reglane.
     *
     * @param regelType kva type beregningsregel som skal brukast
     * @param <T>       typen på resultatet av utrekninga
     * @return resultatet frå beregningsregelen basert på underlagsperiodas tilstand
     * @throws PaakrevdAnnotasjonManglarException dersom det ikkje eksisterer nokon beregningsregel
     *                                            av den angitte typa som er gyldig innanfor beregningsperioda
     */
    <T> T beregn(Class<? extends BeregningsRegel<T>> regelType) throws PaakrevdAnnotasjonManglarException;

    /**
     * Lengda på perioda angitt i antall kalendardagar.
     * <br>
     * Lengda inkluderer frå og med- og til og med-datoane + alle dagar mellom dei to.
     *
     * @return antall dagar tidsperioda overlappar
     * @see AntallDagar#antallDagarMellom(LocalDate, LocalDate)
     */
    AntallDagar lengde();
}
