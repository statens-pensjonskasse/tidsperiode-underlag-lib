package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Risikoklasse;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AvtaleproduktTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

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

    @Test
    public void skalPopulereRisikoklassePaaAvtalen() {
        final Optional<Risikoklasse> expected = of(new Risikoklasse("1"));
        final AvtaleId id = avtaleId(123456L);

        final Avtale.AvtaleBuilder avtale = avtale(id);
        lagProdukt(id, YSK, 71).risikoklasse(expected).populer(avtale);

        assertThat(avtale.bygg().risikoklasse()).as("risikoklasse").isEqualTo(expected);
    }

    @Test
    public void skalIkkjeGodtaRisikoklasseForAndreProduktEnnYsk() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("risikoklasse er ikkje støtta for " + GRU);
        e.expectMessage("risikoklasse er kun støtta for avtaleprodukt tilknytta " + YSK);
        lagProdukt(avtaleId(123456L), GRU, 35).risikoklasse(of(new Risikoklasse("1")));
    }

    private static AbstractBooleanAssert<?> assertBetalerPremieFor(AvtaleId id, Produkt type, Avtaleprodukt produkt) {
        return assertThat(produkt.populer(avtale(id)).bygg().betalarTilSPKFor(type)).as("betalar avtalen " + type + " til SPK for avtaleprodukt " + produkt);
    }

    private static Avtaleprodukt lagProdukt(AvtaleId id, Produkt type, int produktinfo) {
        return new Avtaleprodukt(
                dato("2000.01.01"),
                empty(),
                id,
                type,
                new Produktinfo(produktinfo),
                Satser.ingenSatser()
        );
    }
}