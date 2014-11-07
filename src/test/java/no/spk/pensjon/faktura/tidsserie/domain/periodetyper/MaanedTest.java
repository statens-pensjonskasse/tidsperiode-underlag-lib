package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Maaned}.
 *
 * @author Tarjei Skorgenes
 */
@RunWith(Theories.class)
public class MaanedTest {
    @DataPoints
    public static Month[] months = Month.values();

    @DataPoints
    public static Aarstall[] years = IntStream.rangeClosed(1917, 2099).mapToObj(y -> new Aarstall(y)).collect(Collectors.toList()).toArray(new Aarstall[0]);

    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKreveAarstallVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("årstall er påkrevd, men var null");
        new Maaned(null, Month.AUGUST);
    }

    @Test
    public void skalKreveMaanedVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("måned er påkrevd, men var null");
        new Maaned(new Aarstall(1917), null);
    }

    /**
     * Verifiserer den overordna regelen om at månedens fra og med-dato for skal vere lik 1. dag i måneden.
     *
     * @param aar    årstallet som måneden vi skal sjekke skal vere tilknytta
     * @param maaned måned-i-året som måneden vi skal sjekke skal vere tilknytta
     */
    @Theory
    @Test
    public void skalAlltidReturnereDenFoersteDagenIMaanedenSomFraOgMedDato(final Aarstall aar, final Month maaned) {
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
    @Theory
    @Test
    public void skalReturnere29FebruarSomTilOgMedDatoISkuddAar(final Aarstall aar, final Month maaned) {
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
    @Theory
    @Test
    public void skalReturnereSisteDagIMaanedenSomTilOgMedDato(final Aarstall aar, final Month maaned) {
        assertThat(new Maaned(aar, maaned).tilOgMed().get())
                .as("til og med-dato for " + maaned + " i " + aar)
                .isEqualTo(aar.atStartOfYear().withMonth(maaned.getValue()).with(lastDayOfMonth()));
    }
}