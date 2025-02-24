package no.spk.premie.tidsperiode;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Enheitstestar for {@link Aarstall}
 *
 * @author Tarjei Skorgenes
 */
public class AarstallIT {
    public static Integer[] lotsOfYears = IntStream.rangeClosed(1, 2100).mapToObj(Integer::valueOf).toArray(Integer[]::new);

    @ParameterizedTest
    @MethodSource("range")
    void skalVereLikOgHaSammeHashcodeSomAnnaInstansMedSammeVerdi(final Integer aar) {
        assertThat(new Aarstall(aar)).isEqualTo(new Aarstall(aar));
        assertThat(new Aarstall(aar).hashCode()).isEqualTo(new Aarstall(aar).hashCode());
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalVereLikSegSjoelv(final Integer aarstall) {
        final Aarstall self = new Aarstall(aarstall);
        assertThat(self).isEqualTo(self);
        assertThat(self.hashCode()).isEqualTo(self.hashCode());
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalVereLikAnnaInstansMedSammeVerdi(final Integer aarstall) {
        assertThat(new Aarstall(aarstall)).isEqualTo(new Aarstall(aarstall));
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalVereUlikAlleMuligeAndreObjekt(final Integer aarstall) {
        final Aarstall verdi = new Aarstall(aarstall);
        assertThat(verdi).isNotEqualTo(null);
        assertThat(verdi).isNotEqualTo(new Object());
        assertThat(verdi).isNotEqualTo(new Aarstall(aarstall + 1));
        assertThat(verdi).isNotEqualTo(aarstall);
        assertThat(verdi).isNotEqualTo(new Aarstall(aarstall - 1));
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalReturnereDatoLik1JanuarIAaret(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).atStartOfYear())
                .isEqualTo(
                        Year.of(aarstall)
                                .atDay(1)
                );
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalReturnereDatoLik31DesemberIAaret(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).atEndOfYear())
                .isEqualTo(
                        Year.of(aarstall)
                                .atDay(1)
                                .with(lastDayOfYear())
                );
    }

    @ParameterizedTest
    @MethodSource("range")
    void skalInneholdeKorrektAntallDagar(final Integer aarstall) {
        assertThat(new Aarstall(aarstall).lengde())
                .as("antall dagar i år " + aarstall)
                .isEqualTo(
                        new AntallDagar(
                                Year.of(aarstall).length())
                )
        ;
    }
    static Stream<Integer> range() {
        return IntStream.range(1, 2100).boxed();
    }
}