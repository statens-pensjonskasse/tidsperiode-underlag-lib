package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link Underlag} representerer eit periodisert
 * tidsperiode beståande av ei eller fleire
 * {@link Underlagsperiode underlagsperioder}.
 * <p>
 * Eit underlag, med tilhøyrande underlagsperioder, skal understøtte beregningar/spørringar som skal utførast basert
 * på verdiar henta frå 2 eller fleire periodiserte datatyper som kan individuelt variere over tid, frå dag til dag,
 * veke til veke, måned til måned eller andre semi-tilfeldige variasjonsmønster.
 * <p>
 * Denne typen variasjon frå periode til periode, med mange nivå av mulige overlappande perioder, er hovedmotivasjonen
 * for underlags- og underlagsperiode-konsepta.
 * <br>
 * Underlaget inneheld underlagsperioder og kvar underlagsperiode representerer den minste tidsperioda som ein kan
 * behandle utan at nokon av dei tilknytta tidsperiodiske datasetta, endrar tilstand innanfor tidsperioda.
 * <h2>Koblingar</h2>
 * Sidan konstruksjon av underlag og underlagsperioder krever eit periodisert datasett som input, er det ønskelig å
 * kunne spore tilbake frå genererte underlagsperioder til input-periodene som førte til at dei vart oppretta.
 * <p>
 * For å støtte denne typen sporing, gir kvar underlagsperiode tilgang til alle input-perioder som den er kobla til.
 * Dette er alle tidsperiodene som overlappar underlagsperiode, enten delvis eller fullstendig.
 * <h3>Eksempel</h3>
 * Tore startar i ny stilling 1. januar 2001, går opp frå lønnstrinn 10 til 20 3. juni 2001 og sluttar i stilling
 * 31. desember 2001, han jobbar heile tida i 100% stilling.
 * <p>
 * Lønnstrinn 10 endrar beløp 1. mai 2001, frå kr 100 000 til kr 105 000, 20 endrar beløp 1. mai 2001, frå kr 200 000
 * til kr 210 000. Ingen seinare endring av lønnstrinnbeløpa er registrert.
 * <br>
 * Eit underlag generert ut frå desse 6 tidsperiodene (2 stillingsforholdperioder + 4 lønnstrinnperioder) består
 * av følgjande 3 underlagsperioder:
 * <table>
 * <thread>
 * <tr>Frå og med-dato</tr>
 * <tr>Til og med-dato</tr>
 * <tr>Tilknytta perioder</tr>
 * </thread>
 * <tbody>
 * <tr>
 * <td>2001.01.01</td>
 * <td>2001.04.30</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -> 2001.06.02
 * Lønnstrinnperiode ltr. 10, 2000.05.01 -> 2001.04.30
 * Lønnstrinnperiode ltr. 20, 2000.05.01 -> 2001.04.30
 * </td>
 * </tr>
 * <tr>
 * <td>2001.05.01</td>
 * <td>2001.06.02</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -> 2001.06.02
 * Lønnstrinnperiode ltr. 10, 2001.05.01 -> løpande
 * Lønnstrinnperiode ltr. 20, 2001.05.01 -> løpande
 * </td>
 * </tr>
 * <tr>
 * <td>2001.06.03</td>
 * <td>2001.12.31</td>
 * <td>
 * Stillingsforholdperiode 2001.06.03 -> 2001.12.31
 * Lønnstrinnperiode ltr. 10, 2001.05.01 -> løpande
 * Lønnstrinnperiode ltr. 20, 2001.05.01 -> løpande
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * Ut frå dette underlaget blir det no trivielt å beregne utbetalt lønn for Tore for 2001, ein kan iterere over kvar
 * underlagsperiode, og slå opp nødvendige verdiar for beregninga frå underlagsperiodas tilknytta perioder. Tilsvarande
 * kan ein no enkelt finne ut om ein har perioder med inkonsistente eller manglande data, f.eks. om det finnes
 * underlagsperioder utan kobling til ei stillingsforholdperiode, eller med kobling til meir enn ei
 * stillingsforholdperiode.
 *
 * @author Tarjei Skorgenes
 */
public class Underlag implements Iterable<Underlagsperiode> {
    private final ArrayList<Underlagsperiode> perioder = new ArrayList<>();

    /**
     * Konstruerer eit nytt underlag ut frå ein straum
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
     * @return ein kronologisk straum av underlagsperiodene som inngår i underlaget, frå eldste til yngste periode
     */
    public Stream<Underlagsperiode> stream() {
        return perioder.stream();
    }

    /**
     * Returnerer ei ikkje-modifiserbar liste som inneheld underlagsperiodene som underlaget er bygd opp av.
     * <p>
     * Metoda anbefalast kun brukt i situasjonar der ein må slå opp underlagsperiodene etter rekkefølge/index,
     * den primære tilgangen til underlagets perioder bør skje via {@link #stream()}.
     *
     * @return underlagsperiodene i underlaget i kronologisk rekkefølge
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
