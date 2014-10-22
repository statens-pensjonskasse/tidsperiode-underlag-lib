package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * {@link UnderlagFactory} representerer algoritma
 * og datasettet som eit periodisert {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}
 * blir bygd opp av og frå.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagFactory {
    private final ArrayList<StillingsforholdPeriode> perioder = new ArrayList<>();
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
     */
    public UnderlagFactory addPerioder(final StillingsforholdPeriode... perioder) {
        return addPerioder(asList(perioder));
    }

    /**
     * @see #addPerioder(java.util.stream.Stream)
     */
    public UnderlagFactory addPerioder(final Iterable<StillingsforholdPeriode> perioder) {
        return addPerioder(stream(perioder.spliterator(), false));
    }

    /**
     * Legger til periodene som input-data til periodiseringa som {@link #periodiser()} utfører når eit
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} blir bygd opp.
     *
     * @param perioder input-perioder som skal leggast til for seinare å brukast ved periodisering av og konstruksjon av nye underlag
     * @return <code>this</code>
     */
    public UnderlagFactory addPerioder(Stream<StillingsforholdPeriode> perioder) {
        perioder.collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
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
        if (perioder.isEmpty()) {
            throw new IllegalStateException(
                    "Periodisering av underlag krever minst ei tidsperiode som input, " +
                            "men fabrikken er satt opp uten nokon tidsperioder."
            );
        }
        final List<StillingsforholdPeriode> input = finnObserverbarePerioder();

        List<LocalDate> endringsdatoar = Stream.of(
                input
                        .stream()
                        .map(StillingsforholdPeriode::fraOgMed)
                ,
                input.stream()
                        .map(StillingsforholdPeriode::tilOgMed)
                        .map(o -> o.orElse(grenser.tilOgMed().get()))
                        .map(UnderlagFactory::nesteDag)
        )
                .flatMap(perioder -> perioder)
                .map(d -> d.isBefore(grenser.fraOgMed()) ? grenser.fraOgMed() : d)
                .map(d -> d.isAfter(grenser.tilOgMed().get()) ? nesteDag(grenser.tilOgMed().get()) : d)
                .distinct()
                .sorted(LocalDate::compareTo)
                .collect(toList());

        final ArrayList<Underlagsperiode> nyePerioder = new ArrayList<>();
        Optional<LocalDate> fraOgMed = Optional.empty();
        for (final LocalDate nextDate : endringsdatoar) {
            fraOgMed.ifPresent(dato -> {
                nyePerioder.add(new Underlagsperiode(dato, nextDate.minusDays(1)));
            });
            fraOgMed = of(nextDate);
        }
        return new Underlag(
                nyePerioder
                        .stream()
        );
    }

    /**
     * Filtrerer vekk alle input-perioder som ikkje overlappar observasjonsperioda.
     * <p>
     * Intensjonen her er å hindre at underlaget kan bli påvirka og periodisert basert på data frå perioder som ein
     * ikkje skal ta hensyn til.
     *
     * @return ei liste som kun inneheld perioder som overlappar observasjonsperioda
     */
    private List<StillingsforholdPeriode> finnObserverbarePerioder() {
        return perioder
                .stream()
                .filter(p -> p.overlapper(grenser))
                .collect(toList());
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
