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
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertFraOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link GenerellTidsperiode}.
 *
 * @author Tarjei Skorgenes
 */
@SuppressWarnings("rawtypes")
public class GenerellTidsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    protected BiFunction<LocalDate, Optional<LocalDate>, Tidsperiode> factory;

    public GenerellTidsperiodeTest() {
        this.factory = (fraOgMed, tilOgMed) -> new GenerellTidsperiode(fraOgMed, tilOgMed);
    }

    /**
     * Verifiserer at tidsperioder med samme frå og med- og til og med-dato er like
     */
    @Test
    public void skalVereLikeVissFraOgMedOgTilOgMedDatoHarLikVerdi() {
        assertThat(create(dato("1987.06.23"), empty()))
                .isEqualTo(create(dato("1987.06.23"), empty()));
        assertThat(create(dato("1987.06.23"), of(dato("2011.09.01"))))
                .isEqualTo(create(dato("1987.06.23"), of(dato("2011.09.01"))));
    }

    /**
     * Verifiserer at tidsperioder med forskjellig frå og med- eller til og med-dato er ulike
     */
    @Test
    public void skalVereULikeVissFraOgMedEllerTilOgMedDatoHarUlikVerdi() {
        // Frå og med ulik
        final LocalDate fraOgMed = dato("1987.06.23");
        final LocalDate tilOgMed = dato("2011.09.01");

        assertThat(create(fraOgMed, of(tilOgMed))).isNotEqualTo(create(fraOgMed.minusYears(2), of(tilOgMed)));
        assertThat(create(fraOgMed, empty())).isNotEqualTo(create(fraOgMed.plusDays(1), empty()));

        // Til og med ulik
        assertThat(create(fraOgMed, of(tilOgMed))).isNotEqualTo(create(fraOgMed, of(tilOgMed.minusMonths(3))));
        assertThat(create(fraOgMed, empty())).isNotEqualTo(create(fraOgMed, of(tilOgMed)));
        assertThat(create(fraOgMed, of(tilOgMed))).isNotEqualTo(create(fraOgMed, empty()));
    }

    /**
     * Verifiserer at hashcode på to perioder som er like, har samme verdi.
     */
    @Test
    public void skalHaLikHashCodeVissPeriodeneErLike() {
        assertThat(create("2000.01.01", "2012.04.30").hashCode())
                .isEqualTo(create("2000.01.01", "2012.04.30").hashCode());
    }

    /**
     * Verifiserer at frå og med- og til og med-dato blir kopiert frå andre tidsperioder.
     */
    @Test
    public void skalKopiereFraOgMedOgTilOgMedDatoFraAnnaTidsperiodeVedKonstruksjon() {
        final GenerellTidsperiode lukka = new GenerellTidsperiode(new GenerellTidsperiode(dato("2000.01.01"), of(dato("2012.12.15"))));
        assertFraOgMed(lukka).isEqualTo(dato("2000.01.01"));
        assertTilOgMed(lukka).isEqualTo(of(dato("2012.12.15")));

        final GenerellTidsperiode loepende = new GenerellTidsperiode(new GenerellTidsperiode(dato("1959.07.21"), empty()));
        assertFraOgMed(loepende).isEqualTo(dato("1959.07.21"));
        assertTilOgMed(loepende).isEqualTo(empty());
    }

    @Test
    public void skalIkkjeKunneKonstruerePeriodeUtanKilde() {
        e.expect(NullPointerException.class);
        e.expectMessage("kilde er påkrevd");
        e.expectMessage("men var null");
        new GenerellTidsperiode(null);
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

    public AbstractBooleanAssert<?> assertOverlapper(Tidsperiode<?> a, Tidsperiode<?> b) {
        return assertThat(a.overlapper(b)).as("overlapper " + a + " med " + b + "?");
    }

    protected Tidsperiode create(final String fraOgMed, final String tilOgMed) {
        return factory.apply(dato(fraOgMed), ofNullable(dato(tilOgMed)));
    }

    protected Tidsperiode create(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return factory.apply(fraOgMed, tilOgMed);
    }
}