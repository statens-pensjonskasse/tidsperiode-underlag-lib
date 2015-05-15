package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import org.junit.Test;

public class AvtaleversjonTest {
    @Test
    public void skalPopulerePremiestatus() {
        final Premiestatus expected = Premiestatus.AAO_02;

        final AvtaleId avtaleId = avtaleId(123456L);
        final AvtaleBuilder builder = avtale(avtaleId);
        new Avtaleversjon(dato("2000.01.01"), empty(), avtaleId, expected)
                .populer(
                        builder
                );

        assertThat(builder.bygg().premiestatus()).isEqualTo(expected);
    }

}