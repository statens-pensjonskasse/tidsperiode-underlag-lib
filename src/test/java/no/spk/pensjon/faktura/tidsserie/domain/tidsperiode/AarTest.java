package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static java.time.LocalDate.ofYearDay;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar}.
 *
 * @author Tarjei Skorgenes
 */
@RunWith(Theories.class)
@SuppressWarnings("rawtypes")
public class AarTest {
    @DataPoints
    public static Aarstall[] years = IntStream.rangeClosed(1917, 2099).mapToObj(y -> new Aarstall(y)).collect(toList()).toArray(new Aarstall[0]);

    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at årstall er påkrevd ved konstruksjon av nye år.
     */
    @Test
    public void skalKreveAarstallVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("årstall er påkrevd, men var null");

        new Aar(null);
    }

    /**
     * Verifiserer at året overlappar alltid datoar som ligg innanfor året.
     */
    @Theory
    @Test
    public void skalAlltidOverlappeDatoarInnanforAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);
        assertThat(
                IntStream
                        .rangeClosed(1, aarstall.toYear().length())
                        .mapToObj(d -> ofYearDay(aarstall.toYear().getValue(), d))
                        .filter(d -> !aar.overlapper(d))
                        .toArray()
        ).as("datoar innanfor år " + aarstall + " som året ikke overlapper")
                .isEmpty();
    }

    /**
     * Verifiserer at året aldri overlappar datoar som ligg utanfor året.
     */
    @Theory
    @Test
    public void skalIkkjeOverlappeDatoarUtanforAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);
        final Aarstall forrige = aarstall.forrige();
        final Aarstall neste = aarstall.neste();
        assertThat(
                IntStream
                        .rangeClosed(1, 365)
                        .mapToObj(dagIAaret ->
                                        Stream.of(
                                                ofYearDay(forrige.toYear().getValue(), dagIAaret),
                                                ofYearDay(neste.toYear().getValue(), dagIAaret))
                        )
                        .flatMap(datoer -> datoer)
                        .filter(aar::overlapper)
                        .toArray()
        ).as("datoar utanfor år " + aarstall + " som året overlapper")
                .isEmpty();
    }

    /**
     * Verifiserer at året overlappar perioder som ligg 100% innanfor året.
     */
    @Theory
    @Test
    public void skalOverlappePerioderSomLiggHeiltInnanforAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);

        assertThat(
                IntStream
                        .rangeClosed(1, aarstall.toYear().length())
                        .mapToObj(d -> ofYearDay(aarstall.toYear().getValue(), d))
                        .map(d -> new GenerellTidsperiode(d, of(d)))
                        .filter(p -> !aar.overlapper(p))
                        .toArray()
        ).as("perioder innenfor år " + aarstall + " som året ikke overlapper")
                .isEmpty();

        assertThat(
                asList(
                        Month.values()
                )
                        .stream()
                        .map(m -> new Maaned(aarstall, m))
                        .filter(p -> !aar.overlapper(p))
                        .toArray()
        ).as("perioder innenfor år " + aarstall + " som året ikke overlapper")
                .isEmpty();

        assertThat(aar.overlapper(aar)).as("overlapper aaret seg selv?").isTrue();
    }

    /**
     * Verifiserer at året overlappar perioder som ligg delvis innanfor året.
     */
    @Theory
    @Test
    public void skalOverlappePerioderSomLiggDelvisInnanforStartenAvAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);

        final Aarstall forrige = aarstall.forrige();
        assertThat(
                IntStream
                        .rangeClosed(200, forrige.toYear().length())
                        .mapToObj(datoForrigeAar -> ofYearDay(forrige.toYear().getValue(), datoForrigeAar))
                        .map(datoForrigeAar -> new GenerellTidsperiode(datoForrigeAar, of(datoForrigeAar.plusDays(200))))
                        .filter(periode -> !aar.overlapper(periode))
                        .toArray()
        ).as("200 dagers perioder som startar før og sluttar i år " + aarstall + " men som året ikke overlapper")
                .isEmpty();

        assertThat(aar.overlapper(new GenerellTidsperiode(forrige.atEndOfYear(), of(forrige.atEndOfYear().plusDays(1)))))
                .as("overlapper " + aarstall + " 2-dagers perioda som starta nyttårsafta i fjor?")
                .isTrue();
    }

    /**
     * Verifiserer at året overlappar perioder som ligg delvis innanfor året.
     */
    @Theory
    @Test
    public void skalOverlappePerioderSomLiggDelvisInnanforSluttenAvAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);

        final Aarstall neste = aarstall.neste();
        assertThat(
                IntStream
                        .rangeClosed(1, 200)
                        .mapToObj(datoNesteAar -> ofYearDay(neste.toYear().getValue(), datoNesteAar))
                        .map(datoNesteAar -> new GenerellTidsperiode(datoNesteAar.minusDays(200), of(datoNesteAar)))
                        .filter(periode -> !aar.overlapper(periode))
                        .toArray()
        ).as("200 dagers perioder som startar i og sluttar etter år " + aarstall + " men som året ikke overlapper")
                .isEmpty();

        assertThat(aar.overlapper(new GenerellTidsperiode(aarstall.atEndOfYear(), of(neste.atStartOfYear()))))
                .as("overlapper år " + aar + " 2-dagers perioda som startar nyttårsafta i år?")
                .isTrue();
    }

    /**
     * Verifiserer at året aldri overlappar perioder som ligg utanfor året.
     */
    @Theory
    @Test
    public void skalAldriOverlappePerioderFørAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);

        final Aarstall forrige = aarstall.forrige();
        final LocalDate sisteDag = forrige.atEndOfYear();

        final Tidsperiode forrigeAar = new Aar(forrige);

        final GenerellTidsperiode forrigeMnd = new GenerellTidsperiode(
                sisteDag.with(firstDayOfMonth()),
                of(sisteDag)
        );

        final GenerellTidsperiode forrigeAarsSisteDag = new GenerellTidsperiode(
                sisteDag,
                of(sisteDag)
        );
        Stream.of(forrigeAar, forrigeMnd, forrigeAarsSisteDag).forEach(periode -> {
            assertOverlapper(aar, periode).isFalse();
        });
    }

    /**
     * Verifiserer at året aldri overlappar perioder som ligg utanfor året.
     */
    @Theory
    @Test
    public void skalAldriOverlappePerioderEtterAaret(final Aarstall aarstall) {
        final Aar aar = new Aar(aarstall);

        final Aarstall neste = aarstall.neste();
        final LocalDate foersteDag = neste.atStartOfYear();

        final Tidsperiode nesteAar = new Aar(neste);
        final Tidsperiode nesteMnd = new GenerellTidsperiode(
                foersteDag,
                of(foersteDag.with(lastDayOfMonth()))
        );

        final Tidsperiode nesteAarsFoersteDag = new GenerellTidsperiode(
                foersteDag,
                of(foersteDag)
        );
        Stream.of(nesteAar, nesteMnd, nesteAarsFoersteDag).forEach(periode -> {
            assertOverlapper(aar, periode).isFalse();
        });
    }

    /**
     * Verifiserer at årets fra og med-dato alltid er lik 1. januar.
     */
    @Theory
    @Test
    public void skalBruke1JanuarSomFraOgMedDatoKvartAar(final Aarstall aar) {
        assertThat(new Aar(aar).fraOgMed())
                .as("fra og med-dato for år " + aar)
                .isEqualTo(
                        LocalDate.of(aar.toYear().getValue(), JANUARY, 1)
                );
    }

    /**
     * Verifiserer at årets fra og med-dato alltid er lik 31. desember.
     */
    @Theory
    @Test
    public void skalBruke31DesemberSomTilOgMedDatoKvartAar(final Aarstall aar) {
        assertThat(new Aar(aar).tilOgMed())
                .as("til og med-dato for år " + aar)
                .isEqualTo(
                        of(
                                LocalDate.of(aar.toYear().getValue(), DECEMBER, 31)
                        )
                );
    }

    /**
     * Verifiserer at alle årstall skal inneholde 12 månedar.
     */
    @Theory
    @Test
    public void skalInneholde12Maanedar(final Aarstall aar) {
        assertAar(aar).hasSize(12);
    }

    private static AbstractListAssert<?, ? extends List<? extends Maaned>, Maaned> assertAar(final Aarstall aar) {
        return assertThat(new Aar(aar).maaneder().collect(toList())).as("måneder i år " + aar);
    }

    private static AbstractBooleanAssert<?> assertOverlapper(Aar aar, Tidsperiode periode) {
        return assertThat(aar.overlapper(periode)).as("overlapper " + aar + " og " + periode + "?");
    }
}