package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.DeltidsjustertLoennRegel;
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
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_A;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.STILLING_B;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.and;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstest av {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieIT {
    @ClassRule
    public static final EksempelDataForLoennstrinn loennstrinn = new EksempelDataForLoennstrinn();

    @ClassRule
    public static final EksempelDataForMedlem medlem = new EksempelDataForMedlem();

    private StatligLoennstrinnperiodeOversetter loennstrinnOversetter;

    private Observasjonsperiode observasjonsperiode;

    private Medlemsdata medlemsdata;

    private Tidsserie tidsserie;

    private ArrayList<TidsserieObservasjon> observasjonar;

    @Before
    public void _before() {
        final Map<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());
        loennstrinnOversetter = new StatligLoennstrinnperiodeOversetter();

        medlemsdata = new Medlemsdata(medlem.toList(), oversettere);

        observasjonsperiode = new Observasjonsperiode(dato("2005.01.01"), dato("2014.12.31"));

        tidsserie = new Tidsserie();

        observasjonar = new ArrayList<>();
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stilling A har
     * rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForKvarObservasjonI2012ForStillingsforholdA() {
        final Predicate<TidsserieObservasjon> predikat = and(
                o -> o.tilhoeyrer(new Aarstall(2012)),
                o -> o.tilhoeyrer(STILLING_A)
        );
        final List<TidsserieObservasjon> observasjonar = genererObservasjonar(predikat);
        assertThat(observasjonar).hasSize(12);
        rangeClosed(Month.JANUARY.getValue(), Month.APRIL.getValue())
                .mapToObj(Month::of)
                .forEach(m -> {
                    assertObservasjonAvMaskineltgrunnlag(observasjonar, m)
                            .isEqualTo(new Kroner(548_200));
                });
        rangeClosed(Month.MAY.getValue(), Month.MAY.getValue())
                .mapToObj(Month::of)
                .forEach(m -> {
                    assertObservasjonAvMaskineltgrunnlag(observasjonar, m)
                            .isEqualTo(new Kroner(558_107));
                });
        rangeClosed(Month.JUNE.getValue(), Month.DECEMBER.getValue())
                .mapToObj(Month::of)
                .forEach(m -> {
                    assertObservasjonAvMaskineltgrunnlag(observasjonar, m)
                            .isEqualTo(new Kroner(275_069));
                });
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stilling B har
     * rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForKvarObservasjonI2012ForStillingsforholdB() {
        final Predicate<TidsserieObservasjon> predikat = and(
                o -> o.tilhoeyrer(new Aarstall(2012)),
                o -> o.tilhoeyrer(STILLING_B)
        );
        final List<TidsserieObservasjon> observasjonar = genererObservasjonar(predikat);
        assertThat(observasjonar).hasSize(4);
        assertThat(observasjonar.get(0).maskineltGrunnlag).isEqualTo(new Kroner(195_148));
        assertThat(observasjonar.get(1).maskineltGrunnlag).isEqualTo(new Kroner(195_148));
        assertThat(observasjonar.get(2).maskineltGrunnlag).isEqualTo(new Kroner(195_148));
        assertThat(observasjonar.get(3).maskineltGrunnlag).isEqualTo(new Kroner(195_148));
    }

    /**
     * Verifiserer at totalt antall observasjonar er lik summen av forventa antall observasjonar pr stillingsforhold.
     */
    @Test
    public void skalGenerere117ObservasjonarTotaltForMedlemmet() {
        assertAlleObservasjonar().hasSize(117);
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold {@link EksempelDataForMedlem#STILLING_A}
     */
    @Test
    public void skalGenerereXXXObservasjonarForStillingA() {
        assertObservasjonar(tilhoeyrer(STILLING_A)).hasSize(5 + 6 * 12 + 12); // 2005, 2006-2011, 2012
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold {@link EksempelDataForMedlem#STILLING_B}
     */
    @Test
    public void skalGenerereXXXObservasjonarForStillingB() {
        assertObservasjonar(tilhoeyrer(STILLING_B)).hasSize(4 + 24); // 2012, 2013-2014
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold {@link EksempelDataForMedlem#STILLING_C}
     */
    @Ignore("Disabla inntil vi får implementert støtte for medregning")
    @Test
    public void skalGenerereXXXObservasjonarForStillingC() {
    }

    private static AbstractComparableAssert<?, Kroner> assertObservasjonAvMaskineltgrunnlag(List<TidsserieObservasjon> observasjonar, Month month) {
        return assertThat(observasjonar.get(month.getValue() - 1).maskineltGrunnlag)
                .as("maskinelt grunnlag for observasjonsunderlag for " + month);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertAlleObservasjonar() {
        return assertObservasjonar(o -> true);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertObservasjonar(
            final Predicate<TidsserieObservasjon> predikat) {
        return assertThat(genererObservasjonar(predikat))
                .as("antall observasjonsperioder i tidsserien for alle stillingar");
    }

    private List<TidsserieObservasjon> genererObservasjonar(final Predicate<TidsserieObservasjon> predikat) {
        generer();
        return this.observasjonar.stream().filter(predikat).collect(toList());
    }

    private void generer() {
        ;

        tidsserie.generer(medlemsdata, observasjonsperiode, observasjonar::add,
                Stream.concat(
                        Stream.<Tidsperiode<?>>of(
                                new Regelperiode<>(dato("1917.01.01"), empty(), new MaskineltGrunnlagRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AarsfaktorRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new DeltidsjustertLoennRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AntallDagarRegel()),
                                new Regelperiode<>(dato("1917.01.01"), empty(), new AarsLengdeRegel())
                        ),
                        Loennstrinnperioder.grupper(
                                loennstrinn.stream()
                                        .filter(loennstrinnOversetter::supports)
                                        .map(loennstrinnOversetter::oversett)
                        )
                                .map(p -> p)
                )
        );
    }

    private Predicate<TidsserieObservasjon> tilhoeyrer(final StillingsforholdId stillingsforhold) {
        return (TidsserieObservasjon o) -> o.tilhoeyrer(stillingsforhold);
    }
}
