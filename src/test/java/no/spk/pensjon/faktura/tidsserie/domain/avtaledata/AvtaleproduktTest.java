package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleproduktTest {
    @Test
    public void skalIgnorereGruppelivsproduktetVissProduktInfoIkkjeErFakturerbar() {
        final AvtaleId id = avtaleId(123456L);
        assertBetalerPremieFor(id, GRU, lagProdukt(id, GRU, 31)).isFalse();
        assertBetalerPremieFor(id, GRU, lagProdukt(id, GRU, 35)).isTrue();
        assertBetalerPremieFor(id, GRU, lagProdukt(id, GRU, 36)).isTrue();
        assertBetalerPremieFor(id, GRU, lagProdukt(id, GRU, 37)).isFalse();
        assertBetalerPremieFor(id, GRU, lagProdukt(id, GRU, 39)).isFalse();
    }

    @Test
    public void skalIgnorereYrkesskadeproduktetVissProduktInfoIkkjeErFakturerbar() {
        final AvtaleId id = avtaleId(123456L);
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 71)).isTrue();
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 72)).isTrue();
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 73)).isTrue();
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 74)).isTrue();
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 76)).isTrue();
        assertBetalerPremieFor(id, YSK, lagProdukt(id, YSK, 79)).isFalse();
    }

    private static AbstractBooleanAssert<?> assertBetalerPremieFor(AvtaleId id, Produkt type, Avtaleprodukt produkt) {
        return assertThat(produkt.populer(avtale(id)).bygg().betalarTilSPKFor(type)).as("betalar avtalen " + type + " til SPK for avtaleprodukt " + produkt);
    }

    private static Avtaleprodukt lagProdukt(AvtaleId id, Produkt type, int produktinfo) {
        return new Avtaleprodukt(
                dato("2000.01.01"),
                Optional.<LocalDate>empty(),
                id,
                type,
                produktinfo,
                Prosent.ZERO,
                Prosent.ZERO,
                Prosent.ZERO,
                kroner(10),
                kroner(10),
                kroner(10)
        );
    }
}