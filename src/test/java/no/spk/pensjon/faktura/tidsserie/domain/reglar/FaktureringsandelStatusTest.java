package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Snorre E. Brekke - Computas
 */
public class FaktureringsandelStatusTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void skal_ikke_tillate_navgativ_andel() throws Exception {
        expectNegativAndelException("-1%");
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("-1%"));
    }

    @Test
    public void skal_ikke_tillate_negativ_andel_2() throws Exception {
        expectNegativAndelException("-0,001%");
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("-0.001%"));
    }

    @Test
    public void skal_ikke_tillate_andel_storre_enn_100_prosent() throws Exception {
        expectOverMaksAndelException("101%");
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("101%"));
    }

    @Test
    public void skal_ikke_tillate_andel_storre_enn_100_prosent_2() throws Exception {
        expectOverMaksAndelException("100,001%");
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("100.001%"));
    }

    @Test
    public void skal_tillate_minus_null_prosent() throws Exception {
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("-0.000%"));
    }

    @Test
    public void skal_tillate_null_prosent() throws Exception {
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("0%"));
    }

    @Test
    public void skal_tillate_100_prosent() throws Exception {
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("100%"));
    }

    @Test
    public void skal_tillate_100_prosent_v2() throws Exception {
        new FaktureringsandelStatus(stillingsforhold(1L), prosent("100.000000%"));
    }

    private void expectNegativAndelException(String expectedErrorAndel) {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Andel kan ikke være mindre enn 0% men var " + expectedErrorAndel);
    }

    private void expectOverMaksAndelException(String expectedErrorAndel) {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Andel kan ikke større enn 100% men var " + expectedErrorAndel);
    }
}