package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Feilmeldingar.feilmeldingVedOverlappandeTidsperioder;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Feilmeldingar.feilmeldingVedTidsgapIUnderlaget;

/**
 * {@link Underlag} representerer eit periodisert tidsperiode best�ande av ei eller fleire
 * {@link Underlagsperiode underlagsperioder}.
 * <p>
 * Eit underlag, med tilh�yrande underlagsperioder, skal underst�tte beregningar/sp�rringar som skal utf�rast basert
 * p� verdiar henta fr� 2 eller fleire periodiserte datatyper som kan individuelt variere over tid, fr� dag til dag,
 * veke til veke, m�ned til m�ned eller andre semi-tilfeldige variasjonsm�nster.
 * </p>
 * Denne typen variasjon fr� periode til periode, med mange niv� av mulige overlappande perioder, er hovedmotivasjonen
 * for underlags- og underlagsperiode-konsepta.
 * <p>
 * Underlaget inneheld underlagsperioder og kvar underlagsperiode representerer den minste tidsperioda som ein kan
 * behandle utan at nokon av dei tilknytta tidsperiodiske datasetta, endrar tilstand innanfor tidsperioda.
 * </p>
 * <h2>Kontrakt</h2>
 * <p>
 * Den viktigaste kontrakta som klientar som brukar underlaget kan basere seg p� er at at det ikkje skal kunne
 * eksistere tidsgap mellom ei eller fleire av underlagsperiodene som inng�r i underlaget. Fors�k p� � konstruere
 * eit underlag med tidsgap skal feile umiddelbart.
 * </p>
 * <p>
 * Underlaget skal vere bygd opp av underlagsperioder sortert i kronologisk rekkef�lge, med eldste periode f�rste og
 * nyaste periode sist.
 * </p>
 * <h2>Koblingar</h2>
 * Sidan konstruksjon av underlag og underlagsperioder krever eit periodisert datasett som input, er det �nskelig �
 * kunne spore tilbake fr� genererte underlagsperioder til input-periodene som f�rte til at dei vart oppretta.
 * <p>
 * For � st�tte denne typen sporing, gir kvar underlagsperiode tilgang til alle input-perioder som den er kobla til.
 * Dette er alle tidsperiodene som overlappar underlagsperiode, enten delvis eller fullstendig.
 * </p>
 * <h2>Eksempel</h2>
 * Tore startar i ny stilling 1. januar 2001, g�r opp fr� l�nnstrinn 10 til 20 3. juni 2001 og sluttar i stilling
 * 31. desember 2001, han jobbar heile tida i 100% stilling.
 * <p>
 * L�nnstrinn 10 endrar bel�p 1. mai 2001, fr� kr 100 000 til kr 105 000, 20 endrar bel�p 1. mai 2001, fr� kr 200 000
 * til kr 210 000. Ingen seinare endring av l�nnstrinnbel�pa er registrert.
 * </p>
 * <p>
 * Eit underlag generert ut fr� desse 6 tidsperiodene (2 stillingsforholdperioder + 4 l�nnstrinnperioder) best�r
 * av f�lgjande 3 underlagsperioder:
 * </p>
 * <table summary="">
 * <thead>
 * <tr>
 * <td>Fr� og med-dato</td>
 * <td>Til og med-dato</td>
 * <td>Tilknytta perioder</td>
 * </thead>
 * <tbody>
 * <tr>
 * <td>2001.01.01</td>
 * <td>2001.04.30</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -&gt; 2001.06.02
 * L�nnstrinnperiode ltr. 10, 2000.05.01 -&gt; 2001.04.30
 * L�nnstrinnperiode ltr. 20, 2000.05.01 -&gt; 2001.04.30
 * </td>
 * </tr>
 * <tr>
 * <td>2001.05.01</td>
 * <td>2001.06.02</td>
 * <td>
 * Stillingsforholdperiode 2001.01.01 -&gt; 2001.06.02
 * L�nnstrinnperiode ltr. 10, 2001.05.01 -&gt; l�pande
 * L�nnstrinnperiode ltr. 20, 2001.05.01 -&gt; l�pande
 * </td>
 * </tr>
 * <tr>
 * <td>2001.06.03</td>
 * <td>2001.12.31</td>
 * <td>
 * Stillingsforholdperiode 2001.06.03 -&gt; 2001.12.31
 * L�nnstrinnperiode ltr. 10, 2001.05.01 -&gt; l�pande
 * L�nnstrinnperiode ltr. 20, 2001.05.01 -&gt; l�pande
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * Ut fr� dette underlaget blir det no trivielt � beregne utbetalt l�nn for Tore for 2001, ein kan iterere over kvar
 * underlagsperiode, og sl� opp n�dvendige verdiar for beregninga fr� underlagsperiodas tilknytta perioder. Tilsvarande
 * kan ein no enkelt finne ut om ein har perioder med inkonsistente eller manglande data, f.eks. om det finnes
 * underlagsperioder utan kobling til ei stillingsforholdperiode, eller med kobling til meir enn ei
 * stillingsforholdperiode.
 * </p>
 *
 * @author Tarjei Skorgenes
 */
public class Underlag implements Iterable<Underlagsperiode>, Annoterbar<Underlag>, HarAnnotasjonar {
    private final ArrayList<Underlagsperiode> perioder = new ArrayList<>();

    private final Annotasjonar annotasjonar = new Annotasjonar();

    /**
     * Konstruerer eit nytt underlag ut fr� ein straum med underlagsperioder sortert i kronologisk rekkef�lge.
     * <p>
     * Dersom periodene ikkje er sortert eller dersom det eksisterer tidsgap mellom underlagsperiodene vil konstruksjon
     * av nytt underlag feile.
     *
     * @param perioder underlagsperiodene som underlaget er bygd opp av
     * @throws IllegalArgumentException dersom det blir oppdaga eit tidsgap mellom ei eller fleire av underlagsperiodene
     */
    public Underlag(final Stream<Underlagsperiode> perioder) {
        perioder
                .sorted((a, b) -> a.fraOgMed().compareTo(b.fraOgMed()))
                .collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
        detekterTidsgapMellomPerioder();
        detekterOverlappandePerioder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Underlagsperiode> iterator() {
        return Collections.unmodifiableList(perioder).iterator();
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
     * Returnerer eit avgrensa underlag der underlagsperioder som predikatet avviser, er filtrert bort.
     * <p>
     * Ettersom det ikkje er tillatt for eit underlag � inneholde gap mellom underlagsperiodene kan ikkje resultatet
     * av filtreringa medf�re at det nye underlaget har tidsgap mellom ei eller fleire av underlagsperiodene. Viss s�
     * blir tilfellet indikerer det ein feil i predikatet som klienten har sendt inn.
     *
     * @param predikat eit predikat som filtrerer bort perioder som det nye underlaget ikkje skal inneholde
     * @return ein filtrert kopi av det gjeldande underlaget, utan alle underlagsperioder som perdikatet har forkasta
     * @throws IllegalArgumentException dersom det nye underlaget inneheld gap mellom ei eller fleire av
     *                                  underlagsperiodene
     */
    public Underlag restrict(final Predicate<Underlagsperiode> predikat) {
        return annotasjonar.annoter(new Underlag(perioder.stream().filter(predikat)));
    }

    /**
     * Returnerer kronologisk siste periode fra underlaget, eller ein tom verdi dersom underlaget ikkje inneheld
     * nokon perioder.
     *
     * @return kronologisk siste underlagsperiode fr� underlaget, eller ein tom verdi om underlaget ikkje inneheld
     * nokon perioder
     */
    public Optional<Underlagsperiode> last() {
        return perioder.stream().reduce((a, b) -> b);
    }

    @Override
    public <T> T annotasjonFor(final Class<T> type) throws PaakrevdAnnotasjonManglarException {
        return annotasjonar
                .lookup(type)
                .orElseThrow(() -> new PaakrevdAnnotasjonManglarException(this, type));
    }

    @Override
    public <T> Optional<T> valgfriAnnotasjonFor(final Class<T> type) {
        return annotasjonar.lookup(type);
    }

    @Override
    public <T> Underlag annoter(final Class<? extends T> type, final T verdi) {
        annotasjonar.registrer(type, verdi);
        return this;
    }

    @Override
    public Underlag annoterFra(final Underlag kilde) {
        annotasjonar.addAll(kilde.annotasjonar);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "U" + perioder;
    }

    private void detekterTidsgapMellomPerioder() {
        final DetekterTidsgapMellomPerioder validator = new DetekterTidsgapMellomPerioder();
        perioder.stream().reduce(validator);
        if (validator.harTidsgap()) {
            throw new IllegalArgumentException(
                    feilmeldingVedTidsgapIUnderlaget(
                            "Eit underlag kan ikkje inneholde tidsgap mellom underlagsperiodene",
                            validator
                    )
            );
        }
    }

    private void detekterOverlappandePerioder() {
        final DetekterOverlappandePerioder detektor = new DetekterOverlappandePerioder();
        this.perioder.stream().reduce(detektor);
        if (detektor.harOverlappande()) {
            throw new IllegalArgumentException(
                    feilmeldingVedOverlappandeTidsperioder(
                            "Eit underlag kan ikkje inneholde underlagsperioder som overlappar kvarandre",
                            detektor
                    )
            );
        }
    }
}
