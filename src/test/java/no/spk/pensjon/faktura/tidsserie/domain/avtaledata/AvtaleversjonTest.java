package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.enAvtaleversjon;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;

import org.junit.Test;

public class AvtaleversjonTest {
    @Test
    public void skalPopulerePremiestatus() {
        final Premiestatus expected = Premiestatus.AAO_02;

        final AvtaleId avtaleId = avtaleId(123456L);
        final AvtaleBuilder builder = avtale(avtaleId);
        enAvtaleversjon(avtaleId)
                .premiestatus(expected)
                .bygg()
                .populer(
                        builder
                );

        assertThat(builder.bygg().premiestatus()).isEqualTo(expected);
    }

    @Test
    public void skalPopulerePremiekategori() {
        final Premiekategori expected = Premiekategori.HENDELSESBASERT;

        final AvtaleId avtaleId = avtaleId(123456L);
        final AvtaleBuilder builder = avtale(avtaleId);
        enAvtaleversjon(avtaleId)
                .premiekategori(expected)
                .bygg()
                .populer(
                        builder
                );

        assertThat(builder.bygg().premiekategori()).isEqualTo(of(expected));
    }
}