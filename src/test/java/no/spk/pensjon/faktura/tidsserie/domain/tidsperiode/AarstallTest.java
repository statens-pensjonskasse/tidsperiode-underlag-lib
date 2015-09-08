package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall}
 *
 * @author Tarjei Skorgenes
 */
@RunWith(Theories.class)
public class AarstallTest {
    @DataPoints
    public static Integer[] lotsOfYears = IntStream.rangeClosed(1, 2100).mapToObj(Integer::new).toArray(Integer[]::new);

    @Theory
    @Test
    public void skalVereLikOgHaSammeHashcodeSomAnnaInstansMedSammeVerdi(final Integer aar) {
        assertThat(new Aarstall(aar)).isEqualTo(new Aarstall(aar));
        assertThat(new Aarstall(aar).hashCode()).isEqualTo(new Aarstall(aar).hashCode());
    }

    @Theory
    @Test
    public void skalVereLikSegSjoelv(final Integer aarstall) {
        final Aarstall self = new Aarstall(aarstall);
        assertThat(self).isEqualTo(self);
        assertThat(self.hashCode()).isEqualTo(self.hashCode());
    }

    @Theory
    @Test
    public void skalVereLikAnnaInstansMedSammeVerdi(final Integer aarstall) {
        assertThat(new Aarstall(aarstall)).isEqualTo(new Aarstall(aarstall));
    }

    @Theory
    @Test
    public void skalVereUlikAlleMuligeAndreObjekt(final Integer aarstall) {
        final Aarstall verdi = new Aarstall(aarstall);
        assertThat(verdi).isNotEqualTo(null);
        assertThat(verdi).isNotEqualTo(new Object());
        assertThat(verdi).isNotEqualTo(new Aarstall(aarstall + 1));
        assertThat(verdi).isNotEqualTo(aarstall);
        assertThat(verdi).isNotEqualTo(new Aarstall(aarstall - 1));
    }

    @Theory
    @Test
    public void skalReturnereDatoLik1JanuarIAaret(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).atStartOfYear())
                .isEqualTo(
                        Year.of(aarstall)
                                .atDay(1)
                );
    }

    @Theory
    @Test
    public void skalReturnereDatoLik31DesemberIAaret(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).atEndOfYear())
                .isEqualTo(
                        Year.of(aarstall)
                                .atDay(1)
                                .with(lastDayOfYear())
                );
    }

    @Theory
    @Test
    public void skalInneholdeKorrektAntallDagar(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).lengde())
                .as("antall dagar i år " + aarstall)
                .isEqualTo(
                        new AntallDagar(
                                Year.of(aarstall).length())
                )
        ;
    }
}