package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link GenerellTidsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class GenerellTidsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    protected BiFunction<LocalDate, Optional<LocalDate>, Tidsperiode> factory;

    public GenerellTidsperiodeTest() {
        this.factory = (fraOgMed, tilOgMed) -> new GenerellTidsperiode(fraOgMed, tilOgMed);
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenFraOgMedDato() {
        e.expect(NullPointerException.class);
        e.expectMessage("fra og med-dato er påkrevd");
        e.expectMessage("men var null");
        create(null, of(now()));
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtenTilOgMedDato() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato er påkrevd");
        e.expectMessage("men var null");

        create(now(), null);
    }

    @Test
    public void skalIkkjeKunneOpprettUnderlagsperiodeMedFraOgMedDatoEtterTilOgMedDato() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("fra og med-dato kan ikkje vere etter til og med-dato");
        e.expectMessage("2005-12-30 er etter 2005-01-01");

        create("2005.12.30", "2005.01.01");
    }

    @Test
    public void skalIndikereAtOverlappandePerioderOverlappar() {
        assertOverlapper(
                create("2001.01.01", "2001.12.31"),
                create("2001.01.01", "2001.01.01")
        ).isTrue();

    }

    @Test
    public void skalOverlapperPerioderSomOverlapparITid() {
        Tidsperiode primaer = create("2001.01.01", "2001.12.31");
        List<Tidsperiode> skalOverlappe = asList(
                create("2000.01.01", "2001.06.30"),
                create("2001.02.01", "2001.06.30"),
                create("2001.07.01", "2002.12.31"),
                create("2000.01.01", null),
                create("2001.01.01", null),
                create("2001.12.31", null)
        );

        skalOverlappe.forEach(p -> assertOverlapper(p, primaer).isTrue());
        skalOverlappe.forEach(p -> assertOverlapper(primaer, p).isTrue());
    }

    @Test
    public void skalIkkjeOverlapperPerioderSomIkkjeOverlapparITid() {
        Tidsperiode primaer = create("2001.01.01", "2001.12.31");
        List<Tidsperiode> skalIkkjeOverlappe = asList(
                create("2000.01.01", "2000.12.31"),
                create("2002.01.01", "2002.12.31"),
                create("2002.01.01", null)
        );

        skalIkkjeOverlappe.forEach(p -> assertOverlapper(p, primaer).isFalse());
        skalIkkjeOverlappe.forEach(p -> assertOverlapper(primaer, p).isFalse());
    }

    public AbstractBooleanAssert<?> assertOverlapper(Tidsperiode a, Tidsperiode b) {
        return assertThat(a.overlapper(b)).as("overlapper " + a + " med " + b + "?");
    }

    protected Tidsperiode create(final String fraOgMed, final String tilOgMed) {
        return factory.apply(dato(fraOgMed), ofNullable(dato(tilOgMed)));
    }

    protected Tidsperiode create(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return factory.apply(fraOgMed, tilOgMed);
    }
}