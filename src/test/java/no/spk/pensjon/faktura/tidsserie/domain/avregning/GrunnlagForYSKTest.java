package no.spk.pensjon.faktura.tidsserie.domain.avregning;


import static java.util.stream.DoubleStream.iterate;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * @author Snorre E. Brekke - Computas
 */
@RunWith(Theories.class)
public class GrunnlagForYSKTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @DataPoints("Prosent fra og med 0% til og med 100%")
    public static Prosent[] lovligeAndeler = concat(
            iterate(0, i -> i + 0.0001)
                    .mapToObj(Prosent::new)
                    .limit(10000),
            of(prosent("100%"))
    )
            .toArray(Prosent[]::new);

    @Test
    @SuppressWarnings("deprecation")
    public void skal_kreve_non_null_aarsfaktor() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("aarsfaktor for grunnlag for GRU kan ikke være null");
        new GrunnlagForYSK(null, faktureringsandel(ZERO));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void skal_kreve_non_null_faktureringsandel() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("faktureringsandel for grunnlag for GRU kan ikke være null");
        new GrunnlagForYSK(new Aarsfaktor(1), null);
    }

    @Test
    @Theory
    @SuppressWarnings("deprecation")
    public void skal_feile_for_faktureringsandel_mindre_enn_0_prosent() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("FaktureringsandelStatus#andel() kan ikke være mindre enn 0% eller større enn 100%, men var");
        new GrunnlagForYSK(new Aarsfaktor(1), faktureringsandel(prosent("-0.01%")));
    }

    @Test
    @Theory
    @SuppressWarnings("deprecation")
    public void skal_feile_for_faktureringsandel_stoerre_enn_100_prosent() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("FaktureringsandelStatus#andel() kan ikke være mindre enn 0% eller større enn 100%, men var");
        new GrunnlagForYSK(new Aarsfaktor(1), faktureringsandel(prosent("100.01%")));
    }

    @Test
    @Theory
    @SuppressWarnings("deprecation")
    public void skal_lage_grunnlag_for_ysk_med_faktureringsandel_mellom_0_og_100_prosent(Prosent faktureringsandel) throws Exception {
        new GrunnlagForYSK(new Aarsfaktor(1), faktureringsandel(faktureringsandel));
    }


    private FaktureringsandelStatus faktureringsandel(Prosent prosent) {
        return new FaktureringsandelStatus(StillingsforholdId.valueOf(1L), prosent);
    }
}