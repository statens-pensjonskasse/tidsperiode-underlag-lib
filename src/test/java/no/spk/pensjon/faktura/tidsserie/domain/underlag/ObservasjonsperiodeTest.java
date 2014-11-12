package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode}.
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
        final Observasjonsperiode periode = new Observasjonsperiode(dato("2005.08.15"), dato("2005.12.31"));
        assertThat(periode.overlappendeAar()).hasSize(1);
        assertThat(
                periode
                        .overlappendeAar()
                        .stream()
                        .flatMap(aar -> aar.maaneder())
                        .collect(toList())
        ).hasSize(12);

    }
}
