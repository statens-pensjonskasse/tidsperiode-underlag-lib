package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.NYTILGANG;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.SLUTTMELDING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.BISTILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.TILLEGG_ANNEN_ARBGIV;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode.medregning;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.ObjectMother.eiMedregning;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.PeriodiserMedlemHelper.assertStillingsforholdperioder;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.PeriodiserMedlemHelper.periode;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.PeriodiserMedlemHelper.periodiser;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import org.junit.Test;

/**
 * Enheitstestar for {@link PeriodiserMedlem}.
 *
 * @author Tarjei Skorgenes
 */
public class PeriodiserMedlemTest {
    private static final Medregning MEDREGNING = new Medregning(kroner(100_000));
    private static final StillingsforholdId STILLING_1 = new StillingsforholdId(1L);
    private static final StillingsforholdId STILLING_2 = new StillingsforholdId(2L);
    private static final StillingsforholdId STILLING_3 = new StillingsforholdId(3L);

    @Test
    public void skalDanneMedlemsperioderUtanKoblingTilStillingsforholdForPerioderMedlemmetIkkjeHarAktiveStillingar() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(dato("2005.08.15"), of(dato("2012.06.30")))
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2005.08.15"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_1)
                                ,
                                new Stillingsendring()
                                        .aksjonsdato(dato("2012.06.30"))
                                        .aksjonskode(SLUTTMELDING)
                                        .stillingsforhold(STILLING_1)
                        ),
                new StillingsforholdPeriode(dato("2012.09.03"), empty())
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2012.09.03"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_2)
                        )
        );
        assertThat(perioder).hasSize(3);

        final Medlemsperiode inaktiv = perioder.get(1);
        assertThat(inaktiv.fraOgMed()).isEqualTo(dato("2012.07.01"));
        assertThat(inaktiv.tilOgMed()).isEqualTo(of(dato("2012.09.02")));

        assertThat(inaktiv.stillingsforhold().count())
                .as("antall stillingsforholdperioder for inaktiv medlemsperiode")
                .isEqualTo(0);
    }

    @Test
    public void skalReturnereTomVerdiVissMedlemmetIkkjeHarNokonMedregningEllerStillingsendringar() {
        assertThat(new PeriodiserMedlem().periodiser()).isEqualTo(empty());
    }

    @Test
    public void skalReturnereEiLøpandePeriodeDersomMedlemmetKunHarEiStillingsendringSomIkkjeErEiSluttmelding() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(dato("1975.01.04"), empty())
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("1975.01.04"))
                        )
        );
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).fraOgMed()).isEqualTo(dato("1975.01.04"));
        assertThat(perioder.get(0).tilOgMed()).isEqualTo(empty());
    }

    @Test
    public void skalReturnereEiLøpandePeriodeDersomMedlemmetKunHarEiLøpandeMedregning() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(
                        eiMedregning()
                                .fraOgMed(dato("2000.01.01"))
                                .loepende()
                                .bygg()
                )
        );
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).fraOgMed()).isEqualTo(dato("2000.01.01"));
        assertThat(perioder.get(0).tilOgMed()).isEqualTo(empty());
    }

    /**
     * Verifiserer at siste medlemsperiode startar dagen etter medregningas sluttdato sidan det først
     * er då medlemstilstanden har blitt endra.
     * <p>
     * Hensikta med dette er å sikre at vi ikkje splittar medlemsperioda og genererer ei en-dag lang medlemsperiode på
     * sjølve sluttdatoen. Det er funksjonelt sett relativt uproblematisk men også 100% overflødig.
     */
    @Test
    public void skalStarteNyPeriodeDagenEtterMedregningsperiodasTilOgMedDato() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(
                        eiMedregning()
                                .fraOgMed(dato("2005.04.03"))
                                .tilOgMed(of(dato("2015.04.30")))
                                .stillingsforhold(STILLING_1)
                                .bygg()
                )
                ,
                new StillingsforholdPeriode(
                        eiMedregning()
                                .fraOgMed(dato("2016.01.01"))
                                .loepende()
                                .stillingsforhold(STILLING_2)
                                .bygg()
                )
        );
        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(1).fraOgMed()).isEqualTo(dato("2015.05.01"));
    }

    /**
     * Verifiserer at siste medlemsperiode startar dagen etter sluttmeldingas aksjonsdato sidan det først
     * er då medlemstilstanden har blitt endra.
     * <p>
     * Hensikta med dette er å sikre at vi ikkje splittar medlemsperioda og genererer ei en-dag lang medlemsperiode på
     * sjølve aksjonsdatoen for sluttmeldinga. Det er funksjonelt sett relativt uproblematisk men også 100% overflødig.
     */
    @Test
    public void skalStarteNyPeriodeDagenEtterStilling2ErAvslutta() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(dato("2007.01.03"), empty())
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2007.01.03"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_1)
                        ),
                new StillingsforholdPeriode(dato("2010.01.01"), of(dato("2010.02.28")))
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2010.01.01"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_2),
                                new Stillingsendring()
                                        .aksjonsdato(dato("2010.02.28"))
                                        .aksjonskode(SLUTTMELDING)
                                        .stillingsforhold(STILLING_2)
                        )
        );
        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(2).fraOgMed()).isEqualTo(dato("2010.03.01"));
    }

    @Test
    public void skalKobleSamanKvarMedlemsperiodeMedOverlappandeStillingsforholdPerioder() {
        final List<Medlemsperiode> perioder = periodiser(
                new StillingsforholdPeriode(dato("2007.01.03"), empty())
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2007.01.03"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_1)
                        ),
                new StillingsforholdPeriode(
                        eiMedregning()
                                .fraOgMed(dato("2009.06.01"))
                                .tilOgMed(of(dato("2016.12.31")))
                                .stillingsforhold(STILLING_3)
                                .bygg()
                ),
                new StillingsforholdPeriode(dato("2010.01.01"), of(dato("2010.02.28")))
                        .leggTilOverlappendeStillingsendringer(
                                new Stillingsendring()
                                        .aksjonsdato(dato("2010.01.01"))
                                        .aksjonskode(NYTILGANG)
                                        .stillingsforhold(STILLING_2),
                                new Stillingsendring()
                                        .aksjonsdato(dato("2010.02.28"))
                                        .aksjonskode(SLUTTMELDING)
                                        .stillingsforhold(STILLING_2)
                        )
        );
        assertThat(perioder).hasSize(5);
        assertStillingsforholdperioder(perioder, periode("2007.01.03", "2009.05.31")).hasSize(1);
        assertStillingsforholdperioder(perioder, periode("2009.06.01", "2009.12.31")).hasSize(2);
        assertStillingsforholdperioder(perioder, periode("2010.01.01", "2010.02.28")).hasSize(3);
        assertStillingsforholdperioder(perioder, periode("2010.01.01", "2010.02.28")).hasSize(3);
        assertStillingsforholdperioder(perioder, periode("2010.03.01", "2016.12.31")).hasSize(2);
        assertStillingsforholdperioder(perioder, periode("2017.01.01", /**/ empty())).hasSize(1);
    }
}