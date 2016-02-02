package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Assertions.assertPremiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GRUpremierTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private UnderlagsperiodeBuilder builder;

    private GRUpremier regel;

    @Before
    public void before() {
        final Aarstall premieaar = new Aarstall(2015);
        builder = new UnderlagsperiodeBuilder()
                .fraOgMed(premieaar.atStartOfYear())
                .tilOgMed(premieaar.atEndOfYear())
        ;
        regel = new GRUpremier();
    }

    @Test
    public void skal_beregne_premiebeloep_for_arbeidsgiver_basert_paa_premiesats_for_arbeidsgiverandel() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("50%"),
                        builder.med(
                                Avtale.class,
                                enAvtaleMedGRU(
                                        new Satser<>(
                                                kroner(100),
                                                kroner(0),
                                                kroner(0)
                                        )
                                )
                                        .bygg()
                        )
                )
                        .arbeidsgiver(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(50))
                );
    }

    @Test
    public void skal_beregne_premiebeloep_for_medlem_basert_paa_premiesats_for_medlemsandel() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("50%"),
                        builder.med(
                                Avtale.class,
                                enAvtaleMedGRU(
                                        new Satser<>(
                                                kroner(0),
                                                kroner(100),
                                                kroner(0)
                                        )
                                )
                                        .bygg()
                        )
                )
                        .medlem(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(50))
                );

    }

    @Test
    public void skal_beregne_premiebeloep_for_administrasjonsgebyr_basert_paa_premiesats_for_administrasjonsgebyr() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("50%"),
                        builder.med(
                                Avtale.class,
                                enAvtaleMedGRU(
                                        new Satser<>(
                                                kroner(0),
                                                kroner(0),
                                                kroner(100)
                                        )
                                )
                                        .bygg()
                        )
                )
                        .administrasjonsgebyr(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(50))
                );
    }

    @Test
    public void skal_avrunde_premiebeloep_til_to_desimaler_etter_premiesats_er_multiplisert_med_grunnlag() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("8.4931507%"),
                        builder.med(
                                Avtale.class,
                                enAvtaleMedGRU(
                                        new Satser<>(
                                                kroner(0),
                                                kroner(0),
                                                kroner(500)
                                        )
                                )
                                        .bygg()
                        )
                )
                        .administrasjonsgebyr(),
                2
        )
                .isEqualTo(
                        premiebeloep("42.47")
                );
    }

    @Test
    public void skal_feile_dersom_perioden_ikke_er_annotert_med_avtale() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage("Avtale");

        beregn(grunnlagForGRU("100%"), builder);
    }

    @Test
    public void skal_beregne_premie_lik_kr_0_dersom_avtalen_ikkje_har_produktet() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("100%"),
                        builder.med(
                                Avtale.class,
                                avtale(avtaleId(200_000)).bygg()
                        )
                )
                        .total(),
                2
        )
                .isEqualTo(premiebeloep("kr 0"));
    }

    @Test
    public void skal_beregne_premie_lik_kr_0_dersom_produktet_ikkje_er_eit_forsikringsprodukt() {
        assertPremiebeloep(
                beregn(
                        grunnlagForGRU("100%"),
                        builder.med(
                                Avtale.class,
                                avtale(avtaleId(200_000))
                                        .addPremiesats(
                                                premiesats(PEN)
                                                        .produktinfo(new Produktinfo(10))
                                                        .satser(new Satser<>(
                                                                prosent("50%"),
                                                                prosent("0%"),
                                                                prosent("0%")
                                                        ))
                                                        .bygg()
                                        )
                                        .bygg()
                        )
                ).total(),
                2
        )
                .isEqualTo(premiebeloep("kr 0"));
    }

    private Premier beregn(final GrunnlagForGRU grunnlag, final UnderlagsperiodeBuilder builder) {
        return regel.beregn(builder.bygg(), grunnlag);
    }

    private static Avtale.AvtaleBuilder enAvtaleMedGRU(final Satser<Kroner> satser) {
        return avtale(avtaleId(240500))
                .addPremiesats(
                        premiesats(GRU)
                                .produktinfo(new Produktinfo(77))
                                .satser(
                                        satser
                                )
                                .bygg()
                );
    }

    private GrunnlagForGRU grunnlagForGRU(String grunnlag) {
        return GrunnlagForGRU.grunnlagForGRU(
                new Aarsfaktor(Prosent.prosent(grunnlag).toDouble()),
                new FaktureringsandelStatus(
                        StillingsforholdId.valueOf(1L),
                        Prosent.prosent("100%")
                )
        );
    }
}