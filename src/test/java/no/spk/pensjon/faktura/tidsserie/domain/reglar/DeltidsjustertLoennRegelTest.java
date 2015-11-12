package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Test;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.DeltidsjustertLoennRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoennRegelTest {
    @Test
    public void skalGenerereTomLoennVissStillingaErUteIPermisjonUtanLoenn() {
        assertDeltidsjustertLoenn(
                perioda()
                        .med(new DeltidsjustertLoenn(kroner(100_000)))
                        .med(Aksjonskode.PERMISJON_UTAN_LOENN)
        ).isEqualTo(kroner(0));
    }

    @Test
    public void skalIkkjeFeileBeregingNårDeltidsjustertLoennManglarPaaGrunnAvMedregning() {
        assertDeltidsjustertLoenn(
                perioda()
                        .med(new Medregning(kroner(12_000)))
        ).isEqualTo(kroner(0));
    }

    @Test
    public void skalKonvertereLoennstrinnTilDeltidsjustertLoennVissLoennstrinnErAnnotertPaaPerioda() {
        assertDeltidsjustertLoenn(
                perioda()
                        .med(new Loennstrinn(53))
                        .med(new LoennstrinnBeloep(new Kroner(372_000)))
                        .med(new Stillingsprosent(new Prosent("100%")))
        ).isEqualTo(new Kroner(372_000));
    }

    @Test
    public void skalJustereLoennstrinnBeloepetIHenholdTilStillingsprosent() {
        assertDeltidsjustertLoenn(
                perioda()
                        .med(new Loennstrinn(60))
                        .med(new LoennstrinnBeloep(new Kroner(400_000)))
                        .med(new Stillingsprosent(new Prosent("50%")))
        ).isEqualTo(new Kroner(200_000));
    }

    @Test
    public void skalSlaaOppDeltidsjustertLoennFraPeriodaVissLoennsTrinnManglar() {
        final Kroner beloep = new Kroner(500_000);
        assertDeltidsjustertLoenn(
                perioda()
                        .med(new DeltidsjustertLoenn(beloep))
        ).isEqualTo(beloep);
    }

    @Test
    public void skalIkkjeAvkorteLoennBasertPaaUnderlagsperiodasLengde() {
        assertDeltidsjustertLoenn(
                periode("2001.01.01", "2001.01.01")
                        .med(new DeltidsjustertLoenn(new Kroner(365_000)))
                        .med(Aksjonskode.ENDRINGSMELDING)
        ).isEqualTo(new Kroner(365_000));
    }

    private UnderlagsperiodeBuilder perioda() {
        return periode("2010.01.01", "2010.12.31").med(Aksjonskode.ENDRINGSMELDING);
    }

    private AbstractComparableAssert<?, Kroner> assertDeltidsjustertLoenn(final UnderlagsperiodeBuilder periode) {
        return assertThat(periode.bygg().beregn(DeltidsjustertLoennRegel.class));
    }

    private static UnderlagsperiodeBuilder periode(final String fra, final String til) {
        return Support.periode(fra, til)
                .med(new DeltidsjustertLoennRegel())
                .med(new ErMedregningRegel())
                .med(new ErPermisjonUtanLoennRegel());
    }
}