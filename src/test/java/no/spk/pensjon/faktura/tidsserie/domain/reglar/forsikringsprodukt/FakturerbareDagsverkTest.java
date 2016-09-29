package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class FakturerbareDagsverkTest {
    @Test
    public void skal_lage_fakturerbare_dagsverk_med_fem_desimaler_fra_double() throws Exception {
        final FakturerbareDagsverk dagsverk = new FakturerbareDagsverk(0.654321);
        assertVerdiOgToString(dagsverk, "0.65432");
    }

    @Test
    public void skal_lage_fakturerbare_dagsverk_med_fem_desimaler_fra_int() throws Exception {
        final FakturerbareDagsverk dagsverk = new FakturerbareDagsverk(1);
        assertVerdiOgToString(dagsverk, "1.00000");
    }

    @Test
    public void skal_lage_fakturerbare_dagsverk_med_fem_desimaler_fra_big_desimal() throws Exception {
        final FakturerbareDagsverk dagsverk = new FakturerbareDagsverk(new BigDecimal("0.654329"));
        assertVerdiOgToString(dagsverk, "0.65433");
    }

    @Test
    public void skal_lage_fakturerbare_dagsverk_med_fem_desimaler_fra_big_desimal_uten_desimaler() throws Exception {
        final FakturerbareDagsverk dagsverk = new FakturerbareDagsverk(new BigDecimal("1"));
        assertVerdiOgToString(dagsverk, "1.00000");
    }

    @Test
    public void skal_multiplisere_med_prosent_avrundet_til_fem_desimaler() throws Exception {
        final FakturerbareDagsverk dagsverk = new FakturerbareDagsverk(new BigDecimal("100")).multiply(Prosent.prosent("50.0002%"));
        assertVerdiOgToString(dagsverk, "50.00000");
    }

    private void assertVerdiOgToString(FakturerbareDagsverk dagsverk, String expected) {
        assertThat(dagsverk.verdi()).isEqualTo(expected);
        assertThat(dagsverk.toString()).isEqualTo(expected);
    }
}