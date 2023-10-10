package no.spk.felles.tidsperiode;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.spk.felles.tidsperiode.Assertions.assertFraOgMed;
import static no.spk.felles.tidsperiode.Assertions.assertTilOgMed;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

/**
 * Enheitstestar for {@link GenerellTidsperiode}.
 *
 * @author Tarjei Skorgenes
 */
@SuppressWarnings("rawtypes")
public class GenerellTidsperiodeTest {
    protected BiFunction<LocalDate, Optional<LocalDate>, Tidsperiode> factory;

    private final Comparator<Tidsperiode<?>> sortering = Tidsperiode::compare;

    public GenerellTidsperiodeTest() {
        this.factory = GenerellTidsperiode::new;
    }

    @Test
    void skalVereUlikeDersomFraaOgMedDatoaneErUlike() {
        assertThat(
                sortering.compare(
                        create("2004.01.01", "2005.01.02"),
                        create("2004.02.01", "2005.01.02")
                )
        ).isLessThan(0);

        assertThat(
                sortering.compare(
                        create("2004.02.01", "2005.01.02"),
                        create("2004.01.01", "2005.01.02")
                )
        ).isGreaterThan(0);
    }

    @Test
    void skalVereUlikeDersomFraaOgMedDatoaneErLikeMenTilOgMedDatoaneErUlike() {
        assertThat(
                sortering.compare(
                        create("2004.01.01", "2005.01.02"),
                        create("2004.01.01", "2005.02.02")
                )
        ).isLessThan(0);

        assertThat(
                sortering.compare(
                        create("2004.01.01", "2005.02.02"),
                        create("2004.01.01", "2005.01.02")
                )
        ).isGreaterThan(0);

        assertThat(
                sortering.compare(
                        create(dato("2004.01.01"), of(dato("2005.01.02"))),
                        create(dato("2004.01.01"), empty())
                )
        ).isLessThan(0);

        assertThat(
                sortering.compare(
                        create(dato("2004.01.01"), empty()),
                        create(dato("2004.01.01"), of(dato("2005.01.02")))
                )
        ).isGreaterThan(0);
    }

    @Test
    void skalVereLikeDersomBaadeFraaOgMedOgTilOgMedDatoaneErLike() {
        assertThat(
                sortering.compare(
                        create(dato("2004.01.01"), empty()),
                        create(dato("2004.01.01"), empty())
                )
        ).isEqualTo(0);

        assertThat(
                sortering.compare(
                        create("2004.01.01", "2005.01.01"),
                        create("2004.01.01", "2005.01.01")
                )
        ).isEqualTo(0);
    }

    /**
     * Verifiserer at tidsperioder med samme frå og med- og til og med-dato er like
     */
    @Test
    void skalVereLikeVissFraOgMedOgTilOgMedDatoHarLikVerdi() {
        assertThat(create(dato("1987.06.23"), empty()))
                .isEqualTo(create(dato("1987.06.23"), empty()));
        assertThat(create(dato("1987.06.23"), of(dato("2011.09.01"))))
                .isEqualTo(create(dato("1987.06.23"), of(dato("2011.09.01"))));
    }

    /**
     * Verifiserer at tidsperioder med forskjellig frå og med- eller til og med-dato er ulike
     */
    @Test
    void skalVereULikeVissFraOgMedEllerTilOgMedDatoHarUlikVerdi() {
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
    void skalHaLikHashCodeVissPeriodeneErLike() {
        assertThat(create("2000.01.01", "2012.04.30").hashCode())
                .isEqualTo(create("2000.01.01", "2012.04.30").hashCode());
    }

    /**
     * Verifiserer at frå og med- og til og med-dato blir kopiert frå andre tidsperioder.
     */
    @Test
    void skalKopiereFraOgMedOgTilOgMedDatoFraAnnaTidsperiodeVedKonstruksjon() {
        final GenerellTidsperiode lukka = new GenerellTidsperiode(new GenerellTidsperiode(dato("2000.01.01"), of(dato("2012.12.15"))));
        assertFraOgMed(lukka).isEqualTo(dato("2000.01.01"));
        assertTilOgMed(lukka).isEqualTo(of(dato("2012.12.15")));

        final GenerellTidsperiode loepende = new GenerellTidsperiode(new GenerellTidsperiode(dato("1959.07.21"), empty()));
        assertFraOgMed(loepende).isEqualTo(dato("1959.07.21"));
        assertTilOgMed(loepende).isEqualTo(empty());
    }

    @Test
    void skalIkkjeKunneKonstruerePeriodeUtanKilde() {
        assertThatCode(() -> new GenerellTidsperiode(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("kilde er påkrevd")
                .hasMessageContaining("men var null")
        ;
    }

    @Test
    void skalIkkjeKunneKonstruerePeriodeUtenFraOgMedDato() {
        assertThatCode(() -> create(null, of(now())))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("fra og med-dato er påkrevd")
                .hasMessageContaining("men var null")
        ;

    }

    @Test
    void skalIkkjeKunneKonstruerePeriodeUtenTilOgMedDato() {
        assertThatCode(() -> create(now(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("til og med-dato er påkrevd")
                .hasMessageContaining("men var null")
        ;
    }

    @Test
    void skalIkkjeKunneOpprettUnderlagsperiodeMedFraOgMedDatoEtterTilOgMedDato() {
        assertThatCode(() -> create("2005.12.30", "2005.01.01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fra og med-dato kan ikkje vere etter til og med-dato")
                .hasMessageContaining("2005-12-30 er etter 2005-01-01")
        ;
    }

    @Test
    void skalIndikereAtOverlappandePerioderOverlappar() {
        assertOverlapper(
                create("2001.01.01", "2001.12.31"),
                create("2001.01.01", "2001.01.01")
        )
                .isTrue();

    }

    @Test
    void skalOverlapperPerioderSomOverlapparITid() {
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
    void skalIkkjeOverlapperPerioderSomIkkjeOverlapparITid() {
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
        return factory.apply(dato(fraOgMed), ofNullable(tilOgMed).map(Datoar::dato));
    }

    protected Tidsperiode create(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return factory.apply(fraOgMed, tilOgMed);
    }
}