package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * {@link UnderlagFactory} representerer algoritma
 * og datasettet som eit periodisert {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}
 * blir bygd opp av og fr�.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagFactory {
    private final ArrayList<Tidsperiode<?>> perioder = new ArrayList<>();
    private final Observasjonsperiode grenser;

    /**
     * Konstruerer ein ny instans som kan generere underlag som er avgrensa til � ligge innanfor observasjonsperioda.
     *
     * @param observasjonsperiode tidsperioda som avgrensasr underlagas fr� og med- og til og med-datoar
     * @throws NullPointerException dersom <code>observasjonsperiode</code> er <code>null</code>
     */
    public UnderlagFactory(final Observasjonsperiode observasjonsperiode) {
        requireNonNull(observasjonsperiode, () -> "observasjonsperiode er p�krevd, men var null");
        this.grenser = observasjonsperiode;
    }

    /**
     * @see #addPerioder(java.util.stream.Stream)
     */
    public UnderlagFactory addPerioder(final Tidsperiode<?>... perioder) {
        return addPerioder(asList(perioder));
    }

    /**
     * @see #addPerioder(java.util.stream.Stream)
     */
    public UnderlagFactory addPerioder(final Iterable<? extends Tidsperiode<?>> perioder) {
        return addPerioder(stream(perioder.spliterator(), false));
    }

    /**
     * Legger til periodene som input-data til periodiseringa som {@link #periodiser()} utf�rer n�r eit
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} blir bygd opp.
     *
     * @param perioder input-perioder som skal leggast til for seinare � brukast ved periodisering av og konstruksjon av nye underlag
     * @return <code>this</code>
     */
    public UnderlagFactory addPerioder(Stream<? extends Tidsperiode<?>> perioder) {
        perioder.collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
        return this;
    }

    /**
     * Konstruerer eit nytt underlag, populert med underlagsperioder mellom alle datoar der input periodene
     * endrar tilstand.
     *
     * @return eit nytt underlag med underlagsperioder i kronologisk rekkef�lge mellom alle endringsdatoar i
     * input-periodene som ligg fr� og med observasjonsperiodas start og slutt
     * @throws java.lang.IllegalStateException dersom ingen input-perioder har blitt lagt til p� factoryen f�r denne
     *                                         metoda blir kalla
     * @see #addPerioder(java.util.stream.Stream)
     */
    public Underlag periodiser() {
        if (perioder.isEmpty()) {
            throw new IllegalStateException(
                    "Periodisering av underlag krever minst ei tidsperiode som input, " +
                            "men fabrikken er satt opp uten nokon tidsperioder."
            );
        }
        final List<Tidsperiode<?>> input = finnObserverbarePerioder();
        return kobleTilOverlappandeTidsperioder(
                new Underlag(
                        byggUnderlagsperioder(alleDatoerUnderlagesPerioderSkalSplittesPaa(input))
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
        underlag.stream().forEach(underlagsperiode -> {
            for (final Tidsperiode<?> periode : perioder) {
                if (periode.overlapper(underlagsperiode)) {
                    underlagsperiode.kobleTil(periode);
                }
            }
        });
        return underlag;
    }

    /**
     * Bygger opp ein kronologisk sortert straum av underlagsperioder for endringsdatoane.
     * <p>
     * For kvar dato i <code>endringsdatoer</code> blir det generert ei ny underlagsperiode som har den aktuelle
     * datoen som sin fr� og med-dato og neste endringsdato, minus 1 dag, som sin til og med-dato.
     * <p>
     * Siste endringsdato m� derfor vere dagen etter at siste underlagsperiode skal bli avslutta for � sikre at
     * periodiseringa blir som forventa.
     *
     * @param endringsdatoer ei kronologisk sortert samling datoar der det skal starte ei ny underlagsperiode
     * @return ein kronologisk sortert straum av underlagsperioder
     */
    private Stream<Underlagsperiode> byggUnderlagsperioder(final SortedSet<LocalDate> endringsdatoer) {
        final ArrayList<Underlagsperiode> nyePerioder = new ArrayList<>();
        Optional<LocalDate> fraOgMed = Optional.empty();
        for (final LocalDate nextDate : endringsdatoer) {
            fraOgMed.ifPresent(dato -> {
                nyePerioder.add(new Underlagsperiode(dato, nextDate.minusDays(1)));
            });
            fraOgMed = of(nextDate);
        }
        return nyePerioder.stream();
    }

    /**
     * Hentar ut alle datoar som underlagets underlagsperioder skal splittast p� fordi ei eller fleire av tidsperiodene
     * i <code>input</code> har sin fr� og med- eller til og med-dato p� den aktuelle dagen.
     * <h4>Split p� fr� og med-dato</h2>
     * <p>
     * N�r ein skal periodisere m� ein f�rst og fremst splitte p� alle tidsperioders fr� og med-dato sidan dette markerer
     * ein overgang fr� ei tilstand til ei anna.
     * <p>
     * <h4>Split p� til og med-dato</h4>
     * <p>
     * I tillegg m� ein spesialhandtere til og med-datoar, ein m� her splitte dagen _etter_ periodas til og med-dato
     * fordi det f�rst er d� det skjer ei tilstandsendring.
     * <p>
     * <h4>Eksempel</h4>
     * Knut startar i stilling 1. januar og sluttar i stillinga stilling 30. juni. Han tar deretter 2 m�nedar ubetalt
     * ferie f�r han startar i ny stilling 3. september.
     * <p>
     * Underlaget for Knut m� her splittast opp i 3 perioder, ei som g�r fr� 1. januar til 30. juni sidan han her er i
     * aktiv stilling og den endrar seg ikkje underveis (vi ser bort fr� alle andre typer periodiske endringar i dette
     * eksempelet). Underlaget m� deretter splittast 1. juli sidan det markerer starten p� ei underlagsperiode der Knut
     * ikkje er i aktiv stilling. Underlagsperioda strekker seg fr� 1. juli til 2. september. Siste underlagsperiode
     * startar 3. september og l�per no fram til underlaget si observsjonsperiodes til og med-dato sidan Knut no jobbar
     * i ei aktiv stilling utan nokon andre endringar i tilstand (s� vidt vi veit).
     * <p>
     * <h4>Avgrensing av datoar</h4>
     * <p>
     * Ved oppsplitting av underlaget s� er det ikkje �nskelig � ende opp med underlagsperioder som har enten fr� og med-
     * eller til og med-dato som ligg utanfor observasjonsperioda som underlaget skal avgrensast til.
     * <p>
     * Tilsvarande, l�pande/aktive tidsperioder (dvs utan til og med-dato) m� p� ein eller anna m�te avgrensast sidan eit
     * underlag ikkje skal kunne vere l�pande.
     * <p>
     * For � sikre desse betingelsane blir derfor alle datoar returnert av denne metoda avgrensa til � tidligast starte
     * samme dag som observsjonsperiodas fr� og med-dato.
     * <p>
     * Datoane blir og garantert � ikkje starte meir enn 1 dag etter observasjonsperiodas til og med-dato, at ein her
     * inkluderer datoar som faktisk ligg ein dag utanfor observasjonsperioda, skyldast at ein seinare i sj�lve
     * periodiseringa, skal trekke fr� ein dag n�r ein bygger opp underlagsperiodene ut fr� lista som vi her returnerer.
     * <p>
     * <h4>Rekkef�lge</h4>
     * <p>
     * Sidan behandlinga av tidsperiodene i <code>input</code> ikkje vil garantere at vi endar opp med ei fr� og med-
     * og til og med-datoar i kronologisk sortert rekkef�lge blir derfor lista vi returnerer sortert kronologisk (dvs
     * fr� minste dato til st�rste dato) f�r vi returnerer. Uten dette vil ein ikkje kunne konstruere eit
     * underlag sidan ein ikkje lenger har ein garanti for at ei underlagsperiodes fr� og med-dato alltid vil vere
     * mindre enn til og med-datoen for samme periode.
     *
     * @param input ei liste som inneheld alle tidsperioder som underlagets potensielt sett skal m�tte periodiserast fr�
     * @return ei kronologisk sortert samling av unike datoar som underlaget sine underlagsperioder skal splittast p�
     */
    private SortedSet<LocalDate> alleDatoerUnderlagesPerioderSkalSplittesPaa(final List<Tidsperiode<?>> input) {
        return Stream.of(
                input
                        .stream()
                        .map(Tidsperiode::fraOgMed)
                ,
                input.stream()
                        .map(Tidsperiode::tilOgMed)
                        .map(o -> o.orElse(grenser.tilOgMed().get()))
                        .map(UnderlagFactory::nesteDag)
        )
                .flatMap(perioder -> perioder)
                .map(this::avgrensTilNedreGrense)
                .map(this::avgrensTilOevreGrense)
                .distinct()
                .sorted(LocalDate::compareTo)
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
    }

    /**
     * Avgrensar <code>dato</code> til � ligge innanfor observasjonsperioda til og med-dato.
     * <p>
     * Intensjonen her er � sikre at underlaget ikkje blir periodisert p� ein slik m�te at ei eller fleire av
     * underlagsperiodene blir liggande utanfor observasjonsperioda, sidan den representerer ei hard,
     * ytre begrensing for f�rste fra og med- og siste til og med-dato til underlagperiodene til underlaget.
     * <p>
     * Dersom <code>dato</code> ligg utanfor observasjonsperioda blir den sett bort fr� og dagen etter
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
     * Avgrensar <code>dato</code> til � ligge innanfor observasjonsperioda.
     * <p>
     * Intensjonen her er � sikre at underlaget ikkje blir periodisert p� ein slik m�te at ei eller fleire av
     * underlagsperiodene har ein fr� og med-dato som ligg utanfor observasjonsperioda, sidan den representerer ei hard,
     * ytre begrensing for f�rste fra og med- og siste til og med-dato til underlagperiodene til underlaget.
     *
     * @param dato ein dato som muligens ligg utanfor observasjonsperioda
     * @return returnerer <code>dato</code> viss <code>dato</code> overlappar observasjonsperioda,
     * ellers blir observasjonsperiodas fra og med-dato returnert
     */

    private LocalDate avgrensTilNedreGrense(final LocalDate dato) {
        return dato.isBefore(grenser.fraOgMed()) ? grenser.fraOgMed() : dato;
    }

    /**
     * Filtrerer vekk alle input-perioder som ikkje overlappar observasjonsperioda.
     * <p>
     * Intensjonen her er � hindre at underlaget kan bli p�virka og periodisert basert p� data fr� perioder som ein
     * ikkje skal ta hensyn til.
     *
     * @return ei liste som kun inneheld perioder som overlappar observasjonsperioda
     */
    private List<Tidsperiode<?>> finnObserverbarePerioder() {
        return perioder
                .stream()
                .filter(p -> p.overlapper(grenser))
                .collect(toList());
    }

    /**
     * Returnerer dagen etter <code>dato</code>.
     *
     * @param dato datoen som er ein dag f�r datoen som blir returnert
     * @return dagen etter <code>dato</code>
     * @see java.time.LocalDate#plusDays(long)
     */
    public static LocalDate nesteDag(final LocalDate dato) {
        return dato.plusDays(1);
    }
}
