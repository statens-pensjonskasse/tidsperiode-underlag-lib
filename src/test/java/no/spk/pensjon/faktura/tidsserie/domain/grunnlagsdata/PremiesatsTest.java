package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;


import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.GRU_35;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.GRU_36;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.YSK_79;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser.ingenSatser;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class PremiesatsTest {

    private static final Produktinfo VILLKAARLIG_PRODUKTINFO = new Produktinfo(1);
    private static final Produkt PRODUKT_IKKE_YSK_ELLER_GRU = Produkt.UKJ;
    private static final Prosent PROSENTSATS_STOERRE_ENN_NULL = prosent("1%");

    @Test
    public void tommeSatserErIkkeFakturerbare() throws Exception {
        Premiesats premiesats = premiesats(Produkt.GRU)
                .produktinfo(GRU_35)
                .satser(ingenSatser()).bygg();

        assertThat(premiesats.erFakturerbar()).isFalse();
    }

    @Test
    public void satserMedBeloepErFakturerbare() throws Exception {
        Premiesats premiesats = premiesats(PRODUKT_IKKE_YSK_ELLER_GRU)
                .produktinfo(VILLKAARLIG_PRODUKTINFO)
                .satser(new Satser<>(PROSENTSATS_STOERRE_ENN_NULL, Prosent.ZERO, Prosent.ZERO)).bygg();

        assertThat(premiesats.erFakturerbar()).isTrue();
    }

    @Test
    public void premiesatsGRUMedBeloepErFakturerbare() throws Exception {
        Premiesats.Builder gruBuilder = premiesats(Produkt.GRU).satser(new Satser<>(KRONEBELOEP_STOERRE_ENN_NULL(), ZERO, ZERO));

        assertThat(gruBuilder.produktinfo(VILLKAARLIG_PRODUKTINFO).bygg().erFakturerbar()).isFalse();

        assertThat(gruBuilder.produktinfo(GRU_35).bygg().erFakturerbar()).isTrue();
        assertThat(gruBuilder.produktinfo(GRU_36).bygg().erFakturerbar()).isTrue();
    }

    private Kroner KRONEBELOEP_STOERRE_ENN_NULL() {
        return kroner(1);
    }

    @Test
    public void premiesatsYSKMedBeloepErFakturerbare() throws Exception {
        Premiesats.Builder gruBuilder = premiesats(Produkt.YSK).satser(new Satser<>(KRONEBELOEP_STOERRE_ENN_NULL(), ZERO, ZERO));

        assertThat(gruBuilder.produktinfo(YSK_79).bygg().erFakturerbar()).isFalse();

        assertThat(gruBuilder.produktinfo(VILLKAARLIG_PRODUKTINFO).bygg().erFakturerbar()).isTrue();
    }
}