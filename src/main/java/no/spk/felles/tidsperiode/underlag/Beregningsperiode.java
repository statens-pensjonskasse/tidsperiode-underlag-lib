package no.spk.felles.tidsperiode.underlag;

import java.time.LocalDate;

import no.spk.felles.tidsperiode.AntallDagar;
import no.spk.felles.tidsperiode.Tidsperiode;

/**
 * Rolle-interface for tidsperioder som er tilrettelagt for å kunne bli køyrt premieberegning på.
 * <p>
 * Ei beregningsperiode består av ei tidsperiode og eit sett med annotasjonar for verdiane som er gyldige innanfor
 * tidsperioda. Beregningsperioda koblar tidsuavhengige grunnlagsdata frå alle periodetyper som premieberegning treng
 * informasjon frå, til perioda desse verdiane er gyldige i. Periodiseringa som bygger opp beregningsperiodene
 * sikrar at {@link BeregningsRegel beregningsreglane} som utfører
 * beregningar, ikkje treng å ta hensyn til om verdiane dei benyttar er gyldige eller ikkje, om det er +/- 1 dag for
 * beregningar som angår frå og med- eller til og med-datoar, om det er mulig å ha gap eller overlapp mellom periodene
 * dei skal hente verdiar frå osv osv.
 *
 * @param <T> periodetypa som implementerer beregningsperiode
 * @author Tarjei Skorgenes
 */
public interface Beregningsperiode<T extends Tidsperiode<T>> extends Tidsperiode<T>, HarAnnotasjonar {
    /**
     * Slår opp ein beregningsregel av ei bestemt type og brukar den for å gjere ei bestemt type beregning
     * ut frå underlagsperiodas annoterte fakta.
     * <br>
     * Resultatet av ei beregning vil vere ikkje-muterbart over tid slik at fleire kall til samme regel på ei og samme
     * periodeinstans, vil returnere eksakt samme verdi for kvart kall.
     * <br>
     * Periodeimplementasjonen står fritt til å bruke denne egenskapen til å implementere caching av tidligare
     * beregna verdiar for å forbetre beregningsytelsen til reglane.
     *
     * @param regelType kva type beregningsregel som skal brukast
     * @param <T>       typen på resultatet av beregningen
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
     * @since 1.1.2
     */
    AntallDagar lengde();
}
