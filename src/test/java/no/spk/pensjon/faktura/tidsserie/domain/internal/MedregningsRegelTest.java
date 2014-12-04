package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Test;

import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.BISTILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.TILLEGG_ANNEN_ARBGIV;
import static org.assertj.core.api.Assertions.assertThat;

public class MedregningsRegelTest {
    @Test
    public void skalMedregneBistilling() {
        assertMedregnaBeloepForKode(
                eiPeriodeOverEtAar()
                        .med(new Medregning(kroner(1_000))),
                BISTILLING
        )
                .isEqualTo(kroner(1_000));
    }

    @Test
    public void skalMedregneLoennAnnenArbeidsgiver() {
        assertMedregnaBeloepForKode(
                eiPeriodeOverEtAar()
                        .med(new Medregning(kroner(1_000))),
                TILLEGG_ANNEN_ARBGIV
        )
                .isEqualTo(kroner(1_000));
    }

    @Test
    public void skalIkkjeMedregneNokonAndreTyperMedregning() {
        final UnderlagsperiodeBuilder periode = eiPeriodeOverEtAar()
                .med(new Medregning(kroner(100_000)));
        rangeClosed(1, 999)
                .mapToObj(Medregningskode::valueOf)
                .filter(m -> !m.erBistilling() && !m.erTilleggAnnenArbeidsgiver())
                .forEach(kode -> {
                    assertMedregnaBeloepForKode(periode, kode).isEqualTo(kroner(0));
                });
    }

    private AbstractComparableAssert<?, Kroner> assertMedregnaBeloepForKode(UnderlagsperiodeBuilder periode, Medregningskode kode) {
        return assertThat(
                periode
                        .med(kode)
                        .bygg()
                        .beregn(MedregningsRegel.class)
        )
                .as("medregna beløp for medregningskode " + kode);
    }

    private UnderlagsperiodeBuilder eiPeriodeOverEtAar() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2005.01.01"))
                .tilOgMed(dato("2005.12.31"))
                .med(new MedregningsRegel())
                ;
    }
}