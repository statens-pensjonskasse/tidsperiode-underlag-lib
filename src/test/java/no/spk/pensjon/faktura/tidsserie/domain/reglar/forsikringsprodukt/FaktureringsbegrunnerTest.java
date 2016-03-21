package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.ENDRINGSMELDING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Faktureringsbegrunner.LOVLIGE_PRODUKT;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ER_MEDREGNING;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ER_PERMISJON_UTEN_LOENN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class FaktureringsbegrunnerTest {

    private static final StillingsforholdId STILLINGSFORHOLD = stillingsforhold(1);
    private Faktureringsbegrunner begrunner;

    @Before
    public void setUp() throws Exception {
        begrunner = new Faktureringsbegrunner(YSK);
    }

    @Test
    public void skal_ikke_feile_for_ysk_og_gru() throws Exception {
        LOVLIGE_PRODUKT.stream().forEach(Faktureringsbegrunner::new);
    }

    @Test
    public void skal_feile_for_produkter_som_ikke_er_ysk_og_gru() throws Exception {
        Stream.of(Produkt.values())
                .filter(p -> !LOVLIGE_PRODUKT.contains(p))
                .forEach(produkt -> {
                    try {
                        new Faktureringsbegrunner(produkt);
                        fail("Skulle ha kastet exception.");
                    } catch (IllegalArgumentException e) {
                        assertThat(e.getMessage()).contains("Kan ikke lage faktureringsaarsak for produkt:");
                    }
                });
    }

    @Test
    public void medregning_er_ikke_fakturerbar() throws Exception {
        final Fordelingsaarsak fordelingsaarsak = begrunner.fordelingsaarsakFor(
                medregningstilling(),
                periode().med(Medlemsavtalar.class, betalerForAlleProdukt(true)).bygg()
        );
        assertThat(fordelingsaarsak).isEqualTo(ER_MEDREGNING);
        assertThat(fordelingsaarsak.fakturerbar()).isFalse();
    }


    @Test
    public void permisjon_uten_loenn_er_ikke_fakturerbar() throws Exception {
        final Fordelingsaarsak fordelingsaarsak = begrunner.fordelingsaarsakFor(
                permisjonUtenLoenn(),
                periode().med(Medlemsavtalar.class, betalerForAlleProdukt(true)).bygg()
        );
        assertThat(fordelingsaarsak).isEqualTo(ER_PERMISJON_UTEN_LOENN);
        assertThat(fordelingsaarsak.fakturerbar()).isFalse();
    }

    @Test
    public void avtale_som_ikke_har_produkt_skal_ikke_faktureres() throws Exception {
        final Fordelingsaarsak fordelingsaarsak = begrunner.fordelingsaarsakFor(
                enAktivStilling("100%", ENDRINGSMELDING),
                periode().med(Medlemsavtalar.class, betalerForAlleProdukt(false)).bygg()
        );
        assertThat(fordelingsaarsak).isEqualTo(AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT);
        assertThat(fordelingsaarsak.fakturerbar()).isFalse();
    }

    @Test
    public void fakturerbareStillingerForPeriodeFactory_skal_sette_fordelingsaarsak_paa_stilling() throws Exception {
        final AktivStilling stilling = medregningstilling();
        final FakturerbareStillingerForPeriodeFactory factory = new FakturerbareStillingerForPeriodeFactory(
                YSK,
                periode().med(Medlemsavtalar.class, betalerForAlleProdukt(true))
                        .annoter(AktiveStillingar.class, () -> Stream.of(stilling))
                        .bygg()
        );

        final List<FakturerbarStilling> stillinger = factory.stillinger().collect(toList());
        assertThat(stillinger).hasSize(1);
        assertThat(stillinger.get(0).aktivStilling()).isSameAs(stilling);
        assertThat(stillinger.get(0).status()).isEqualTo(ER_MEDREGNING);
    }

    private UnderlagsperiodeBuilder periode() {
        final Aarstall premieAar = new Aarstall(now().getYear());
        return new UnderlagsperiodeBuilder()
                .fraOgMed(premieAar.atStartOfYear())
                .tilOgMed(premieAar.atEndOfYear());
    }

    private Medlemsavtalar betalerForAlleProdukt(final boolean betalerForAlleProduktOgStillinger) {
        return new Medlemsavtalar() {
            @Override
            public boolean betalarTilSPKFor(final StillingsforholdId stilling, final Produkt produkt) {
                return betalerForAlleProduktOgStillinger;
            }

            @Override
            public Avtale avtaleFor(final StillingsforholdId stilling) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private AktivStilling medregningstilling() {
        return enAktivStilling(null, null);
    }

    private AktivStilling permisjonUtenLoenn() {
        return enAktivStilling("100%", Aksjonskode.PERMISJON_UTAN_LOENN);
    }

    private AktivStilling enAktivStilling(String stillingsprosent, Aksjonskode aksjonskode) {
        return new AktivStilling(STILLINGSFORHOLD,
                ofNullable(stillingsprosent).map(Prosent::new),
                ofNullable(aksjonskode));
    }
}