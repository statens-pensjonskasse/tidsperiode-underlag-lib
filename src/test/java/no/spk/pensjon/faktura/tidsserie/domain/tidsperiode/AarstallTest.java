package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void skalVereLikSegSjoelv() {
        final Aarstall self = new Aarstall(2010);
        assertThat(self).isEqualTo(self);
        assertThat(self.hashCode()).isEqualTo(self.hashCode());
    }

    @Test
    public void skalVereLikAnnaInstansMedSammeVerdi() {
        assertThat(new Aarstall(2010)).isEqualTo(new Aarstall(2010));
    }

    @Test
    public void skalVereUlikAlleMuligeAndreObjekt() {
        final Aarstall verdi = new Aarstall(2012);
        assertThat(verdi).isNotEqualTo(null);
        assertThat(verdi).isNotEqualTo(new Object());
        assertThat(verdi).isNotEqualTo(new Aarstall(2013));
        assertThat(verdi).isNotEqualTo(new Integer(2012));
        assertThat(verdi).isNotEqualTo(new Aarstall(2011));
    }

    @Test
    public void skalReturnereDatoLik1JanuarIAaret() {
        assertThat(new Aarstall(2002).atStartOfYear()).isEqualTo(dato("2002.01.01"));
    }

    @Test
    public void skalReturnereDatoLik31DesemberIAaret() {
        assertThat(new Aarstall(1971).atEndOfYear()).isEqualTo(dato("1971.12.31"));
    }
}