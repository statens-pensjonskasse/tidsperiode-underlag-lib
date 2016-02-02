package no.spk.pensjon.faktura.tidsserie.domain.avregning;


import static java.util.stream.DoubleStream.iterate;
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
public class GrunnlagForGRUTest {

    private final static Prosent PROSENT_100 = prosent("100%");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @DataPoints("Prosent fra -1% til 200% bortsett fra 0% og 100%")
    public static Prosent[] ulovligeAndeler = iterate(-1, i -> i + 0.01)
            .mapToObj(Prosent::new)
            .filter(d -> !d.equals(ZERO, 2))
            .filter(d -> !d.equals(PROSENT_100, 2))
            .limit(20000)
            .toArray(Prosent[]::new);

    @Test
    public void skal_kreve_non_null_aarsfaktor() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("aarsfaktor for grunnlag for GRU kan ikke være null");
        new GrunnlagForGRU(null, faktureringsandel(ZERO));
    }

    @Test
    public void skal_kreve_non_null_faktureringsandel() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("faktureringsandel for grunnlag for GRU kan ikke være null");
        new GrunnlagForGRU(new Aarsfaktor(1), null);
    }

    @Test
    @Theory
    public void skal_feile_for_faktureringsandel_forskjellig_fra_0_og_100_prosent(Prosent faktureringsandel) throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("FaktureringsandelStatus#andel() må være 0% eller 100%, men var ");
        new GrunnlagForGRU(new Aarsfaktor(1), faktureringsandel(faktureringsandel));
    }

    @Test
    public void skal_lage_grunnlag_med_0_prosent_faktureringsandel() throws Exception {
        new GrunnlagForGRU(new Aarsfaktor(1), faktureringsandel(ZERO));
    }

    @Test
    public void skal_lage_grunnlag_med_100_prosent_faktureringsandel() throws Exception {
        new GrunnlagForGRU(new Aarsfaktor(1), faktureringsandel(PROSENT_100));
    }

    private FaktureringsandelStatus faktureringsandel(Prosent prosent) {
        return new FaktureringsandelStatus(StillingsforholdId.valueOf(1L), prosent);
    }
}