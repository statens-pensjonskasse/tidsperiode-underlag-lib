package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * Ein dekorator som transformerer eit observasjonsunderlag til ein observasjon pr avtale stillingsforholdet har vore aktiv på
 * innanfor observasjonsunderlaget.
 *
 * @author Tarjei Skorgenes
 */
class GenererObservasjonPrStillingsforholdOgAvtale implements Observasjonspublikator {
    private final Consumer<TidsserieObservasjon> consumer;

    /**
     * Konstruerer ein ny dekorator som vil videresende aggregerte observasjonar til <code>consumer</code>.
     *
     * @param consumer publikatoren som er ansvarlig for vidare prosessering og behandling av aggregerte observasjonar
     * @throws NullPointerException viss <code>consumer</code> er <code>null</code>
     */
    GenererObservasjonPrStillingsforholdOgAvtale(final Consumer<TidsserieObservasjon> consumer) {
        this.consumer = requireNonNull(consumer, "consumer er påkrevd, men var null");
    }

    /**
     * Transformerer og aggregerer kvart observasjonsunderlag til ein observasjon pr avtale pr underlag.
     * <p>
     * Dei aggregerte observasjonane blir publisert vidare til dekoratørens consumer for vidare prosessering/lagring.
     *
     * @param observasjonsunderlag alle observasjonsunderlaga som blir generert for ein tidsserie
     */
    @Override
    public void publiser(final Stream<Underlag> observasjonsunderlag) {
        observasjonsunderlag
                .flatMap(this::genererObservasjonPrAvtale)
                .forEach(consumer::accept);
    }

    /**
     * Genererer ein ny observasjon pr avtale som er stillingsforholdet er aktivt på i løpet av premieåret
     * som <code>observasjonsunderlag</code> inneheld underlagsperioder for.
     * <p>
     * For observasjonsunderlag der alle periodene er tilknytta ein go samme avtale, vil det kun bli returnert ein
     * observasjon.
     * <p>
     * For observasjonsunderlag der stillingsforholdet har vore gjennom eit eller fleire avtalebytte i løpet
     * av premieåret, vil det bli returnert ein observaasjon pr avtale som stillingsforholdet har vore
     * tilknytta i løpet av premieåret som observasjonsunderlaget representerer.
     *
     * @param observasjonsunderlag eit observasjonsunderlag som det skal genererast ein eller fleire
     *                             observasjonar av for tidsserien
     * @return ein straum med ein observasjon for stillingsforhold som ikkje har vore gjennom eit avtalebytte i
     * løpet av premieåret, eller ein straum med ein observasjon pr avtale stillingsforholdet har vore innom i
     * løpet av premieåret for stillingsforhold som har vore gjennom eit eller fleire avtalebytte
     */
    private Stream<TidsserieObservasjon> genererObservasjonPrAvtale(final Underlag observasjonsunderlag) {
        return observasjonsunderlag
                .stream()
                .map(observerPeriode(observasjonsunderlag))
                .collect(summerPrAvtale())
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Opprettar ein ny {@link Collector} som blir brukt for å gruppere og summere alle
     * {@link TidsserieObservasjon observasjonar} på periodenivå basert på kvar periodeobservasjon sin avtale.
     *
     * @return ein ny collector som grupperer periodeobservasjonane pr avtale og slår dei saman til ein observasjon som
     * inneheld avtalens totalresultat for heile premieåret
     * @see java.util.stream.Collectors#groupingBy(Function, Collector)
     * @see TidsserieObservasjon#plus(TidsserieObservasjon)
     */
    private static Collector<TidsserieObservasjon, ?, Map<AvtaleId, Optional<TidsserieObservasjon>>> summerPrAvtale() {
        return groupingBy(
                TidsserieObservasjon::avtale,
                reducing(TidsserieObservasjon::plus)
        );
    }

    /**
     * Genererer ein ny funksjon som genererer ein ny tidsserie-observasjon for ei underlagsperiode.
     * <p>
     * Observasjonane generert av denne metoda må seinare grupperast pr avtale og summerast saman
     * for å sitte igjen med ein total observasjon av avtalen for heile premieåret.
     * <p>
     * For underlag tilknytta stillingsforhold som har vore gjennom eit eller fleire avtalebytte,
     * vil grupperinga sikre at maskinelt grunnlag og dei andre målingane som inngår i tidsserien, blir summert
     * pr avtale slik at det blir generert eit innslag i tidsserien for kvar avtale stillingsforholdet har vore
     * aktivt på i løpet av kvart premieår.
     *
     * @param observasjonsunderlag observasjonsunderlaget som stillingsforhold og observasjonsdato blir henta frå
     * @return ein ny funksjon som vil generere ein tidsserieobservasjon pr underlagsperiode den blir kalla på
     */
    private static Function<Underlagsperiode, TidsserieObservasjon> observerPeriode(final Underlag observasjonsunderlag) {
        return p -> new TidsserieObservasjon(
                observasjonsunderlag.annotasjonFor(StillingsforholdId.class),
                p.annotasjonFor(AvtaleId.class),
                observasjonsunderlag.annotasjonFor(Observasjonsdato.class),
                observasjonsunderlag.valgfriAnnotasjonFor(Premiestatus.class)
        )
                .maskineltGrunnlag(p.beregn(MaskineltGrunnlagRegel.class))
                .aarsverk(p.beregn(AarsverkRegel.class));
    }
}
