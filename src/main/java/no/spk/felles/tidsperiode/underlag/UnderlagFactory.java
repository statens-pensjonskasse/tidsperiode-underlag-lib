package no.spk.felles.tidsperiode.underlag;

import no.spk.felles.tidsperiode.Tidsperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.time.LocalDate.MAX;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

/**
 * {@link UnderlagFactory} representerer algoritma
 * og datasettet som eit periodisert {@link Underlag}
 * blir bygd opp av og frå.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagFactory {
    private final ArrayList<Tidsperiode<?>> perioder = new ArrayList<>();
    private final Observasjonsperiode grenser;

    /**
     * Konstruerer ein ny instans som kan generere underlag som er avgrensa til å ligge innanfor observasjonsperioda.
     *
     * @param observasjonsperiode tidsperioda som avgrensasr underlagas frå og med- og til og med-datoar
     * @throws NullPointerException dersom <code>observasjonsperiode</code> er <code>null</code>
     */
    public UnderlagFactory(final Observasjonsperiode observasjonsperiode) {
        requireNonNull(observasjonsperiode, () -> "observasjonsperiode er påkrevd, men var null");
        this.grenser = observasjonsperiode;
    }

    /**
     * @see #addPerioder(java.util.stream.Stream)
     * @param perioder som skal legges til underlaget
     * @return dette UnderlagFactory for chaining
     */
    public UnderlagFactory addPerioder(final Tidsperiode<?>... perioder) {
        return addPerioder(asList(perioder));
    }

    /**
     * @see #addPerioder(java.util.stream.Stream)
     * @param perioder som skal legges til underlaget
     * @return dette UnderlagFactory for chaining
     */
    public UnderlagFactory addPerioder(final Iterable<? extends Tidsperiode<?>> perioder) {
        return addPerioder(stream(perioder.spliterator(), false));
    }

    /**
     * Legger til periodene som input-data til periodiseringa som {@link #periodiser()} utfører når eit
     * {@link Underlag} blir bygd opp.
     *
     * @param perioder input-perioder som skal leggast til for seinare å brukast ved periodisering av og konstruksjon av nye underlag
     * @return <code>this</code>
     */
    public UnderlagFactory addPerioder(Stream<? extends Tidsperiode<?>> perioder) {
        perioder
                .filter(grenser::overlapper)
                .collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
        return this;
    }

    /**
     * Konstruerer eit nytt underlag, populert med underlagsperioder mellom alle datoar der input periodene
     * endrar tilstand.
     *
     * @return eit nytt underlag med underlagsperioder i kronologisk rekkefølge mellom alle endringsdatoar i
     * input-periodene som ligg frå og med observasjonsperiodas start og slutt
     * @throws java.lang.IllegalStateException dersom ingen input-perioder har blitt lagt til på factoryen før denne
     *                                         metoda blir kalla
     * @see #addPerioder(java.util.stream.Stream)
     */
    public Underlag periodiser() {
        perioder.sort(Comparator.comparing(Tidsperiode::fraOgMed));
        return kobleTilOverlappandeTidsperioder(
                new Underlag(
                        byggUnderlagsperioder(alleDatoerUnderlagesPerioderSkalSplittesPaa(perioder))
                )
        );
    }

    /**
     * Koblar gjennom kvar av underlagsperiodene i underlaget og koblar dei saman med alle tidsperioder
     * lagt til via ei av {@link #addPerioder(java.util.stream.Stream)}-metodene, som overlappar underlagsperioda.
     *
     * @param underlag underlaget som inneheld underlagsperiodene som skal koblast til tidsperiodene som vart
     *                 brukt ved periodiseringa av underlaget
     * @return <code>underlag</code>
     */
    private Underlag kobleTilOverlappandeTidsperioder(final Underlag underlag) {
        for (final Underlagsperiode underlagsperiode : underlag.toList()) {
            for (final Tidsperiode<?> periode : this.perioder) {
                if (periode.tilOgMed().orElse(MAX).isBefore(underlagsperiode.fraOgMed())) {
                    continue;
                }
                if (periode.fraOgMed().isAfter(underlagsperiode.tilOgMed().orElse(MAX))) {
                    break;
                }
                underlagsperiode.kobleTil(periode);
            }
        }
        return underlag;
    }

    /**
     * Bygger opp ein kronologisk sortert straum av underlagsperioder for endringsdatoane.
     * <p>
     * For kvar dato i <code>endringsdatoer</code> blir det generert ei ny underlagsperiode som har den aktuelle
     * datoen som sin frå og med-dato og neste endringsdato, minus 1 dag, som sin til og med-dato.
     * <p>
     * Siste endringsdato må derfor vere dagen etter at siste underlagsperiode skal bli avslutta for å sikre at
     * periodiseringa blir som forventa.
     *
     * @param endringsdatoer ei kronologisk sortert samling datoar der det skal starte ei ny underlagsperiode
     * @return ein kronologisk sortert straum av underlagsperioder
     */
    private Stream<Underlagsperiode> byggUnderlagsperioder(final SortedSet<LocalDate> endringsdatoer) {
        final ArrayList<Underlagsperiode> nyePerioder = new ArrayList<>();
        LocalDate fraOgMed = null;
        for (final LocalDate nextDate : endringsdatoer) {
            if (fraOgMed != null) {
                nyePerioder.add(new Underlagsperiode(fraOgMed, nextDate.minusDays(1)));
            }
            fraOgMed = nextDate;
        }
        return nyePerioder.stream();
    }

    /**
     * Hentar ut alle datoar som underlagets underlagsperioder skal splittast på fordi ei eller fleire av tidsperiodene
     * i <code>input</code> har sin frå og med- eller til og med-dato på den aktuelle dagen.
     * <h4>Split på frå og med-dato</h2>
     * <p>
     * Når ein skal periodisere må ein først og fremst splitte på alle tidsperioders frå og med-dato sidan dette markerer
     * ein overgang frå ei tilstand til ei anna.
     * <p>
     * <h4>Split på til og med-dato</h4>
     * <p>
     * I tillegg må ein spesialhandtere til og med-datoar, ein må her splitte dagen _etter_ periodas til og med-dato
     * fordi det først er då det skjer ei tilstandsendring.
     * <p>
     * <h4>Eksempel</h4>
     * Knut startar i stilling 1. januar og sluttar i stillinga stilling 30. juni. Han tar deretter 2 månedar ubetalt
     * ferie før han startar i ny stilling 3. september.
     * <p>
     * Underlaget for Knut må her splittast opp i 3 perioder, ei som går frå 1. januar til 30. juni sidan han her er i
     * aktiv stilling og den endrar seg ikkje underveis (vi ser bort frå alle andre typer periodiske endringar i dette
     * eksempelet). Underlaget må deretter splittast 1. juli sidan det markerer starten på ei underlagsperiode der Knut
     * ikkje er i aktiv stilling. Underlagsperioda strekker seg frå 1. juli til 2. september. Siste underlagsperiode
     * startar 3. september og løper no fram til underlaget si observsjonsperiodes til og med-dato sidan Knut no jobbar
     * i ei aktiv stilling utan nokon andre endringar i tilstand (så vidt vi veit).
     * <p>
     * <h4>Avgrensing av datoar</h4>
     * <p>
     * Ved oppsplitting av underlaget så er det ikkje ønskelig å ende opp med underlagsperioder som har enten frå og med-
     * eller til og med-dato som ligg utanfor observasjonsperioda som underlaget skal avgrensast til.
     * <p>
     * Tilsvarande, løpande/aktive tidsperioder (dvs utan til og med-dato) må på ein eller anna måte avgrensast sidan eit
     * underlag ikkje skal kunne vere løpande.
     * <p>
     * For å sikre desse betingelsane blir derfor alle datoar returnert av denne metoda avgrensa til å tidligast starte
     * samme dag som observsjonsperiodas frå og med-dato.
     * <p>
     * Datoane blir og garantert å ikkje starte meir enn 1 dag etter observasjonsperiodas til og med-dato, at ein her
     * inkluderer datoar som faktisk ligg ein dag utanfor observasjonsperioda, skyldast at ein seinare i sjølve
     * periodiseringa, skal trekke frå ein dag når ein bygger opp underlagsperiodene ut frå lista som vi her returnerer.
     * <p>
     * <h4>Rekkefølge</h4>
     * <p>
     * Sidan behandlinga av tidsperiodene i <code>input</code> ikkje vil garantere at vi endar opp med ei frå og med-
     * og til og med-datoar i kronologisk sortert rekkefølge blir derfor lista vi returnerer sortert kronologisk (dvs
     * frå minste dato til største dato) før vi returnerer. Uten dette vil ein ikkje kunne konstruere eit
     * underlag sidan ein ikkje lenger har ein garanti for at ei underlagsperiodes frå og med-dato alltid vil vere
     * mindre enn til og med-datoen for samme periode.
     *
     * @param input ei liste som inneheld alle tidsperioder som underlagets potensielt sett skal måtte periodiserast frå
     * @return ei kronologisk sortert samling av unike datoar som underlaget sine underlagsperioder skal splittast på
     */
    private SortedSet<LocalDate> alleDatoerUnderlagesPerioderSkalSplittesPaa(final List<Tidsperiode<?>> input) {
        return input.stream()
                .flatMap(p -> Stream.of(
                        p.fraOgMed(),
                        nesteDag(p.tilOgMed().orElse(grenser.tilOgMed().get()))
                ))
                .map(this::avgrensTilNedreGrense)
                .map(this::avgrensTilOevreGrense)
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
    }

    /**
     * Avgrensar <code>dato</code> til å ligge innanfor observasjonsperioda til og med-dato.
     * <p>
     * Intensjonen her er å sikre at underlaget ikkje blir periodisert på ein slik måte at ei eller fleire av
     * underlagsperiodene blir liggande utanfor observasjonsperioda, sidan den representerer ei hard,
     * ytre begrensing for første fra og med- og siste til og med-dato til underlagperiodene til underlaget.
     * <p>
     * Dersom <code>dato</code> ligg utanfor observasjonsperioda blir den sett bort frå og dagen etter
     * observasjonsperiodas siste dag, blir returnert.
     *
     * @param dato ein dato som muligens ligg etter observasjonsperiodas til og med-dato
     * @return returnerer <code>dato</code> viss <code>dato</code> overlappar observasjonsperioda,
     * ellers blir dagen etter observasjonsperiodas til og med-dato returnert
     */
    private LocalDate avgrensTilOevreGrense(final LocalDate dato) {
        return dato.isAfter(grenser.tilOgMed().get()) ? nesteDag(grenser.tilOgMed().get()) : dato;
    }

    /**
     * Avgrensar <code>dato</code> til å ligge innanfor observasjonsperioda.
     * <p>
     * Intensjonen her er å sikre at underlaget ikkje blir periodisert på ein slik måte at ei eller fleire av
     * underlagsperiodene har ein frå og med-dato som ligg utanfor observasjonsperioda, sidan den representerer ei hard,
     * ytre begrensing for første fra og med- og siste til og med-dato til underlagperiodene til underlaget.
     *
     * @param dato ein dato som muligens ligg utanfor observasjonsperioda
     * @return returnerer <code>dato</code> viss <code>dato</code> overlappar observasjonsperioda,
     * ellers blir observasjonsperiodas fra og med-dato returnert
     */

    private LocalDate avgrensTilNedreGrense(final LocalDate dato) {
        return dato.isBefore(grenser.fraOgMed()) ? grenser.fraOgMed() : dato;
    }

    /**
     * Returnerer dagen etter <code>dato</code>.
     *
     * @param dato datoen som er ein dag før datoen som blir returnert
     * @return dagen etter <code>dato</code>
     * @see java.time.LocalDate#plusDays(long)
     */
    public static LocalDate nesteDag(final LocalDate dato) {
        return dato.plusDays(1);
    }
}
