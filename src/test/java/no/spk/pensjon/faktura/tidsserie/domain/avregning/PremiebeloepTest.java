package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Assertions.assertDesimaler;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.GrunnlagForGRU.grunnlagForGRU;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.GrunnlagForPensjonsprodukt.grunnlagForPensjonsprodukt;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.GrunnlagForYSK.grunnlagForYSK;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;

import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

/**
 * Enhetstester for {@link Premiebeloep}.
 *
 * @author Tarjei Skorgenes
 */
public class PremiebeloepTest {
    /**
     * Verifiserer at prosentsatsane blir avrunda til 2 desimalar før dei blir multiplisert ut
     * med premiebeløpet.
     * <br>
     * Intensjonen med denne oppførselen er å sikre konsistent oppførsel uavhengig av om premiebeløpet ein
     * multipliserer med har ein stor eller lav verdi. Ved summering av multiplisering gjort mange gangar på eit
     * lavt premiebeløp og multiplisering 1 gang med ein stort premiebeløp som er lik summen av premiebeløpa ein
     * multipliserte manger gangar på, burde ein få likt resultat.
     */
    @Test
    public void skal_avrunde_prosentsats_til_2_desimaler_foer_multiplikasjon_med_grunnlag_for_pensjonsprodukt() {
        final GrunnlagForPensjonsprodukt grunnlag = new GrunnlagForPensjonsprodukt(new Kroner(100_000_000_000d));
        assertThat(premiebeloep(grunnlag, new Prosent("0.001%"))).isEqualTo(premiebeloep("kr 0"));
        assertThat(premiebeloep(grunnlag, new Prosent("0.0049%"))).isEqualTo(premiebeloep("kr 0"));
        assertThat(premiebeloep(grunnlag, new Prosent("0.005%"))).isEqualTo(premiebeloep("kr 10 000 000"));
        assertThat(premiebeloep(grunnlag, new Prosent("0.01%"))).isEqualTo(premiebeloep("kr 10 000 000"));
    }

    @Test
    public void skal_avrunde_premiebeloepet_etter_grunnlag_for_pensjonsprodukt_er_multiplisert_med_premiesats() {
        assertPremiebeloep(premiebeloep(grunnlagForPensjonsprodukt(10_000), prosent("0.35%")))
                .isEqualTo(premiebeloep("kr 35.00"));
        assertPremiebeloep(premiebeloep(grunnlagForPensjonsprodukt(1_000), prosent("0.35%")))
                .isEqualTo(premiebeloep("kr 3.50"));
        assertPremiebeloep(premiebeloep(grunnlagForPensjonsprodukt(100), prosent("0.35%")))
                .isEqualTo(premiebeloep("kr 0.35"));

        assertPremiebeloep(premiebeloep(grunnlagForPensjonsprodukt(10), prosent("0.35%")))
                .isEqualTo(premiebeloep("kr 0.04"));

        final GrunnlagForPensjonsprodukt grunnlag = grunnlagForPensjonsprodukt(1);
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5051%")))
                .isEqualTo(premiebeloep("kr 0.01"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.49%")))
                .isEqualTo(premiebeloep("kr 0.00"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.35%")))
                .isEqualTo(premiebeloep("kr 0.00"));
    }

    /**
     * Verifiserer at {@link java.math.RoundingMode#HALF_UP} blir benytta ved avrunding av premiebeløpa
     * sidan dette er den ordinære / normalt brukte avrundingsregelen for norske kroner.
     */
    @Test
    public void skal_avrunde_med_half_up() {
        final GrunnlagForPensjonsprodukt grunnlag = new GrunnlagForPensjonsprodukt(kroner(100));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5050%"))).isEqualTo(premiebeloep("kr 0.51"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5150%"))).isEqualTo(premiebeloep("kr 0.52"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5250%"))).isEqualTo(premiebeloep("kr 0.53"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5350%"))).isEqualTo(premiebeloep("kr 0.54"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5450%"))).isEqualTo(premiebeloep("kr 0.55"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5550%"))).isEqualTo(premiebeloep("kr 0.56"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5650%"))).isEqualTo(premiebeloep("kr 0.57"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5750%"))).isEqualTo(premiebeloep("kr 0.58"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5850%"))).isEqualTo(premiebeloep("kr 0.59"));
        assertPremiebeloep(premiebeloep(grunnlag, prosent("0.5950%"))).isEqualTo(premiebeloep("kr 0.60"));
    }

    /**
     * Verifiserer at premiebeløpa har presisjon 14, dvs at dei taklar opp til 12-sifra heiltall med 2 desimalar
     * (maksimalverdien blir då kr 999 999 999 999.99, presisjonsgrada for desimaltalla og den minst signifikante
     * heiltallsdelen synke for større tall enn dette).
     */
    @Test
    public void skal_klare_å_representere_opp_til_999_milliardar_med_to_desimalar() {
        assertThat(premiebeloep("kr   999 000 000 000.99").toString()).isEqualTo("999000000000.99");
        assertThat(premiebeloep("kr 1 000 000 000 000.99").toString()).isEqualTo("1000000000001.00");
        assertThat(premiebeloep("kr 1 000 000 000 000 009.99").toString()).isEqualTo("1000000000000000.00");
    }

    @Test
    public void skal_konstruere_premiebeloep_med_2_desimalar_sjoelv_om_input_ikkje_inneheld_desimalar() {
        assertDesimaler(premiebeloep("kr 100")).isEqualTo(2);
        assertDesimaler(premiebeloep(kroner(100))).isEqualTo(2);
    }

    @Test
    public void skal_konvertere_premiebeloepet_fra_tekst_til_oensket_verdi() {
        assertPremiebeloep(premiebeloep("kr 100.99")).isEqualTo(premiebeloep("100.99"));
        assertPremiebeloep(premiebeloep("100.99")).isEqualTo(premiebeloep("100.99"));
        assertPremiebeloep(premiebeloep("100,99")).isEqualTo(premiebeloep("100.99"));
        assertPremiebeloep(premiebeloep("10099")).isEqualTo(premiebeloep("10099"));
        assertPremiebeloep(premiebeloep("10 099")).isEqualTo(premiebeloep("10099"));
        assertPremiebeloep(premiebeloep("10 099 kr")).isEqualTo(premiebeloep("10099"));
    }

    @Test
    public void skal_avrunde_angitt_beloep_til_2_desimaler_ved_konstruksjon() {
        assertPremiebeloep(premiebeloep("kr 100.999")).isEqualTo(premiebeloep("101.00"));
    }

    @Test
    public void skal_avrunde_premiebeloepet_til_2_desimaler_etter_grunnlag_for_GRU_er_multiplisert_med_premiesats() {
        assertPremiebeloep(premiebeloep(grunnlagForGRU(aarsfaktor("50.004%"), faktureringsandel("100%")), kroner(1000)))
                .isEqualTo(premiebeloep("kr 500.04"));
    }

    @Test
    public void skal_avrunde_premiebeloepet_til_2_desimaler_etter_grunnlag_for_YSK_er_multiplisert_med_premiesats() {
        assertPremiebeloep(premiebeloep(grunnlagForYSK(aarsfaktor("50.004%"), faktureringsandel("100%")), kroner(1000)))
                .isEqualTo(premiebeloep("kr 500.04"));
    }

    private static AbstractObjectAssert<?, Premiebeloep> assertPremiebeloep(final Premiebeloep beloep) {
        return Assertions.assertPremiebeloep(beloep, 2);
    }


    private FaktureringsandelStatus faktureringsandel(String stillingsandel) {
        return new FaktureringsandelStatus(StillingsforholdId.valueOf(1L), Prosent.prosent(stillingsandel));
    }

    private Aarsfaktor aarsfaktor(String aarsfaktor) {
        return new Aarsfaktor(Prosent.prosent(aarsfaktor).toDouble());
    }
}