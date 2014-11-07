package no.spk.pensjon.faktura.tidsserie.domain;

import org.junit.Test;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.Aarstall}
 *
 * @author Tarjei Skorgenes
 */
public class AarstallTest {
    @Test
    public void skalReturnereDatoLik1JanuarIAaret() {
        assertThat(new Aarstall(2002).atStartOfYear()).isEqualTo(dato("2002.01.01"));
    }

    @Test
    public void skalReturnereDatoLik31DesemberIAaret() {
        assertThat(new Aarstall(1971).atEndOfYear()).isEqualTo(dato("1971.12.31"));
    }
}