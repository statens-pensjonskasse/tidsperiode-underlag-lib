package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avregning.AvregningsRegelsett;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.FakturerbareDagsverkGRURegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.FakturerbareDagsverkYSKRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.Before;
import org.junit.Test;

public class AvregningsRegelsettTest {

    private UnderlagsperiodeBuilder builder;

    @Before
    public void _before() {
        builder = new UnderlagsperiodeBuilder();
    }

    @Test
    public void skal_kunne_beregne_gruppelivsdagsverk() {
        final StillingsforholdId stillingsforhold = StillingsforholdId.stillingsforhold(1);
        final UnderlagsperiodeBuilder periode = builder
                .fraOgMed(dato("2020.01.01"))
                .tilOgMed(dato("2020.12.31"))
                .med(new Aarstall(2020))
                .med(stillingsforhold)
                .med(AktiveStillingar.class, () -> Stream.of(enMedregning(stillingsforhold)))
                .med(Medlemsavtalar.class, ingenFakturerbareAvtaler())
                .med(Stillingsprosent.fulltid());

        assertGruppelivsdagsverk(periode).isEqualTo("0.00000");
    }

    @Test
    public void skal_kunne_beregne_yrkesskadedagsverk() {
        final StillingsforholdId stillingsforhold = StillingsforholdId.stillingsforhold(1);
        final UnderlagsperiodeBuilder periode = builder
                .fraOgMed(dato("2020.01.01"))
                .tilOgMed(dato("2020.12.31"))
                .med(new Aarstall(2020))
                .med(stillingsforhold)
                .med(AktiveStillingar.class, () -> Stream.of(enMedregning(stillingsforhold)))
                .med(Medlemsavtalar.class, ingenFakturerbareAvtaler())
                .med(Stillingsprosent.fulltid());

        assertYrkesskadedagsverk(periode).isEqualTo("0.00000");
    }

    private AktiveStillingar.AktivStilling enMedregning(StillingsforholdId stillingsforhold) {
        return new AktiveStillingar.AktivStilling(stillingsforhold, empty(), empty());
    }

    private static AbstractCharSequenceAssert<?, String> assertGruppelivsdagsverk(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p.beregn(FakturerbareDagsverkGRURegel.class)
                        .verdi()
                        .toString()
        ).as("fakturerbare dagsverk for gruppeliv for periode " + p);
    }

    private static AbstractCharSequenceAssert<?, String> assertYrkesskadedagsverk(final UnderlagsperiodeBuilder builder) {
        final Underlagsperiode p = bygg(builder);
        return assertThat(
                p.beregn(FakturerbareDagsverkYSKRegel.class)
                        .verdi()
                        .toString()
        ).as("fakturerbare dagsverk for yrkesskade for periode " + p);
    }

    private static Underlagsperiode bygg(UnderlagsperiodeBuilder builder) {
        final Underlagsperiode underlagsperiode = builder.bygg();
        final AvregningsRegelsett reglar = new AvregningsRegelsett();
        reglar.reglar().filter(p -> p.overlapper(underlagsperiode)).forEach(periode -> periode.annoter(underlagsperiode));
        return underlagsperiode;
    }

    private Medlemsavtalar ingenFakturerbareAvtaler() {
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