package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Before;
import org.junit.Test;

public class PrognoseRegelsettTest {

    private UnderlagsperiodeBuilder builder;

    @Before
    public void _before() {
        builder = new UnderlagsperiodeBuilder();
    }

    @Test
    public void skalKunneBeregnePensjonsgivandeAarsloennPrDagsDato() {
        assertPensjonsgivendeAarsloenn(
                builder
                        .fraOgMed(dato("2000.01.01"))
                        .tilOgMed(dato("2000.12.31"))
                        .med(Aksjonskode.ENDRINGSMELDING)
                        .med(new DeltidsjustertLoenn(kroner(300_000)))
                        .med(Stillingsprosent.fulltid())
                        .med(new Grunnbeloep(kroner(88_300)))
                        .med(Ordning.SPK)
                        .med(Premiestatus.AAO_01)
                        .med(new Aarstall(2000))
        )
                .isEqualTo(kroner(300_000));
    }

    @Test
    public void skalKunneBeregneAarsverk() {
        assertAarsverk(
                builder
                        .fraOgMed(dato("2020.01.01"))
                        .tilOgMed(dato("2020.12.31"))
                        .med(new Aarstall(2020))
                        .med(Stillingsprosent.fulltid())
        ).isEqualTo("100%");

    }

    private static AbstractComparableAssert<?, Kroner> assertPensjonsgivendeAarsloenn(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(p.beregn(MaskineltGrunnlagRegel.class))
                .as("pensjonsgivende årslønn pr dags dato for periode " + p);
    }

    private static AbstractCharSequenceAssert<?, String> assertAarsverk(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p
                        .beregn(AarsverkRegel.class)
                        .tilProsent()
                        .toString()
        ).as("årsverk for periode " + p);
    }

    private static Underlagsperiode bygg(UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = builder.bygg();
        final PrognoseRegelsett reglar = new PrognoseRegelsett();
        reglar.reglar().forEach(periode -> periode.annoter(p));
        return p;
    }
}