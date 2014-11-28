package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at underlaget validerer og dermed garanterer, at ingen av underlagsperiodene overlappar nokon
     * av dei andre underlagsperiodene i underlaget.
     */
    @Test
    public void skalIkkjeKunneKonstruereUnderlagMedOverlappandeUnderlagsperioder() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Eit underlag kan ikkje inneholde underlagsperioder som overlappar kvarandre");
        e.expectMessage("2015-01-15->2015-12-31");
        e.expectMessage("2015-01-15->2015-01-31");
        e.expectMessage("2015-02-01->2015-02-28");
        create(
                periode().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.14")),
                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.01.31")),
                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.12.31")),
                periode().fraOgMed(dato("2015.02.01")).tilOgMed(dato("2015.02.28"))
        );
    }

    /**
     * Verifiserer at underlaget garanterer at underlagsperiodene er lagt inn i kronologisk rekkefølge.
     */
    @Test
    public void skalInneholdeUnderlagsperioderIKronologiskRekkefoelge() {
        final Underlagsperiode b = periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.08.14")).bygg();
        final Underlagsperiode c = periode().fraOgMed(dato("2000.08.15")).tilOgMed(dato("2000.12.31")).bygg();
        final Underlagsperiode a = periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.02.29")).bygg();
        final Underlag underlag = new Underlag(Stream.of(c, a, b));

        assertThat(underlag.toList().get(0)).isEqualTo(a);
        assertThat(underlag.toList().get(1)).isEqualTo(b);
        assertThat(underlag.toList().get(2)).isEqualTo(c);
    }

    /**
     * Verifiserer at konstruksjon av nye underlag feilar dersom det eksisterer tidsgap mellom ei eller fleire av
     * underlagsperiodene.
     */
    @Test
    public void skalIkkjeKunneKonstruereUnderlagMedTidsgapMellomUnderlagsperiodene() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("kan ikkje inneholde tidsgap");
        e.expectMessage("31 dagar tidsgap mellom");
        e.expectMessage("2000-01-01->2000-04-30");
        e.expectMessage("2000-06-01->2000-12-31");

        create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31"))
        );
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate) avgrensing} av nye underlag feilar dersom
     * det eksisterer tidsgap mellom ei eller fleire av underlagsperiodene etter at predikatet har filtrert bort
     * uønska underlagsperioder.
     */
    @Test
    public void skalIkkjeKunneAvgrenseUnderlagSlikAtDetOppstaarTidsgapMellomUnderlagsperiodene() {
        e.expect(IllegalArgumentException.class);

        final Underlag uavgrensa = create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")).med(new Integer(2)),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(new Integer(3)),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31")).med(new Integer(2))
        );
        uavgrensa.restrict(p -> p.annotasjonFor(Integer.class).intValue() == 2);
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate)} fjernar alle uønska underlagsperioder frå
     * det nye underlaget
     */
    @Test
    public void skalFjerneAlleUoenskaUnderlagsperioderVedAvgrensing() {
        final Underlag uavgrensa = create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")).med(new Integer(2)),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.12.31")).med(new Integer(3))
        );
        final Underlag avgrensa = uavgrensa.restrict(p -> p.annotasjonFor(Integer.class).intValue() == 2);
        assertThat(
                avgrensa
                        .stream()
                        .map(p -> p.valgfriAnnotasjonFor(Integer.class))
                        .map(o -> o.get())
                        .collect(toList())
        ).containsOnly(new Integer(2));
    }

    /**
     * Verifiserer at {@link Underlag#last()} returnerer den kronologisk siste underlagsperioda i underlaget.
     */
    @Test
    public void skalReturnereKronologiskSistePeriodeFraUnderlaget() {
        final Optional<Underlagsperiode> sistePeriode = create(
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.12.31")),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31"))
        ).last();
        assertThat(
                sistePeriode
                        .map(p -> p.fraOgMed())
        ).as("fra og med-dato for underlagsperiode " + sistePeriode)
                .isEqualTo(of(dato("2000.02.01")));
    }

    /**
     * Verifiserer at {@link Underlag#last()} returnerer ein tom verdi dersom underlaget er tomt.
     */
    @Test
    public void skalReturnereEmptyDersomUnderlagetIkkjeInneheldNokonUnderlagsperiode() {
        assertThat(new Underlag(Stream.empty()).last()).isEqualTo(Optional.empty());
    }

    private UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private Underlag create(UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map(b -> b.bygg()));
    }
}