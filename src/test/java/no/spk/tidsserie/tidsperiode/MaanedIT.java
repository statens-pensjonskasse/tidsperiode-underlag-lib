package no.spk.tidsserie.tidsperiode;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Month;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Enheitstestar for {@link Maaned}.
 *
 * @author Tarjei Skorgenes
 */
public class MaanedIT {

    private static Stream<Arguments> monthAndYears() {
        return Arrays.stream(Month.values()).map( month -> Arguments.of(new Aarstall(1997), month));
    }
    @Test
    void skalKreveAarstallVedKonstruksjon() {
        assertThatCode(
                () -> new Maaned(null, Month.AUGUST)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("årstall er påkrevd, men var null")
        ;
    }

    @Test
    void skalKreveMaanedVedKonstruksjon() {
        assertThatCode(
                () -> new Maaned(new Aarstall(1917), null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("måned er påkrevd, men var null")
        ;
    }

    /**
     * Verifiserer den overordna regelen om at månedens fra og med-dato for skal vere lik 1. dag i måneden.
     *
     * @param aar    årstallet som måneden vi skal sjekke skal vere tilknytta
     * @param maaned måned-i-året som måneden vi skal sjekke skal vere tilknytta
     */
    @ParameterizedTest
    @MethodSource("monthAndYears")
    void skalAlltidReturnereDenFoersteDagenIMaanedenSomFraOgMedDato(final Aarstall aar, final Month maaned) {
        assertThat(new Maaned(aar, maaned).fraOgMed())
                .as("fra og med-dato for " + maaned + " i " + aar)
                .isEqualTo(aar.atStartOfYear().withMonth(maaned.getValue()).with(firstDayOfMonth()));
    }

    /**
     * Verifiserer den overordna regelen om at månedens til og med-dato skal vere lik siste dag i måneden velger 29. februar
     * som siste dag i februar dersom året er eit skuddår.
     *
     * @param aar    årstallet som måneden vi skal sjekke skal vere tilknytta
     * @param maaned måned-i-året som måneden vi skal sjekke skal vere tilknytta
     */
    @ParameterizedTest
    @MethodSource("monthAndYears")
    void skalReturnere29FebruarSomTilOgMedDatoISkuddAar(final Aarstall aar, final Month maaned) {
        if (aar.toYear().isLeap() && maaned == Month.FEBRUARY) {
            assertThat(new Maaned(aar, maaned).tilOgMed().get())
                    .as("til og med-dato for " + maaned + " i " + aar)
                    .isEqualTo(aar.atStartOfYear().withMonth(2).withDayOfMonth(29));
        }
    }

    /**
     * Verifiserer den overordna regelen om at månedens til og med-dato for skal vere lik siste dag i måneden.
     *
     * @param aar    årstallet som måneden vi skal sjekke skal vere tilknytta
     * @param maaned måned-i-året som måneden vi skal sjekke skal vere tilknytta
     */
    @ParameterizedTest
    @MethodSource("monthAndYears")
    void skalReturnereSisteDagIMaanedenSomTilOgMedDato(final Aarstall aar, final Month maaned) {
        assertThat(new Maaned(aar, maaned).tilOgMed().get())
                .as("til og med-dato for " + maaned + " i " + aar)
                .isEqualTo(aar.atStartOfYear().withMonth(maaned.getValue()).with(lastDayOfMonth()));
    }
}