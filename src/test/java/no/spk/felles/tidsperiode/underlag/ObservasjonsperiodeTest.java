package no.spk.felles.tidsperiode.underlag;

import static java.time.LocalDate.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.felles.tidsperiode.Datoar;
import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.OptionalAssert;
import org.junit.Test;

/**
 * Enheitstestar for {@link Observasjonsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class ObservasjonsperiodeTest {
    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenFraOgMedDato() {
        assertThatCode(
                () -> new Observasjonsperiode(null, now())
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("fra og med-dato er påkrevd")
                .hasMessageContaining("men var null")
        ;
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenTilOgMedDato() {
        final LocalDate til_og_med = null;

        assertThatCode(
                () -> new Observasjonsperiode(now(), til_og_med)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("til og med-dato er påkrevd")
                .hasMessageContaining("men var null")
        ;
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenOptionalTilOgMedDato() {
        final Optional<LocalDate> til_og_med = null;

        assertThatCode(
                () -> new Observasjonsperiode(now(), til_og_med)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("til og med-dato er påkrevd")
                .hasMessageContaining("men var null")
        ;
    }

    @Test
    public void skalReturnereAarSomInneheld12MaanedarSjoelvOmObservasjonsperiodaKunDekkerDelarAvAaret() {
        final Observasjonsperiode periode = observasjonsperiode("2005.08.15", "2005.12.31");
        assertThat(periode.overlappendeAar()).hasSize(1);
        assertThat(
                periode
                        .overlappendeAar()
                        .stream()
        )
                .hasSize(1)
                .satisfies(
                        overlappendeÅr ->
                                assertThat(
                                        overlappendeÅr.stream()
                                )
                                        .allSatisfy(
                                                årstall -> assertThat(årstall.maaneder()).hasSize(12)
                                        )
                );
        ;
    }

    @Test
    public void skalGenerereNyObservasjonsperiodeSomKunInneheldDaganeSomDeiToPeriodeneOverlappar() {
        final Observasjonsperiode observasjonsperiode = observasjonsperiode(
                "2000.01.01",
                "2000.12.31"
        );

        assertIntersect(observasjonsperiode, "1999.01.01", of("1999.12.31")).isEqualTo(empty());
        assertIntersect(observasjonsperiode, "1999.01.01", løpende()).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "1999.01.01", of("2000.01.01")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.01.01")));
        assertIntersect(observasjonsperiode, "1999.01.01", of("2000.01.02")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.01.02")));
        assertIntersect(observasjonsperiode, "1999.01.01", of("2000.01.02")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.01.02")));

        assertIntersect(observasjonsperiode, "1999.01.01", of("2000.12.30")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.30")));
        assertIntersect(observasjonsperiode, "1999.01.01", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "1999.01.01", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2000.01.01", of("2000.01.01")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.01.01")));
        assertIntersect(observasjonsperiode, "2000.01.01", of("2000.01.02")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.01.02")));

        assertIntersect(observasjonsperiode, "2000.01.01", of("2000.12.30")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.30")));
        assertIntersect(observasjonsperiode, "2000.01.01", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.01.01", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.01.01", løpende()).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.06.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.06.01")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.10.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.10.01")));

        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.12.30")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.30")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.06.01", løpende()).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2000.12.31", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.12.31", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.12.31", empty()).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2001.01.01", of("2001.12.31")).isEqualTo(empty());
        assertIntersect(observasjonsperiode, "2001.01.01", løpende()).isEqualTo(empty());
    }

    @Test
    public void skalGenerereNyObservasjonsperiodeSomKunInneheldDaganeSomDeiToPeriodeneOverlapparForLøpendeObservasjonsperiode() {
        final Observasjonsperiode observasjonsperiode = observasjonsperiode(
                "2000.01.01",
                løpende()
        );

        assertIntersect(observasjonsperiode, "1999.01.01", of("1999.12.31")).isEqualTo(empty());
        assertIntersect(observasjonsperiode, "2001.01.01", of("2001.12.31")).isEqualTo(of(observasjonsperiode("2001.01.01", "2001.12.31")));
        assertIntersect(observasjonsperiode, "2001.01.01", løpende()).isEqualTo(of(observasjonsperiode("2001.01.01", løpende())));
        assertIntersect(observasjonsperiode, "1999.01.01", løpende()).isEqualTo(of(observasjonsperiode));
    }

    private static OptionalAssert<Observasjonsperiode> assertIntersect(
            final Observasjonsperiode observasjonsperiode,
            final String fraOgMed,
            final Optional<String> tilOgMed
    ) {
        final Tidsperiode<?> other = periode(dato(fraOgMed), tilOgMed.map(Datoar::dato));
        return assertThat(observasjonsperiode.intersect(other))
                .as("overlappande periode for " + observasjonsperiode + " og " + other);
    }

    private static Observasjonsperiode observasjonsperiode(final String fraOgMed, final String tilogMed) {
        return new Observasjonsperiode(dato(fraOgMed), dato(tilogMed));
    }

    private static Observasjonsperiode observasjonsperiode(final String fraOgMed, final Optional<String> tilogMed) {
        return new Observasjonsperiode(dato(fraOgMed), tilogMed.map(Datoar::dato));
    }

    private static Tidsperiode<?> periode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return new GenerellTidsperiode(fraOgMed, tilOgMed);
    }

    private Optional<String> løpende() {
        return Optional.empty();
    }
}
