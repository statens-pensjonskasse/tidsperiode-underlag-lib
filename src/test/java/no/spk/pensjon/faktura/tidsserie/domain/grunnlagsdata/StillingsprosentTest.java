package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsprosentTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalFeileVedNegativStillingsprosent() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("stillingsprosent må vere positiv");
        e.expectMessage("men var");
        e.expectMessage("-20%");

        new Stillingsprosent(new Prosent("-20%"));
    }

    @Test
    public void skalKreveProsentVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("prosent er påkrevd, men var null");

        new Stillingsprosent(null);
    }

    @Test
    public void skalAvrundastTilToDesimalarFoerSamanlikning() {
        assertThat(new Stillingsprosent(new Prosent("57.125%")))
                .isEqualTo(new Stillingsprosent(new Prosent("57.129%")))
                .isEqualTo(new Stillingsprosent(new Prosent("57.13%")));
        assertThat(new Stillingsprosent(new Prosent("57.124%")))
                .isNotEqualTo(new Stillingsprosent(new Prosent("57.129%")))
                .isEqualTo(new Stillingsprosent(new Prosent("57.12%")));
    }

    @Test
    public void skalVereLikAndreStillingsprosentarMedSammeProsentverdi() {
        assertThat(new Stillingsprosent(new Prosent("57%"))).isEqualTo(new Stillingsprosent(new Prosent("57%")));
    }

    @Test
    public void skalVereUlikAndreStillingsprosentarMedForskjelligProsentverdi() {
        assertThat(new Stillingsprosent(new Prosent("57%"))).isNotEqualTo(new Stillingsprosent(new Prosent("75%")));
    }

    @Test
    public void skalVereLikSegSjoelv() {
        assertThat(new Stillingsprosent(new Prosent("57%"))).isEqualTo(new Stillingsprosent(new Prosent("57%")));
    }

    @Test
    public void skalVereUlikNull() {
        assertThat(new Stillingsprosent(new Prosent("57%"))).isNotEqualTo(null);
    }

    @Test
    public void skalVereUlikObjektAvAnnaType() {
        assertThat(new Stillingsprosent(new Prosent("57%"))).isNotEqualTo(new Prosent("57%"));
    }

    @Test
    public void skalGiSammeHashCodeForUlikeInstansarMedLikVerdi() {
        assertThat(new Stillingsprosent(new Prosent("57%")).hashCode()).isEqualTo(new Stillingsprosent(new Prosent("57%")).hashCode());
        assertThat(new Stillingsprosent(new Prosent("57.129%")).hashCode()).isEqualTo(new Stillingsprosent(new Prosent("57.134%")).hashCode());
    }
}