package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.LoennstilleggRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MedregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MinstegrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.OevreLoennsgrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.OmregningsperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StatligLoennstrinnperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Observasjonspublikator;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieObservasjon;
import no.spk.pensjon.faktura.tidsserie.storage.csv.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.storage.csv.MedregningsOversetter;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StillingsendringOversetter;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_A;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_B;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_C;
import static no.spk.pensjon.faktura.tidsserie.domain.it.TidsserieAvtalebytteIT.feilVissMeirEnnEinObservasjonPrMonth;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.and;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Integrasjonstest av {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieIT {
    @ClassRule
    public static final EksempelDataForStatligeLoennstrinn loennstrinn = new EksempelDataForStatligeLoennstrinn();

    @ClassRule
    public static final EksempelDataForOmregningsperioder omregningsperioder = new EksempelDataForOmregningsperioder();

    @ClassRule
    public static final EksempelDataForMedlem medlem = new EksempelDataForMedlem();

    private OmregningsperiodeOversetter omregningOversetter = new OmregningsperiodeOversetter();

    private StatligLoennstrinnperiodeOversetter loennstrinnOversetter;

    private Observasjonsperiode observasjonsperiode;

    private Medlemsdata medlemsdata;

    private Tidsserie tidsserie;

    @Before
    public void _before() {
        final Map<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());
        oversettere.put(Medregningsperiode.class, new MedregningsOversetter());
        loennstrinnOversetter = new StatligLoennstrinnperiodeOversetter();

        medlemsdata = new Medlemsdata(medlem.toList(), oversettere);

        observasjonsperiode = new Observasjonsperiode(dato("2005.01.01"), dato("2014.12.31"));

        tidsserie = new Tidsserie();
        tidsserie.overstyr(avtale -> Stream.of(new Avtaleversjon(dato("1917.01.01"), empty(), avtale, Premiestatus.valueOf("AAO-10"))));
    }

    /**
     * Verifiserer at funksjonstillegg blir inkludert ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereFunksjonstilleggIMaskineltGrunnlagForKvarObservasjonAv2006FraOgMedFebruar() {
        final int aar = 2014;
        assertThat(generer(STILLING_B, aar)).hasSize(12);
        assertObservasjonAvMaskineltgrunnlag(aar, JANUARY, STILLING_B).isEqualTo(kroner(627_100 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, FEBRUARY, STILLING_B).isEqualTo(kroner(627_100 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, MARCH, STILLING_B).isEqualTo(kroner(627_100 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, APRIL, STILLING_B).isEqualTo(kroner(627_100 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, MAY, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, JUNE, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, JULY, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, AUGUST, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, SEPTEMBER, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, OCTOBER, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, NOVEMBER, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
        assertObservasjonAvMaskineltgrunnlag(aar, DECEMBER, STILLING_B).isEqualTo(kroner(635_423 + 12_000));
    }

    /**
     * Verifiserer at faste tillegg blir inkludert ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereFasteTilleggIMaskineltGrunnlagForKvarObservasjonAv2006FraOgMedFebruar() {
        final int aar = 2006;
        assertThat(generer(STILLING_A, aar)).hasSize(12);
        assertObservasjonAvMaskineltgrunnlag(aar, JANUARY, STILLING_A).isEqualTo(kroner(372_000));
        assertObservasjonAvMaskineltgrunnlag(aar, FEBRUARY, STILLING_A).isEqualTo(kroner(387_059));
        assertObservasjonAvMaskineltgrunnlag(aar, MARCH, STILLING_A).isEqualTo(kroner(397_407));
        assertObservasjonAvMaskineltgrunnlag(aar, APRIL, STILLING_A).isEqualTo(kroner(383_665));
        assertObservasjonAvMaskineltgrunnlag(aar, MAY, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, JUNE, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, JULY, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, AUGUST, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, SEPTEMBER, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, OCTOBER, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, NOVEMBER, STILLING_A).isEqualTo(kroner(397_855));
        assertObservasjonAvMaskineltgrunnlag(aar, DECEMBER, STILLING_A).isEqualTo(kroner(397_855));
    }

    /**
     * Verifiserer at variable tillegg blir inkludert ved beregning av maskinelt grunnlag.
     */
    @Test
    public void skalInkludereVariableTilleggIMaskineltGrunnlagForKvarObservasjonAv2006FraOgMedFebruar() {
        final int aar = 2007;
        assertThat(generer(STILLING_A, aar)).hasSize(12);
        assertObservasjonAvMaskineltgrunnlag(aar, JANUARY, STILLING_A).isEqualTo(kroner(418_100));
        assertObservasjonAvMaskineltgrunnlag(aar, FEBRUARY, STILLING_A).isEqualTo(kroner(418_100));
        assertObservasjonAvMaskineltgrunnlag(aar, MARCH, STILLING_A).isEqualTo(kroner(418_100));
        assertObservasjonAvMaskineltgrunnlag(aar, APRIL, STILLING_A).isEqualTo(kroner(418_100));
        assertObservasjonAvMaskineltgrunnlag(aar, MAY, STILLING_A).isEqualTo(kroner(421_725));
        assertObservasjonAvMaskineltgrunnlag(aar, JUNE, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, JULY, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, AUGUST, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, SEPTEMBER, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, OCTOBER, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, NOVEMBER, STILLING_A).isEqualTo(kroner(430_461));
        assertObservasjonAvMaskineltgrunnlag(aar, DECEMBER, STILLING_A).isEqualTo(kroner(430_461));
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stilling A har
     * rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForKvarObservasjonI2012ForStillingsforholdA() {
        assertThat(generer(STILLING_A, 2012)).hasSize(12);
        assertObservasjonAvMaskineltgrunnlag(2012, JANUARY, STILLING_A).isEqualTo(kroner(548_200));
        assertObservasjonAvMaskineltgrunnlag(2012, FEBRUARY, STILLING_A).isEqualTo(kroner(548_200));
        assertObservasjonAvMaskineltgrunnlag(2012, MARCH, STILLING_A).isEqualTo(kroner(548_200));
        assertObservasjonAvMaskineltgrunnlag(2012, APRIL, STILLING_A).isEqualTo(kroner(548_200));
        assertObservasjonAvMaskineltgrunnlag(2012, MAY, STILLING_A).isEqualTo(kroner(558_107));
        assertObservasjonAvMaskineltgrunnlag(2012, JUNE, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, JULY, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, AUGUST, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, SEPTEMBER, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, OCTOBER, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, NOVEMBER, STILLING_A).isEqualTo(kroner(275_069));
        assertObservasjonAvMaskineltgrunnlag(2012, DECEMBER, STILLING_A).isEqualTo(kroner(275_069));
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stilling B har
     * rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForKvarObservasjonI2012ForStillingsforholdB() {
        assertThat(generer(STILLING_B, 2012)).hasSize(4);
        assertObservasjonAvMaskineltgrunnlag(2012, SEPTEMBER, STILLING_B).isEqualTo(kroner(195_148));
        assertObservasjonAvMaskineltgrunnlag(2012, OCTOBER, STILLING_B).isEqualTo(kroner(195_148));
        assertObservasjonAvMaskineltgrunnlag(2012, NOVEMBER, STILLING_B).isEqualTo(kroner(195_148));
        assertObservasjonAvMaskineltgrunnlag(2012, DECEMBER, STILLING_B).isEqualTo(kroner(195_148));
    }

    /**
     * Verifiserer at totalt antall observasjonar er lik summen av forventa antall observasjonar pr stillingsforhold.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForKvarObservasjonI2012ForStillingsforholdC() {
        assertThat(generer(STILLING_C, 2012)).hasSize(6);
        final Kroner expected = new Aarsfaktor(184d / 366d).multiply(kroner(109_600));
        assertObservasjonAvMaskineltgrunnlag(2012, JULY, STILLING_C).isEqualTo(expected);
        assertObservasjonAvMaskineltgrunnlag(2012, AUGUST, STILLING_C).isEqualTo(expected);
        assertObservasjonAvMaskineltgrunnlag(2012, SEPTEMBER, STILLING_C).isEqualTo(expected);
        assertObservasjonAvMaskineltgrunnlag(2012, OCTOBER, STILLING_C).isEqualTo(expected);
        assertObservasjonAvMaskineltgrunnlag(2012, NOVEMBER, STILLING_C).isEqualTo(expected);
        assertObservasjonAvMaskineltgrunnlag(2012, DECEMBER, STILLING_C).isEqualTo(expected);
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold
     * {@link EksempelDataForMedlemMedAvtalebytte#STILLING} pr avtale pr år er som forventa.
     */
    @Test
    public void skalGenerereForventaAntallObservasjonarPrForStillingaPrÅr() {
        assertObservasjonar(2005, STILLING_A).hasSize(5); // juni til november
        assertObservasjonar(2005, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2006, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2006, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2007, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2007, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2008, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2008, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2009, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2009, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2010, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2010, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2011, STILLING_A).hasSize(12); // juni til november
        assertObservasjonar(2011, STILLING_B).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(2012, STILLING_A).hasSize(12); // januar til desember
        assertObservasjonar(2012, STILLING_B).hasSize(4); // september til desember
        assertObservasjonar(2012, STILLING_C).hasSize(6); // juli til desember

        assertObservasjonar(2013, STILLING_A).hasSize(0); // ikkje lenger aktiv
        assertObservasjonar(2013, STILLING_B).hasSize(12); // heile året
        assertObservasjonar(2013, STILLING_C).hasSize(12); // heile året

        assertObservasjonar(2014, STILLING_A).hasSize(0); // juni til november
        assertObservasjonar(2014, STILLING_B).hasSize(12); // heile året
        assertObservasjonar(2014, STILLING_C).hasSize(12); // heile året
    }

    /**
     * Verifiserer at antall årsverk er korrekt beregna for stillingsforhold A for kvar observasjon.
     */
    @Test
    public void skalBeregneForventaAntallAarsverkPrStillingPrAar() {
        final Offset<Double> presisjon = offset(0.0001);

        final Prosent aarsverkStart = prosent("38.082%");
        assertObservasjonAvAarsverk(2005, AUGUST, STILLING_A).isEqualTo(aarsverkStart.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2005, SEPTEMBER, STILLING_A).isEqualTo(aarsverkStart.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2005, OCTOBER, STILLING_A).isEqualTo(aarsverkStart.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2005, NOVEMBER, STILLING_A).isEqualTo(aarsverkStart.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2005, DECEMBER, STILLING_A).isEqualTo(aarsverkStart.toDouble(), presisjon);

        rangeClosed(2006, 2007)
                .forEach(year -> {
                    asList(Month.values()).stream().forEach(month -> {
                        assertObservasjonAvAarsverk(year, month, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
                    });
                });
        assertObservasjonAvAarsverk(2008, JANUARY, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2008, NOVEMBER, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);

        // Nedgang til 50% fom 1. desember 2008
        assertObservasjonAvAarsverk(2008, DECEMBER, STILLING_A).isEqualTo(prosent("95.765%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2009, JANUARY, STILLING_A).isEqualTo(prosent("50%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2009, FEBRUARY, STILLING_A).isEqualTo(prosent("50%").toDouble(), presisjon);

        // Oppgang til 100% fom 1. mars 2009
        assertObservasjonAvAarsverk(2009, MARCH, STILLING_A).isEqualTo(prosent("91.918%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2009, DECEMBER, STILLING_A).isEqualTo(prosent("91.918%").toDouble(), presisjon);
        rangeClosed(2010, 2011)
                .forEach(year -> {
                    asList(Month.values()).stream().forEach(month -> {
                        assertObservasjonAvAarsverk(year, month, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
                    });
                });

        assertObservasjonAvAarsverk(2012, JANUARY, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, FEBRUARY, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, MARCH, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, APRIL, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, MAY, STILLING_A).isEqualTo(prosent("100%").toDouble(), presisjon);

        final Prosent aarsverkSlutt = prosent("49.727%");
        assertObservasjonAvAarsverk(2012, JUNE, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, JULY, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, AUGUST, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, SEPTEMBER, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, OCTOBER, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, NOVEMBER, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
        assertObservasjonAvAarsverk(2012, DECEMBER, STILLING_A).isEqualTo(aarsverkSlutt.toDouble(), presisjon);
    }

    private AbstractDoubleAssert assertObservasjonAvAarsverk(int aarstall, Month month, StillingsforholdId stilling) {
        final Optional<Aarsverk> aarsverk = observasjonFor(generer(stilling, aarstall), month).get().maaling(Aarsverk.class);
        assertThat(aarsverk.isPresent()).as("er det gjort ei måling av årsverk for " + month + " " + aarstall + "?").isTrue();
        return assertThat(aarsverk.get().tilProsent().toDouble())
                .as("årsverk grunnlag for observasjonsunderlag for " + month + " " + aarstall);
    }

    private static AbstractComparableAssert<?, Kroner> assertObservasjonAvMaskineltgrunnlag(
            final List<TidsserieObservasjon> observasjonar, final Month month) {
        return assertThat(observasjonFor(observasjonar, month).get().maskineltGrunnlag)
                .as("maskinelt grunnlag for observasjonsunderlag for " + month);
    }

    private static Optional<TidsserieObservasjon> observasjonFor(List<TidsserieObservasjon> observasjonar, Month month) {
        final Optional<TidsserieObservasjon> observasjon = observasjonar.stream()
                .filter(o -> o.tilhoeyrer(month))
                .reduce(feilVissMeirEnnEinObservasjonPrMonth(month, observasjonar));
        assertThat(observasjon.isPresent()).as("eksisterer det ein observasjon for " + month + "?").isTrue();
        return observasjon;
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertObservasjonar(
            final int aarstall, final StillingsforholdId stilling) {
        return assertObservasjonar(tilhoeyrer(aarstall, stilling))
                .as("observasjonar for " + stilling + " i år " + aarstall);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertObservasjonar(
            final Predicate<TidsserieObservasjon> predikat) {
        return assertThat(genererObservasjonar(predikat));
    }

    private AbstractComparableAssert<?, Kroner> assertObservasjonAvMaskineltgrunnlag(
            final int aarstall, final Month month, final StillingsforholdId stilling) {
        return assertObservasjonAvMaskineltgrunnlag(generer(stilling, aarstall), month);
    }

    private List<TidsserieObservasjon> generer(final StillingsforholdId stilling, final int aarstall) {
        return genererObservasjonar(tilhoeyrer(aarstall, stilling));
    }

    private List<TidsserieObservasjon> genererObservasjonar(final Predicate<TidsserieObservasjon> predikat) {
        return generer().stream().filter(predikat).collect(toList());
    }

    private List<TidsserieObservasjon> generer() {
        final List<TidsserieObservasjon> observasjonar = new ArrayList<>();
        final Observasjonspublikator<TidsserieObservasjon> publikator = observasjonar::add;
        tidsserie.generer(medlemsdata, observasjonsperiode, publikator,
                Stream.concat(
                        Stream.concat(
                                Stream.<Tidsperiode<?>>of(
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new MaskineltGrunnlagRegel()),
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new LoennstilleggRegel()),
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new AarsfaktorRegel()),
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new DeltidsjustertLoennRegel()),
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new AntallDagarRegel()),
                                        new Regelperiode<>(dato("1917.01.01"), empty(), new AarsLengdeRegel()),
                                        new Regelperiode<>(dato("2000.01.01"), empty(), new OevreLoennsgrenseRegel()),
                                        new Regelperiode<>(dato("2000.01.01"), empty(), new MedregningsRegel()),
                                        new Regelperiode<>(dato("2000.01.01"), empty(), new MinstegrenseRegel()),
                                        new Regelperiode<>(dato("2000.01.01"), empty(), new AarsverkRegel())
                                ),
                                Loennstrinnperioder.grupper(
                                        Ordning.SPK, loennstrinn.stream()
                                                .filter(loennstrinnOversetter::supports)
                                                .map(loennstrinnOversetter::oversett)
                                )
                                        .map(p -> p)
                        ),
                        omregningsperioder
                                .stream()
                                .filter(omregningOversetter::supports)
                                .map(omregningOversetter::oversett)
                )
        );
        return observasjonar;
    }

    private Predicate<TidsserieObservasjon> tilhoeyrer(int aarstall, final StillingsforholdId stilling) {
        return and(
                o -> o.tilhoeyrer(new Aarstall(aarstall)),
                o -> o.tilhoeyrer(stilling)
        );
    }
}
