package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Assertions.assertPremiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.KonverterFraTekst.pensjonsgivendeLoenn;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.AFP;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enhetstester for {@link PensjonsproduktPensjonspremier}.
 *
 * @author Tarjei Skorgenes
 */
public class PensjonsproduktPensjonspremierTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private UnderlagsperiodeBuilder builder;

    private PensjonsproduktPensjonspremier regel;

    @Before
    public void before() {
        final Aarstall premieaar = new Aarstall(2015);
        builder = new UnderlagsperiodeBuilder()
                .fraOgMed(premieaar.atStartOfYear())
                .tilOgMed(premieaar.atEndOfYear())
                .med(MaskineltGrunnlagRegel.class, pensjonsgivendeLoenn("kr 600 000"))
        ;
        regel = new PensjonsproduktPensjonspremier();
    }

    @Test
    public void skal_beregne_premiebeloep_for_arbeidsgiver_basert_paa_premiesats_for_arbeidsgiverandel() {
        assertPremiebeloep(
                beregn(
                        PEN,
                        builder.med(
                                Avtale.class,
                                enAvtaleMedPEN(
                                        new Satser<>(
                                                prosent("10%"),
                                                prosent("0%"),
                                                prosent("0%")
                                        )
                                )
                                        .bygg()
                        )
                )
                        .arbeidsgiver(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(60_000))
                );
    }

    @Test
    public void skal_beregne_premiebeloep_for_medlem_basert_paa_premiesats_for_medlemsandel() {
        assertPremiebeloep(
                beregn(
                        PEN,
                        builder.med(
                                Avtale.class,
                                enAvtaleMedPEN(
                                        new Satser<>(
                                                prosent("0%"),
                                                prosent("2%"),
                                                prosent("0%")
                                        )
                                )
                                        .bygg()
                        )
                )
                        .medlem(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(12_000))
                );

    }

    @Test
    public void skal_beregne_premiebeloep_for_administrasjonsgebyr_basert_paa_premiesats_for_administrasjonsgebyr() {
        assertPremiebeloep(
                beregn(
                        PEN,
                        builder.med(
                                Avtale.class,
                                enAvtaleMedPEN(
                                        new Satser<>(
                                                prosent("0%"),
                                                prosent("0%"),
                                                prosent("0.35%")
                                        )
                                )
                                        .bygg()
                        )
                )
                        .administrasjonsgebyr(),
                2
        )
                .isEqualTo(
                        premiebeloep(kroner(2_100))
                );
    }

    @Test
    public void skal_feile_dersom_perioden_ikke_er_annotert_med_avtale() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage("Avtale");

        beregn(Produkt.PEN, builder);
    }

    @Test
    public void skal_beregne_premie_lik_kr_0_dersom_avtalen_ikkje_har_produktet() {
        assertPremiebeloep(
                beregn(
                        AFP,
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
    public void skal_beregne_premie_lik_kr_0_dersom_produktet_ikkje_er_eit_pensjonsprodukt() {
        assertPremiebeloep(
                beregn(
                        GRU,
                        builder.med(
                                Avtale.class,
                                avtale(avtaleId(200_000))
                                        .addPremiesats(
                                                premiesats(GRU)
                                                        .produktinfo(Produktinfo.GRU_35)
                                                        .satser(new Satser<>(
                                                                kroner(1000),
                                                                kroner(0),
                                                                kroner(0)
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

    private Premier beregn(final Produkt produkt, final UnderlagsperiodeBuilder builder) {
        return regel.beregn(builder.bygg(), produkt);
    }

    private static Avtale.AvtaleBuilder enAvtaleMedPEN(final Satser<Prosent> satser) {
        return avtale(avtaleId(240500))
                .addPremiesats(
                        premiesats(PEN)
                                .produktinfo(new Produktinfo(10))
                                .satser(
                                        satser
                                )
                                .bygg()
                );
    }
}