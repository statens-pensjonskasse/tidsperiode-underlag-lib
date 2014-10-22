package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link Underlag} representerer eit periodisert
 * tidsperiode best�ande av ei eller fleire
 * {@link Underlagsperiode underlagsperioder}.
 * <p>
 * Eit underlag, med tilh�yrande underlagsperioder, skal underst�tte beregningar/sp�rringar som skal utf�rast basert
 * p� verdiar henta fr� 2 eller fleire periodiserte datatyper som kan individuelt variere over tid, fr� dag til dag,
 * veke til veke, m�ned til m�ned eller andre semi-tilfeldige variasjonsm�nster.
 * <p>
 * Denne typen variasjon fr� periode til periode, med mange niv� av mulige overlappande perioder, er hovedmotivasjonen
 * for underlags- og underlagsperiode-konsepta.
 * <br>
 * Underlaget inneheld underlagsperioder og kvar underlagsperiode representerer den minste tidsperioda som ein kan
 * behandle utan at nokon av dei tilknytta tidsperiodiske datasetta, endrar tilstand innanfor tidsperioda.
 * <h2>Koblingar</h2>
 * Sidan konstruksjon av underlag og underlagsperioder krever eit periodisert datasett som input, er det �nskelig �
 * kunne spore tilbake fr� genererte underlagsperioder til input-periodene som f�rte til at dei vart oppretta.
 * <p>
 * For � st�tte denne typen sporing, gir kvar underlagsperiode tilgang til alle input-perioder som den er kobla til.
 * Dette er alle tidsperiodene som overlappar underlagsperiode, enten delvis eller fullstendig.
 * <h3>Eksempel</h3>
 * Tore startar i ny stilling 1. januar 2001, g�r opp fr� l�nnstrinn 10 til 20 3. juni 2001 og sluttar i stilling
 * 31. desember 2001, han jobbar heile tida i 100% stilling.
 * <p>
 * L�nnstrinn 10 endrar bel�p 1. mai 2001, fr� kr 100 000 til kr 105 000, 20 endrar bel�p 1. mai 2001, fr� kr 200 000
 * til kr 210 000. Ingen seinare endring av l�nnstrinnbel�pa er registrert.
 * <br>
 * Eit underlag generert ut fr� desse 6 tidsperiodene (2 stillingsforholdperioder + 4 l�nnstrinnperioder) best�r
 * av f�lgjande 3 underlagsperioder:
 * <table>
 * <thread>
 * <tr>Fr� og med-dato</tr>
 * <tr>Til og med-dato</tr>
 * <tr>Tilknytta perioder</tr>
 * </thread>
 * <tbody>
 * <tr>
 * <td>2001.01.01</td>
 * <td>2001.04.30</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -> 2001.06.02
 * L�nnstrinnperiode ltr. 10, 2000.05.01 -> 2001.04.30
 * L�nnstrinnperiode ltr. 20, 2000.05.01 -> 2001.04.30
 * </td>
 * </tr>
 * <tr>
 * <td>2001.05.01</td>
 * <td>2001.06.02</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -> 2001.06.02
 * L�nnstrinnperiode ltr. 10, 2001.05.01 -> l�pande
 * L�nnstrinnperiode ltr. 20, 2001.05.01 -> l�pande
 * </td>
 * </tr>
 * <tr>
 * <td>2001.06.03</td>
 * <td>2001.12.31</td>
 * <td>
 * Stillingsforholdperiode 2001.06.03 -> 2001.12.31
 * L�nnstrinnperiode ltr. 10, 2001.05.01 -> l�pande
 * L�nnstrinnperiode ltr. 20, 2001.05.01 -> l�pande
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * Ut fr� dette underlaget blir det no trivielt � beregne utbetalt l�nn for Tore for 2001, ein kan iterere over kvar
 * underlagsperiode, og sl� opp n�dvendige verdiar for beregninga fr� underlagsperiodas tilknytta perioder. Tilsvarande
 * kan ein no enkelt finne ut om ein har perioder med inkonsistente eller manglande data, f.eks. om det finnes
 * underlagsperioder utan kobling til ei stillingsforholdperiode, eller med kobling til meir enn ei
 * stillingsforholdperiode.
 *
 * @author Tarjei Skorgenes
 */
public class Underlag implements Iterable<Underlagsperiode> {
    private final ArrayList<Underlagsperiode> perioder = new ArrayList<>();

    /**
     * Konstruerer eit nytt underlag ut fr� ein straum
     *
     * @param perioder underlagsperiodene som underlaget er bygd opp av
     */
    public Underlag(final Stream<Underlagsperiode> perioder) {
        perioder
                .collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Underlagsperiode> iterator() {
        return perioder.iterator();
    }

    /**
     * Returnerer ein kronologisk straum som innehelde underlagets underlagsperioder.
     *
     * @return ein kronologisk straum av underlagsperiodene som inng�r i underlaget, fr� eldste til yngste periode
     */
    public Stream<Underlagsperiode> stream() {
        return perioder.stream();
    }

    /**
     * Returnerer ei ikkje-modifiserbar liste som inneheld underlagsperiodene som underlaget er bygd opp av.
     * <p>
     * Metoda anbefalast kun brukt i situasjonar der ein m� sl� opp underlagsperiodene etter rekkef�lge/index,
     * den prim�re tilgangen til underlagets perioder b�r skje via {@link #stream()}.
     *
     * @return underlagsperiodene i underlaget i kronologisk rekkef�lge
     */
    public List<Underlagsperiode> toList() {
        return Collections.unmodifiableList(perioder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "U" + perioder;
    }
}
