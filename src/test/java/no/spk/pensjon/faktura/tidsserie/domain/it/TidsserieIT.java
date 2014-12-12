package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.LoennstilleggRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StatligLoennstrinnperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Observasjonspublikator;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieObservasjon;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
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
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_A;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_B;
import static no.spk.pensjon.faktura.tidsserie.domain.it.TidsserieAvtalebytteIT.feilVissMeirEnnEinObservasjonPrMonth;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.and;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstest av {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieIT {
    @ClassRule
    public static final EksempelDataForStatligeLoennstrinn loennstrinn = new EksempelDataForStatligeLoennstrinn();

    @ClassRule
    public static final EksempelDataForMedlem medlem = new EksempelDataForMedlem();

    private StatligLoennstrinnperiodeOversetter loennstrinnOversetter;

    private Observasjonsperiode observasjonsperiode;

    private Medlemsdata medlemsdata;

    private Tidsserie tidsserie;

    @Before
    public void _before() {
        final Map<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());
        loennstrinnOversetter = new StatligLoennstrinnperiodeOversetter();

        medlemsdata = new Medlemsdata(medlem.toList(), oversettere);

        observasjonsperiode = new Observasjonsperiode(dato("2005.01.01"), dato("2014.12.31"));

        tidsserie = new Tidsserie();
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
    public void skalGenerereForventaAntallObservasjonarTotaltForMedlemmet() {
        assertAlleObservasjonar().hasSize(0
                + 5      // A i 2005
                + 6 * 12 // A i 2006-2011
                + 12     // A i 2012
                + 4      // B i 2012
                + 2 * 12 // B i 2013-2014
        );
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

        assertObservasjonar(2013, STILLING_A).hasSize(0); // ikkje lenger aktiv
        assertObservasjonar(2013, STILLING_B).hasSize(12); // heile året

        assertObservasjonar(2014, STILLING_A).hasSize(0); // juni til november
        assertObservasjonar(2014, STILLING_B).hasSize(12); // heile året
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold {@link EksempelDataForMedlem#STILLING_C}
     */
    @Ignore("Disabla inntil vi får implementert støtte for medregning")
    @Test
    public void skalGenerereXXXObservasjonarForStillingC() {
    }

    private static AbstractComparableAssert<?, Kroner> assertObservasjonAvMaskineltgrunnlag(List<TidsserieObservasjon> observasjonar, Month month) {
        final Optional<TidsserieObservasjon> observasjon = observasjonar.stream()
                .filter(o -> o.tilhoeyrer(month))
                .reduce(feilVissMeirEnnEinObservasjonPrMonth(month, observasjonar));
        assertThat(observasjon.isPresent()).as("eksisterer det ein observasjon for " + month + "?").isTrue();
        return assertThat(observasjon.get().maskineltGrunnlag)
                .as("maskinelt grunnlag for observasjonsunderlag for " + month);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertAlleObservasjonar() {
        return assertObservasjonar(o -> true).as("alle observasjonar i tidsserien");
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
                        Stream.<Tidsperiode<?>>of(
                                new Regelperiode<>(dato("1917.01.01"), empty(), new MaskineltGrunnlagRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new LoennstilleggRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AarsfaktorRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new DeltidsjustertLoennRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AntallDagarRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AarsLengdeRegel())
                        ),
                        Loennstrinnperioder.grupper(
                                Ordning.SPK, loennstrinn.stream()
                                        .filter(loennstrinnOversetter::supports)
                                        .map(loennstrinnOversetter::oversett)
                        )
                                .map(p -> p)
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
