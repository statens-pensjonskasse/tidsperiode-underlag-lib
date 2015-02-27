package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Assume;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link Observasjonsdato}.
 *
 * @author Tarjei Skorgenes
 */
@RunWith(Theories.class)
public class ObservasjonsdatoTest {
    @DataPoints
    public static Month[] months = Month.values();

    @DataPoints
    public static Year[] years = rangeClosed(1917, 2099)
            .mapToObj(Year::of)
            .toArray(Year[]::new);

    /**
     * Verifiserer at observasjonsdatoen tilhøyrer rett årstall.
     */
    @Theory
    public void skalAlltidTilhoeyreAarstalletDatoenErOpprettaFor(final Year year, final Month month) {
        final Aarstall aar = new Aarstall(year.getValue());
        final Observasjonsdato dato = Observasjonsdato.forSisteDag(aar, month);
        assertTilhoeyrer(dato, aar).isTrue();
        assertTilhoeyrer(dato, aar.neste()).isFalse();
        assertTilhoeyrer(dato, aar.forrige()).isFalse();
    }

    /**
     * Verifiserer at observasjonsdatoen er 30. i månden for månedar som alltid er 31 dagar lange.
     *
     * @param year  årstallet observasjonsdatoen skal ligge innanfor
     * @param month månaden vi skal sjekke om observasjonsdatoen blir rett generert for
     */
    @Theory
    public void skalGenerereObservasjonsdatoLik31IMaanedarSomInneheld31Dagar(final Year year, final Month month) {
        Assume.assumeTrue(asList(JANUARY, MARCH, MAY, JULY, AUGUST, OCTOBER, DECEMBER).contains(month));

        assertObservasjonsdatoForSisteDag(year, month)
                .isEqualTo(new Observasjonsdato(LocalDate.of(year.getValue(), month, 31)));
    }

    /**
     * Verifiserer at observasjonsdatoen er 30. i månden for månedar som alltid er 30 dagar lange.
     *
     * @param year  årstallet observasjonsdatoen skal ligge innanfor
     * @param month månaden vi skal sjekke om observasjonsdatoen blir rett generert for
     */
    @Theory
    public void skalGenerereObservasjonsdatoLik30IMaanedarSomInneheld30Dagar(final Year year, final Month month) {
        Assume.assumeTrue(asList(APRIL, JUNE, SEPTEMBER, NOVEMBER).contains(month));

        assertObservasjonsdatoForSisteDag(year, month)
                .isEqualTo(new Observasjonsdato(LocalDate.of(year.getValue(), month, 30)));
    }

    /**
     * Verifiserer at observasjonsdatoen er 29. februar for alle skuddår.
     *
     * @param year  årstallet observasjonsdatoen skal ligge innanfor
     * @param month månaden vi skal sjekke om observasjonsdatoen blir rett generert for
     */
    @Theory
    public void skalGenerereObservasjonsdatoLik29FebruarISkuddaar(final Year year, final Month month) {
        Assume.assumeTrue(asList(FEBRUARY).contains(month));
        Assume.assumeTrue(year.isLeap());

        assertObservasjonsdatoForSisteDag(year, month)
                .isEqualTo(new Observasjonsdato(LocalDate.of(year.getValue(), Month.FEBRUARY, 29)));
    }

    /**
     * Verifiserer at observasjonsdatoen er 28. februar for alle år som ikkje er skuddår.
     *
     * @param year  årstallet observasjonsdatoen skal ligge innanfor
     * @param month månaden vi skal sjekke om observasjonsdatoen blir rett generert for
     */
    @Theory
    public void skalGenerereObservasjonsdatoLik28FebruarIAlleAarSomIkkjeErSkuddaar(final Year year, final Month month) {
        Assume.assumeTrue(asList(FEBRUARY).contains(month));
        Assume.assumeTrue(!year.isLeap());

        assertObservasjonsdatoForSisteDag(year, month)
                .isEqualTo(new Observasjonsdato(LocalDate.of(year.getValue(), Month.FEBRUARY, 28)));
    }

    private static AbstractBooleanAssert<?> assertTilhoeyrer(Observasjonsdato dato, Aarstall aar) {
        return assertThat(dato.tilhoeyrer(aar)).as("tilhøyrer " + dato + " år " + aar + "?");
    }

    private static AbstractObjectAssert<?, Observasjonsdato> assertObservasjonsdatoForSisteDag(Year year, Month month) {
        return assertThat(
                Observasjonsdato.forSisteDag(new Aarstall(year.getValue()), month)
        ).as("observasjonsdato for siste dag i " + month + " " + year);
    }
}