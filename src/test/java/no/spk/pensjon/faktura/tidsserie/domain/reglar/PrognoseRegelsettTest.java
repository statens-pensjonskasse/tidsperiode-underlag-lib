package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Termintype;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractObjectAssert;
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
    public void skalIkkeBeregnePensjonsgivandeAarsloennPrDagsDatoUnderMinstegrense2015() {
        assertPensjonsgivendeAarsloenn(
                builder
                        .fraOgMed(dato("2015.01.01"))
                        .tilOgMed(dato("2015.12.31"))
                        .med(Aksjonskode.ENDRINGSMELDING)
                        .med(new DeltidsjustertLoenn(kroner(300_000)))
                        .med(new Stillingsprosent(new Prosent("20%")))
                        .med(new Grunnbeloep(kroner(88_300)))
                        .med(Ordning.SPK)
                        .med(Premiestatus.AAO_01)
                        .med(new Aarstall(2015))
        )
                .isEqualTo(kroner(211_920));
    }

    @Test
    public void skalIkkeBeregnePensjonsgivandeAarsloennPrDagsDatoUnderMinstegrense2016() {
        assertPensjonsgivendeAarsloenn(
                builder
                        .fraOgMed(dato("2016.01.01"))
                        .tilOgMed(dato("2016.12.31"))
                        .med(Aksjonskode.ENDRINGSMELDING)
                        .med(new DeltidsjustertLoenn(kroner(250_000)))
                        .med(new Stillingsprosent(new Prosent("19,9%")))
                        .med(Ordning.SPK)
                        .med(Premiestatus.AAO_01)
                        .med(new Aarstall(2016))
        )
                .isEqualTo(kroner(0));
    }

    @Test
    public void skalKunneBeregnePensjonsgivandeAarsloennPrDagsDatoOverMinstegrense2016() {
        assertPensjonsgivendeAarsloenn(
                builder
                        .fraOgMed(dato("2016.01.01"))
                        .tilOgMed(dato("2016.12.31"))
                        .med(Aksjonskode.ENDRINGSMELDING)
                        .med(new DeltidsjustertLoenn(kroner(300_000)))
                        .med(new Stillingsprosent(new Prosent("20%")))
                        .med(new Grunnbeloep(kroner(88_300)))
                        .med(Ordning.SPK)
                        .med(Premiestatus.AAO_01)
                        .med(new Aarstall(2016))
        )
                .isEqualTo(kroner(211_920));
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

    @Test
    public void skalKunneBeregneGruppelivsandel() {
        final StillingsforholdId stillingsforhold = StillingsforholdId.stillingsforhold(1);
        assertGruppelivsfakturering(
                builder
                        .fraOgMed(dato("2020.01.01"))
                        .tilOgMed(dato("2020.12.31"))
                        .med(new Aarstall(2020))
                        .med(stillingsforhold)
                        .med(AktiveStillingar.class, Stream::empty)
                        .med(Medlemsavtalar.class, dummyMedlemsavtaler())
                        .med(Stillingsprosent.fulltid())
        ).isEqualTo("0%");
    }

    @Test
    public void skalKunneBeregneYrkesskadeandel() {
        final StillingsforholdId stillingsforhold = StillingsforholdId.stillingsforhold(1);
        assertYrkesskadefakturering(
                builder
                        .fraOgMed(dato("2020.01.01"))
                        .tilOgMed(dato("2020.12.31"))
                        .med(new Aarstall(2020))
                        .med(stillingsforhold)
                        .med(AktiveStillingar.class, Stream::empty)
                        .med(Medlemsavtalar.class, dummyMedlemsavtaler())
                        .med(Stillingsprosent.fulltid())
        ).isEqualTo("0%");
    }


    @Test
    public void skalKunneBeregneTermintype() {
        assertTermintype(
                builder
                        .fraOgMed(dato("2020.01.01"))
                        .tilOgMed(dato("2020.12.31"))
                        .med(new Aarstall(2020))
        ).isEqualTo(Termintype.UKJENT);
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

    private static AbstractCharSequenceAssert<?, String> assertGruppelivsfakturering(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p.beregn(GruppelivsfaktureringRegel.class)
                        .andel()
                        .toString()
        ).as("gruppelivsfakturering for periode " + p);
    }

    private static AbstractCharSequenceAssert<?, String> assertYrkesskadefakturering(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p.beregn(YrkesskadefaktureringRegel.class)
                        .andel()
                        .toString()
        ).as("Yrkesskadefakturering for periode " + p);
    }

    private static AbstractObjectAssert<?, Termintype> assertTermintype(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p.beregn(TermintypeRegel.class)
        ).as("termintype for periode " + p);
    }

    private static Underlagsperiode bygg(UnderlagsperiodeBuilder builder) {
        final Underlagsperiode underlagsperiode = builder.bygg();
        final PrognoseRegelsett reglar = new PrognoseRegelsett();
        reglar.reglar().filter(p -> p.overlapper(underlagsperiode)).forEach(periode -> periode.annoter(underlagsperiode));
        return underlagsperiode;
    }

    private Medlemsavtalar dummyMedlemsavtaler() {
        return new Medlemsavtalar() {
            @Override
            public boolean betalarTilSPKFor(final StillingsforholdId stilling, final Produkt produkt) {
                return false;
            }

            @Override
            public Avtale avtaleFor(final StillingsforholdId stilling) {
                throw new UnsupportedOperationException();
            }
        };
    }
}