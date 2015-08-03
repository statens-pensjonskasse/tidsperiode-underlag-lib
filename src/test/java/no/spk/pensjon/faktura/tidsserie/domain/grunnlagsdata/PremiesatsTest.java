package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;


import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Produktinfo.GRU_35;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Produktinfo.GRU_36;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Produktinfo.YSK_79;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser.ingenSatser;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Produktinfo;

import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class PremiesatsTest {

    private static final Produktinfo VILLKAARLIG_PRODUKTINFO = new Produktinfo(1);

    @Test
    public void tommeSatserErIkkeFakturerbare() throws Exception {
        Premiesats premiesats = premiesats(Produkt.GRU)
                .produktinfo(GRU_35)
                .satser(ingenSatser()).bygg();

        assertThat(premiesats.erFakturerbar()).isFalse();
    }

    @Test
    public void satserMedBeloepErIkkeFakturerbare() throws Exception {
        Premiesats premiesats = premiesats(Produkt.UKJ)
                .produktinfo(VILLKAARLIG_PRODUKTINFO)
                .satser(new Satser<>(prosent("1%"), Prosent.ZERO, Prosent.ZERO)).bygg();

        assertThat(premiesats.erFakturerbar()).isTrue();
    }

    @Test
    public void premiesatsGRUMedBeloepErFakturerbare() throws Exception {
        Premiesats.Builder gruBuilder = premiesats(Produkt.GRU).satser(new Satser<>(kroner(1), ZERO, ZERO));

        assertThat(gruBuilder.produktinfo(VILLKAARLIG_PRODUKTINFO).bygg().erFakturerbar()).isFalse();

        assertThat(gruBuilder.produktinfo(GRU_35).bygg().erFakturerbar()).isTrue();
        assertThat(gruBuilder.produktinfo(GRU_36).bygg().erFakturerbar()).isTrue();
    }

    @Test
    public void premiesatsYSKMedBeloepErFakturerbare() throws Exception {
        Premiesats.Builder gruBuilder = premiesats(Produkt.YSK).satser(new Satser<>(kroner(1), ZERO, ZERO));

        assertThat(gruBuilder.produktinfo(YSK_79).bygg().erFakturerbar()).isFalse();

        assertThat(gruBuilder.produktinfo(VILLKAARLIG_PRODUKTINFO).bygg().erFakturerbar()).isTrue();
    }
}