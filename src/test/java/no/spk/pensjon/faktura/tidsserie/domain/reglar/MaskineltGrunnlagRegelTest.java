package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class MaskineltGrunnlagRegelTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at maskinelt grunnlag blir satt lik kr 0 dersom gjeldande aksjonskode for stillingsforholdet
     * er permisjon utan lønn.
     */
    @Test
    public void skalBeregneMaskineltGrunnlagLikKr0DersomStillingaErUteIPermisjonUtanLoenn() {
        assertMaskineltGrunnlag(
                periode("2008.04.01", "2008.04.30")
                        .med(new Aarstall(2008))
                        .med(new DeltidsjustertLoenn(kroner(75_000)))
                        .med(fulltid())
                        .med(Aksjonskode.PERMISJON_UTAN_LOENN)
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at maskinelt grunnlag blir satt lik kr 0 dersom stillingsforholdet er under minstegrensa
     * i perioda som blir beregna.
     */
    @Test
    public void skalBeregneMaskineltGrunnlagLikKr0DersomStillingaErUnderMinstegrensaVersjon1() {
        assertMaskineltGrunnlag(
                periode("2008.04.01", "2008.04.30")
                        .med(new Aarstall(2008))
                        .med(new DeltidsjustertLoenn(kroner(75_000)))
                        .med(new Stillingsprosent(new Prosent("15%")))
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at maskinelt grunnlag blir satt lik kr 0 dersom stillingsforholdet er under ny minstegrensa
     * (201%) i perioda som blir beregna.
     */
    @Test
    public void skalBeregneMaskineltGrunnlagLikKr0DersomStillingaErUnderMinstegrensaVersjon2() {
        assertMaskineltGrunnlag(
                periodeFom2016("2016.01.01", "2016.12.31")
                        .med(new Aarstall(2016))
                        .med(new DeltidsjustertLoenn(kroner(75_000)))
                        .med(new Stillingsprosent(new Prosent("15%")))
        ).isEqualTo(kroner(0));
    }

    /**
     * Verifiserer at maskinelt grunnlag blir beregnet dersom stillingsforholdet er over ny minstegrensen
     * (20%) i perioden som blir beregnet.
     */
    @Test
    public void skalBeregneMaskineltGrunnlagDersomStillingaErOver20ProsentFom2016() {
        UnderlagsperiodeBuilder med = periodeFom2016("2016.01.01", "2016.12.31")
                .med(new Aarstall(2016))
                .med(new DeltidsjustertLoenn(kroner(500_000)))
                .med(new Stillingsprosent(new Prosent("25%")));
        assertMaskineltGrunnlag(
                med
        ).isEqualTo(kroner(500000));
    }

    /**
     * Verifiserer at minstegrensa blir ignorert for medregningar sidan minstegrensehandteringa for bistillingar og
     * tillegg annan arbeidsgivar ikkje er veldefinert korleis skal fungere innanfor dei forenklingane som fastsats-
     * metodikken implementerer.
     */
    @Test
    public void skalIkkjeTaHensynTilMinstegrensaVedMedregning() {
        assertMaskineltGrunnlag(
                periode("2008.01.01", "2008.12.31")
                        .med(new Aarstall(2008))
                        .med(new Medregning(kroner(75_000)))
                        .med(Medregningskode.BISTILLING)
        ).isEqualTo(kroner(75_000));

        assertMaskineltGrunnlag(
                periode("2008.01.01", "2008.12.31")
                        .med(new Aarstall(2008))
                        .med(new Medregning(kroner(5_000)))
                        .med(Medregningskode.TILLEGG_ANNEN_ARBGIV)
        ).isEqualTo(kroner(5_000));
    }

    /**
     * Verifiserer at maskinelt grunnlag blir avgrensa til 12G også for medregningar.
     * <p>
     * Merk at dette er oppførsel som er ulik den gamle løysinga, det blir der ikkje foretatt noko form for avgrensing
     * oppover slik at skyhøge medregningar der vil medføre skyhøgt maskinelt grunnlag.
     */
    @Test
    public void skalAvgrenseMaskineltGrunnlagTilOevregrense12GOgsaaForMedregningar() {
        assertMaskineltGrunnlag(
                periode("2007.01.01", "2007.12.31")
                        .med(new Aarstall(2007))
                        .med(new Medregning(kroner(10_000_000)))
                        .med(Medregningskode.BISTILLING)
                        .med(Ordning.SPK)
                        .med(new Grunnbeloep(kroner(50_000)))
        ).isEqualTo(kroner(12 * 50_000));
    }

    /**
     * Verifiserer at avgrensing til øvre grense blir utført før maskinelt grunnlag blir justert i henhold til årsfaktor.
     */
    @Test
    public void skalAvgrenseMaskineltGrunnlagTilAarsfaktorEtterAvgrensingTilOevreloennsgrense() {
        assertMaskineltGrunnlag(
                periode("2005.01.01", "2005.01.31")
                        .med(new Aarstall(2005))
                        .med(new DeltidsjustertLoenn(new Kroner(2_000_000)))
                        .med(new Stillingsprosent(fulltid()))
                        .med(Ordning.SPK)
                        .med(new Grunnbeloep(new Kroner(50_000)))
        ).isEqualTo(
                new Aarsfaktor(31d / 365d).multiply(
                        new Kroner(50_000).multiply(12)
                )
        );
    }

    /**
     * Verifiserer at lønn blir avgrensa til 12G for stillingar tilknytta SPK-ordninga.
     */
    @Test
    public void skalAvgrenseLoennTilOevregrense12GForStillingarTilknyttaSpkOrdninga() {
        assertMaskineltGrunnlag(
                periode("2005.01.01", "2005.12.31")
                        .med(new Aarstall(2005))
                        .med(new DeltidsjustertLoenn(new Kroner(2_000_000)))
                        .med(new Stillingsprosent(fulltid()))
                        .med(Ordning.SPK)
                        .med(new Grunnbeloep(new Kroner(50_000)))
        ).isEqualTo(new Kroner(50_000).multiply(12));
    }

    /**
     * Verifiserer at lønn blir avgrensa til 12G for stillingar tilknytta Opera-ordninga.
     * <p>
     * Merk at dette er kopiering av oppførsel frå den gamle systemløysinga for faktura fastsats, vi har ikkje funne
     * nokon plass det er definert kva som er eller om det er ei øvre grense for Opera-ordninga.
     */
    @Test
    public void skalAvgrenseLoennTilOevregrense12GForStillingarTilknyttaOperaOrdningaFordiViIkkjeHarNokoBedreKildeTilInformasjonOmDetteEnnOppfoerselIGamaltSystem() {
        assertMaskineltGrunnlag(
                periode("2005.01.01", "2005.12.31")
                        .med(new Aarstall(2005))
                        .med(new DeltidsjustertLoenn(new Kroner(3_000_000)))
                        .med(new Stillingsprosent(fulltid()))
                        .med(Ordning.OPERA)
                        .med(new Grunnbeloep(new Kroner(30_000)))
        ).isEqualTo(new Kroner(30_000).multiply(12));
    }

    /**
     * Verifiserer at lønn blir avrensa til 10G for stillingar tilknytta apotekordninga.
     * <p>
     * Merk at dette er ei forenkling, perioda før 1. januar 2008 har andre reglar som ikkje er implementert.
     */
    @Test
    public void skalAvgrenseLoennTilOevregrense10GForStillingarTilknyttaApotekordninga() {
        assertMaskineltGrunnlag(
                periode("2005.01.01", "2005.12.31")
                        .med(new Aarstall(2005))
                        .med(new DeltidsjustertLoenn(new Kroner(2_000_000)))
                        .med(new Stillingsprosent(fulltid()))
                        .med(Stillingskode.K_STIL_APO_FARMASOYT)
                        .med(Ordning.POA)
                        .med(new Grunnbeloep(new Kroner(60_000)))
        ).isEqualTo(new Kroner(60_000).multiply(10));
    }

    @Test
    public void skalIkkjeAvkorteLoennstilleggMedAarsfaktorSidanDetBlirGjortAvLoennstilleggRegelenSjoelv() {
        assertMaskineltGrunnlag(
                periode("2013.01.01", "2013.01.01")
                        .med(new Aarstall(2013))
                        .med(new DeltidsjustertLoenn(new Kroner(0)))
                        .med(new Fastetillegg(new Kroner(365_000)))
        ).isEqualTo(new Kroner(1_000));
    }

    @Test
    public void skalBeregneLoennstilleggViaAnnanRegel() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage(LoennstilleggRegel.class.getSimpleName());

        beregn(
                periode("2013.01.01", "2013.12.31")
                        .med(new Aarstall(2013))
                        .med(new DeltidsjustertLoenn(new Kroner(0)))
                        .uten(LoennstilleggRegel.class)
        ).multiply(0d);
    }

    @Test
    public void skalInkludereMedregningIMaskineltGrunnlag() {
        assertMaskineltGrunnlag(
                periode("2013.01.01", "2013.12.31")
                        .med(new Aarstall(2013))
                        .med(new Medregning(kroner(12_000)))
                        .med(Medregningskode.BISTILLING)
        ).isEqualTo(kroner(12_000));
    }

    @Test
    public void skalBeregneDeltidsjustertLoennViaAnnanRegel() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage(DeltidsjustertLoennRegel.class.getSimpleName());

        beregn(
                periode("2013.01.01", "2013.12.31")
                        .med(new Aarstall(2013))
                        .uten(DeltidsjustertLoennRegel.class)
        ).multiply(0d);
    }

    @Test
    public void skalAvkorteMaskineltGrunnlagUtFraAarsfaktor() {
        assertMaskineltGrunnlag(
                periode("2011.01.01", "2011.01.31")
                        .med(new DeltidsjustertLoenn(new Kroner(365_000)))
                        .med(new Aarstall(2011))
        ).isEqualTo(new Kroner(31_000));
    }

    private static UnderlagsperiodeBuilder periode(final String fraOgMed, final String tilOgMed) {
        return Support.periode(fraOgMed, tilOgMed)
                .med(new MedregningsRegel())
                .med(new MaskineltGrunnlagRegel())
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel())
                .med(new AarsfaktorRegel())
                .med(new DeltidsjustertLoennRegel())
                .med(new LoennstilleggRegel())
                .med(new Stillingsprosent(fulltid()))
                .med(Aksjonskode.ENDRINGSMELDING)
                .med(new OevreLoennsgrenseRegel())
                .med(Premiestatus.valueOf("AAO-07"))
                .med(Ordning.SPK)
                        // Brukar urealistisk høgt beløp for å unngå at det skal påvirke testar som ikkje er fokusert
                        // på å teste beløp som blir påvirka av grunnbeløpet
                .med(new Grunnbeloep(kroner(1_000_000)))
                .med(MinstegrenseRegel.class, new MinstegrenseRegelVersjon1())
                ;
    }

    private static UnderlagsperiodeBuilder periodeFom2016(final String fraOgMed, final String tilOgMed) {
        return Support.periode(fraOgMed, tilOgMed)
                .med(new MedregningsRegel())
                .med(new MaskineltGrunnlagRegel())
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel())
                .med(new AarsfaktorRegel())
                .med(new DeltidsjustertLoennRegel())
                .med(new LoennstilleggRegel())
                .med(new Stillingsprosent(fulltid()))
                .med(Aksjonskode.ENDRINGSMELDING)
                .med(new OevreLoennsgrenseRegel())
                .med(Premiestatus.valueOf("AAO-07"))
                .med(Ordning.SPK)
                .med(new Grunnbeloep(kroner(1_000_000)))
                .med(MinstegrenseRegel.class, new MinstegrenseRegelVersjon2())
                ;
    }

    private AbstractComparableAssert<?, Kroner> assertMaskineltGrunnlag(final UnderlagsperiodeBuilder periode) {
        return assertThat(beregn(periode));
    }

    private Kroner beregn(final UnderlagsperiodeBuilder periode) {
        return periode.bygg().beregn(MaskineltGrunnlagRegel.class);
    }

    private static Prosent fulltid() {
        return new Prosent("100%");
    }
}