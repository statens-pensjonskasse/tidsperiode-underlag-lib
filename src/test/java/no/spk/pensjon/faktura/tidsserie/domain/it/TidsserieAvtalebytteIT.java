package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.LoennstilleggRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MedregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MinstegrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.internal.OevreLoennsgrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.OmregningsperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StatligLoennstrinnperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StillingsendringOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Observasjonspublikator;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieObservasjon;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
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
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlemMedAvtalebytte.A;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlemMedAvtalebytte.B;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlemMedAvtalebytte.C;
import static no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlemMedAvtalebytte.STILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.and;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstest av {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}, fokusert på handtering av
 * stillingsforhold som har vore gjennom eit eller fleire avtalebytte.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieAvtalebytteIT {
    @ClassRule
    public static final EksempelDataForStatligeLoennstrinn loennstrinn = new EksempelDataForStatligeLoennstrinn();

    @ClassRule
    public static final EksempelDataForOmregningsperioder omregningsperioder = new EksempelDataForOmregningsperioder();

    @ClassRule
    public static final EksempelDataForMedlemMedAvtalebytte medlem = new EksempelDataForMedlemMedAvtalebytte();

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
        loennstrinnOversetter = new StatligLoennstrinnperiodeOversetter();

        medlemsdata = new Medlemsdata(medlem.toList(), oversettere);

        observasjonsperiode = new Observasjonsperiode(dato("2005.01.01"), dato("2014.12.31"));

        tidsserie = new Tidsserie();
        tidsserie.overstyr(avtale -> {
            return Stream.of(new Avtaleversjon(dato("1917.01.01"), empty(), avtale, Premiestatus.valueOf("AAO-10")));
        });
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stillinga på
     * avtale A har rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForAvtaleAPrAar() {
        assertObservasjonAvMaskineltgrunnlag(2012, JUNE, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, JULY, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, AUGUST, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, SEPTEMBER, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, OCTOBER, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, NOVEMBER, A).isEqualTo(kroner(218_268));
        assertObservasjonAvMaskineltgrunnlag(2012, DECEMBER, A).isEqualTo(kroner(186_650));
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stillinga på
     * avtale B har rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForAvtaleBPrAar() {
        assertObservasjonAvMaskineltgrunnlag(2012, DECEMBER, B).isEqualTo(kroner(31_618));

        assertObservasjonAvMaskineltgrunnlag(2013, JANUARY, B).isEqualTo(kroner(373_300));
        assertObservasjonAvMaskineltgrunnlag(2013, FEBRUARY, B).isEqualTo(kroner(373_300));
        assertObservasjonAvMaskineltgrunnlag(2013, MARCH, B).isEqualTo(kroner(313_981));
        assertObservasjonAvMaskineltgrunnlag(2013, APRIL, B).isEqualTo(kroner(313_981));
        assertObservasjonAvMaskineltgrunnlag(2013, MAY, B).isEqualTo(kroner(316_398));
        assertObservasjonAvMaskineltgrunnlag(2013, JUNE, B).isEqualTo(kroner(316_398));
        assertObservasjonAvMaskineltgrunnlag(2013, JULY, B).isEqualTo(kroner(164_035));
        assertObservasjonAvMaskineltgrunnlag(2013, AUGUST, B).isEqualTo(kroner(164_035));
        assertObservasjonAvMaskineltgrunnlag(2013, SEPTEMBER, B).isEqualTo(kroner(164_035));
        assertObservasjonAvMaskineltgrunnlag(2013, OCTOBER, B).isEqualTo(kroner(164_035));
        assertObservasjonAvMaskineltgrunnlag(2013, NOVEMBER, B).isEqualTo(kroner(164_035));
        assertObservasjonAvMaskineltgrunnlag(2013, DECEMBER, B).isEqualTo(kroner(164_035));
    }

    /**
     * Verifiserer at observasjonen av kvart av dei 12 observasjonsunderlaga i 2012 for tidsserien til stillinga på
     * avtale C har rett verdi for maskinelt grunnlag.
     */
    @Test
    public void skalBeregneForventaMaskineltGrunnlagForAvtaleCPrAar() {
        assertObservasjonAvMaskineltgrunnlag(2013, JULY, C).isEqualTo(kroner(152_362));
        assertObservasjonAvMaskineltgrunnlag(2013, AUGUST, C).isEqualTo(kroner(152_362));
        assertObservasjonAvMaskineltgrunnlag(2013, SEPTEMBER, C).isEqualTo(kroner(153_913));
        assertObservasjonAvMaskineltgrunnlag(2013, OCTOBER, C).isEqualTo(kroner(153_913));
        assertObservasjonAvMaskineltgrunnlag(2013, NOVEMBER, C).isEqualTo(kroner(153_913));
        assertObservasjonAvMaskineltgrunnlag(2013, DECEMBER, C).isEqualTo(kroner(153_913));

        assertObservasjonAvMaskineltgrunnlag(2014, JANUARY, C).isEqualTo(kroner(378_345));
        assertObservasjonAvMaskineltgrunnlag(2014, FEBRUARY, C).isEqualTo(kroner(378_345));
        assertObservasjonAvMaskineltgrunnlag(2014, MARCH, C).isEqualTo(kroner(378_345));
        assertObservasjonAvMaskineltgrunnlag(2014, APRIL, C).isEqualTo(kroner(378_345));
        assertObservasjonAvMaskineltgrunnlag(2014, MAY, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, JUNE, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, JULY, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, AUGUST, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, SEPTEMBER, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, OCTOBER, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, NOVEMBER, C).isEqualTo(kroner(383_715));
        assertObservasjonAvMaskineltgrunnlag(2014, DECEMBER, C).isEqualTo(kroner(383_715));
    }

    /**
     * Verifiserer at totalt antall observasjonar er lik summen av forventa antall observasjonar pr stillingsforhold.
     */
    @Test
    public void skalGenerereForventaAntallObservasjonarTotaltForMedlemmet() {
        assertAlleObservasjonar().hasSize(0
                + (7 + 0 + 0) /* A i 2012-2014 */
                + (1 + 12 + 0) /* B i 2012-2014 */
                + (0 + 6 + 12) /* C i 2012-2014 */
        );
    }

    /**
     * Verifiserer at antall observasjonar som blir generert for stillingsforhold
     * {@link EksempelDataForMedlemMedAvtalebytte#STILLING} pr avtale pr år er som forventa.
     */
    @Test
    public void skalGenerereForventaAntallObservasjonarPrForStillingaPrÅr() {
        assertObservasjonar(tilhoeyrer(2012, A)).hasSize(7); // juni til november
        assertObservasjonar(tilhoeyrer(2012, B)).hasSize(1); // desember
        assertObservasjonar(tilhoeyrer(2012, C)).hasSize(0); // ikkje aktiv enda

        assertObservasjonar(tilhoeyrer(2013, A)).hasSize(0);  // ikkje lenger aktiv
        assertObservasjonar(tilhoeyrer(2013, B)).hasSize(12); // januar til desember
        assertObservasjonar(tilhoeyrer(2013, C)).hasSize(6);  // juli til desember

        assertObservasjonar(tilhoeyrer(2014, A)).hasSize(0);  // ikkje lenger aktiv
        assertObservasjonar(tilhoeyrer(2014, B)).hasSize(0);  // ikkje lenger aktiv
        assertObservasjonar(tilhoeyrer(2014, C)).hasSize(12); // heile året
    }

    private AbstractComparableAssert<?, Kroner> assertObservasjonAvMaskineltgrunnlag(final int aarstall, final Month month, final AvtaleId avtale) {
        final List<TidsserieObservasjon> observasjonar = genererObservasjonar(
                tilhoeyrer(aarstall, avtale)
        );
        final Optional<TidsserieObservasjon> observasjon = observasjonar.stream()
                .filter(o -> o.tilhoeyrer(month))
                .reduce(feilVissMeirEnnEinObservasjonPrMonth(month, observasjonar));
        assertThat(observasjon.isPresent()).as("eksisterer det ein observasjon for " + month + "?").isTrue();
        return assertThat(observasjon.get().maskineltGrunnlag)
                .as("maskinelt grunnlag for " + avtale + " beregna ut frå observasjonsunderlag for " + month + " " + aarstall);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertAlleObservasjonar() {
        return assertObservasjonar(o -> true);
    }

    private AbstractListAssert<?, ? extends List<TidsserieObservasjon>, TidsserieObservasjon> assertObservasjonar(
            final Predicate<TidsserieObservasjon> predikat) {
        return assertThat(genererObservasjonar(predikat))
                .as("antall observasjonsperioder i tidsserien som matchar filteret");
    }

    private static Predicate<TidsserieObservasjon> tilhoeyrer(final int aarstall, final AvtaleId... avtale) {
        return and(
                o -> o.tilhoeyrer(new Aarstall(aarstall)),
                o -> o.tilhoeyrer(STILLING),
                tilhoeyrerAvtalar(avtale)
        );
    }

    private static Predicate<TidsserieObservasjon> tilhoeyrerAvtalar(final AvtaleId... avtale) {
        return and(
                asList(avtale)
                        .stream()
                        .map(TidsserieAvtalebytteIT::tilhoeyrerAvtale)
                        .toArray((IntFunction<Predicate<TidsserieObservasjon>[]>) (value) -> new Predicate[value])
        );
    }

    private static Predicate<TidsserieObservasjon> tilhoeyrerAvtale(final AvtaleId avtale) {
        return (TidsserieObservasjon o) -> o.tilhoeyrer(avtale);
    }

    private List<TidsserieObservasjon> genererObservasjonar(final Predicate<TidsserieObservasjon> predikat) {
        return generer().stream().filter(predikat).collect(toList());
    }

    private List<TidsserieObservasjon> generer() {
        final ArrayList<TidsserieObservasjon> observasjonar = new ArrayList<>();
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

    static BinaryOperator<TidsserieObservasjon> feilVissMeirEnnEinObservasjonPrMonth(Month month, List<TidsserieObservasjon> observasjonar) {
        return (a, b) -> {
            final StringBuilder builder = new StringBuilder();
            builder.append("Det eksisterer meir enn ein observasjon for ");
            builder.append(month);
            builder.append(" blant observasjonane for tidsserien.\n");
            builder.append("Observasjonar: \n");
            observasjonar.forEach(o -> builder.append("- ").append(o).append('\n'));
            throw new AssertionError(builder.toString());
        };
    }
}
