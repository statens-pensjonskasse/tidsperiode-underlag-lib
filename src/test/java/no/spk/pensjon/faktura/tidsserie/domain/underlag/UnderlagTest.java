package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Arrays.asList;
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
     * Verifiserer at konstruksjon av nye underlag feilar dersom det eksisterer tidsgap mellom ei eller fleire av
     * underlagsperiodene.
     */
    @Test
    public void skalIkkjeKunneKonstruereUnderlagMedTidsgapMellomUnderlagsperiodene() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("skal ikkje kunne inneholde tidsgap");
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

    private UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private Underlag create(UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map(b -> b.bygg()));
    }
}