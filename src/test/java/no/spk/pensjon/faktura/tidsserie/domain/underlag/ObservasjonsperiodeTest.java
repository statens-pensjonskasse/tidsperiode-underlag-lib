package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import static java.time.LocalDate.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.Datoar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.OptionalAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link Observasjonsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class ObservasjonsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenFraOgMedDato() {
        e.expect(NullPointerException.class);
        e.expectMessage("fra og med-dato er påkrevd");
        e.expectMessage("men var null");
        new Observasjonsperiode(null, now());
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenTilOgMedDato() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato er påkrevd");
        e.expectMessage("men var null");
        new Observasjonsperiode(now(), null);
    }

    @Test
    public void skalReturnereAarSomInneheld12MaanedarSjoelvOmObservasjonsperiodaKunDekkerDelarAvAaret() {
        final Observasjonsperiode periode = observasjonsperiode("2005.08.15", "2005.12.31");
        assertThat(periode.overlappendeAar()).hasSize(1);
        assertThat(
                periode
                        .overlappendeAar()
                        .stream()
                        .flatMap(aar -> aar.maaneder())
                        .collect(toList())
        ).hasSize(12);
    }

    @Test
    public void skalGenerereNyObservasjonsperiodeSomKunInneheldDaganeSomDeiToPeriodeneOverlappar() {
        final Observasjonsperiode observasjonsperiode = new Observasjonsperiode(
                new Aarstall(2000).atStartOfYear(),
                new Aarstall(2000).atEndOfYear()
        );

        assertIntersect(observasjonsperiode, "1999.01.01", of("1999.12.31")).isEqualTo(empty());
        assertIntersect(observasjonsperiode, "1999.01.01", empty()).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));

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
        assertIntersect(observasjonsperiode, "2000.01.01", empty()).isEqualTo(of(observasjonsperiode("2000.01.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.06.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.06.01")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.10.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.10.01")));

        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.12.30")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.30")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.06.01", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.06.01", empty()).isEqualTo(of(observasjonsperiode("2000.06.01", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2000.12.31", of("2000.12.31")).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.12.31", of("2001.01.01")).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));
        assertIntersect(observasjonsperiode, "2000.12.31", empty()).isEqualTo(of(observasjonsperiode("2000.12.31", "2000.12.31")));

        assertIntersect(observasjonsperiode, "2001.01.01", of("2001.12.31")).isEqualTo(empty());
        assertIntersect(observasjonsperiode, "2001.01.01", empty()).isEqualTo(empty());
    }

    private static OptionalAssert<Observasjonsperiode> assertIntersect(
            final Observasjonsperiode observasjonsperiode, final String fraOgMed, final Optional<String> tilOgMed) {
        final Tidsperiode<?> other = periode(dato(fraOgMed), tilOgMed.map(Datoar::dato));
        return assertThat(observasjonsperiode.intersect(other))
                .as("overlappande periode for " + observasjonsperiode + " og " + other);
    }

    private static Observasjonsperiode observasjonsperiode(final String fraOgMed, final String tilogMed) {
        return new Observasjonsperiode(dato(fraOgMed), dato(tilogMed));
    }

    private static Tidsperiode<?> periode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return new GenerellTidsperiode(fraOgMed, tilOgMed);
    }
}
