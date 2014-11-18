package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StandardTidsserieAnnotering;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StillingsforholdUnderlagCallback;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade.Annoteringsstrategi;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.valueOf;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.and;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.assertUnderlagsperioder;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.assertUnderlagsperioderUtanKoblingTil;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.assertVerdiFraUnderlagsperioder;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.harAnnotasjon;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.paakrevdAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Integrasjonstestar for something.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieUnderlagFacadeIT {
    private static final StillingsforholdId STILLINGSFORHOLD_A = valueOf(999999999999L);

    private static final StillingsforholdId STILLINGSFORHOLD_B = valueOf(888888888888L);

    private static final StillingsforholdId STILLINGSFORHOLD_C = valueOf(7777777777777L);

    @ClassRule
    public static EksempelDataForMedlem data = new EksempelDataForMedlem();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private Annoteringsstrategi annotator = new StandardTidsserieAnnotering();

    private Map<Class<?>, MedlemsdataOversetter<?>> oversettere;

    private TidsserieUnderlagFacade fasade;

    private Medlemsdata medlem;

    @Before
    public void _before() throws IOException {
        oversettere = new HashMap<>();
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());

        medlem = new Medlemsdata(data.toList(), oversettere);

        fasade = new TidsserieUnderlagFacade();
        fasade.endreAnnoteringsstrategi(annotator);
    }

    /**
     * Verifiserer at dersom annoteringa av underlagsperioder feilar, fører det til at periodiseringa av medlemmet sine
     * stillingsforhold blir umiddelbart avbrutt utan å gå vidare til medlemmets gjennståande stillingsforhold.
     */
    @Test
    public void skalAvbrytePeriodiseringaUmiddelbartDersomFeilBlirKastaFraaAnnoteringa() {
        final Annoteringsstrategi annotator = mock(Annoteringsstrategi.class);

        final RuntimeException expected = new NullPointerException();
        doAnswer(a -> {
            throw expected;
        }).when(annotator).annoter(any(Underlag.class), any(Underlagsperiode.class));
        fasade.endreAnnoteringsstrategi(annotator);

        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        try {
            prosesser(underlagene::put, standardperiode());
            failBecauseExceptionWasNotThrown(expected.getClass());
        } catch (final NullPointerException e) {
            assertThat(e).as("feilen som fasada kastar dersom annoteringa feilar").isSameAs(expected);
        }
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med stillingsprosent henta frå overlappande
     * stillingsforholdperiodes gjeldande stillingsendring.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedStillingsprosentFraStillingsforholdperiode() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioder(
                underlagene.values(),
                harAnnotasjon(Stillingsprosent.class).negate()
        ).isEmpty();

        assertAnnotasjonFraUnderlagsperioder(underlagene.values(), Stillingsprosent.class)
                .containsOnlyElementsOf(
                        fraMedlemsdata(
                                stillingsendringOversetter(),
                                Stillingsendring::stillingsprosent
                        )
                );
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med lønnstrinn henta frå overlappande
     * stillingsforholdperiodes gjeldande stillingsendring for stillingsforholdet
     * som blir innrapportert med lønnstrinn.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedLoennstrinnFraStillingsforholdperiode() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioder(
                asList(underlagene.get(STILLINGSFORHOLD_A)),
                harAnnotasjon(Loennstrinn.class).negate()
        ).isEmpty();

        assertAnnotasjonFraUnderlagsperioder(
                underlagene.values(),
                Loennstrinn.class,
                harAnnotasjon(Loennstrinn.class)
        ).containsOnlyElementsOf(
                fraMedlemsdata(
                        stillingsendringOversetter(),
                        e -> e.loennstrinn().get(),
                        e -> e.loennstrinn().isPresent()
                )
        );
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med lønn henta frå overlappande stillingsforholdperiodes
     * gjeldande stillingsendring for stillingsforholdet
     * som blir innrapportert med lønn.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedLoennFraStillingsforholdperiode() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioder(
                asList(underlagene.get(STILLINGSFORHOLD_B)),
                harAnnotasjon(DeltidsjustertLoenn.class).negate()
        ).isEmpty();

        assertAnnotasjonFraUnderlagsperioder(
                underlagene.values(),
                DeltidsjustertLoenn.class,
                harAnnotasjon(DeltidsjustertLoenn.class)
        ).containsOnlyElementsOf(
                fraMedlemsdata(
                        stillingsendringOversetter(),
                        e -> e.loenn().get(),
                        e -> e.loenn().isPresent()
                )
        );
    }

    /**
     * Verifiserer at underlagsperiodene blir kobla til måneden dei ligg innanfor.
     */
    @Test
    public void skalKobleAlleUnderlagsperioderTilMaanedenDeiLiggInnanfor() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioderUtanKoblingTil(underlagene, Maaned.class).isEmpty();
    }

    /**
     * Verifiserer at underlagsperiodene blir kobla til året dei ligg innanfor.
     */
    @Test
    public void skalKobleAlleUnderlagsperioderTilAaretDeiLiggInnanfor() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioderUtanKoblingTil(underlagene, Aar.class).isEmpty();
    }

    /**
     * Verifiserer at periodiseringa av underlag inkluderer avtalekoblingsperiodene i periodiseringa og koblar opp
     * underlagsperiodene til avtalekoblingsperiodene som dei overlappar.
     */
    @Test
    public void skalInkludereAvtalekoblingarIPeriodiseringa() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioderUtanKoblingTil(underlagene, Avtalekoblingsperiode.class).isEmpty();
    }

    /**
     * Verifiserer at fasada kun returnerer underlag der alle underlagsperiodene er kobla til eit stillingsforhold.
     */
    @Test
    public void skalKobleAlleUnderlagsperiodeTilStillingsforholdPeriode() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        assertUnderlagsperioderUtanKoblingTil(underlagene, StillingsforholdPeriode.class).isEmpty();
    }

    /**
     * Verifiserer at underlaget blir splitta kvar gang ein endrar måned innanfor observasjonsperioda.
     */
    @Test
    public void skalSplitteUnderlagVedKvarOvergangFraaEinMaanedTilEinAnnanMaanedInnanforObservasjonsperioda() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        final Aarstall aar = new Aarstall(2006);
        prosesser(underlagene::put, new Observasjonsperiode(aar.atStartOfYear(), aar.atEndOfYear()));

        final List<Underlag> aktiveStillingsforholdI2006 = underlagene
                .values()
                .stream()
                .filter(u -> u.stream().count() > 0)
                .collect(toList());
        assertThat(aktiveStillingsforholdI2006).hasSize(1);

        final List<Month> actual = aktiveStillingsforholdI2006
                .stream()
                .flatMap(u -> u.stream())
                .map(p -> Stream.of(
                                p.fraOgMed().getMonth(),
                                p.tilOgMed().get().getMonth()
                        )
                ).flatMap(s -> s)
                .distinct()
                .collect(toList());
        assertThat(actual)
                .as("maaneder som det finnes underlagsperioder for i underlaget til stillingsforholdet som er aktivt i 2006")
                .containsOnly(Month.values());
    }

    /**
     * Verifiserer at periodiseringa av underlag tar hensyn til endringar i gjeldande beregningsreglar og splittar også
     * blir periodisert ut frå regelperioder som representerer perioder med potensielt sett forskjellige beregningsreglar.
     */
    @Test
    public void skalPeriodisereUnderlagPaDatoarDerEinEndrarBeregningsRegel() {
        final LocalDate regelEndring = dato("2005.10.07");

        fasade.addBeregningsregel(new Regelperiode(regelEndring, empty(), new MaskineltGrunnlagRegel()));

        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        final Underlag underlag = underlagene.get(STILLINGSFORHOLD_A);
        assertThat(underlag
                        .stream()
                        .filter(p -> p.fraOgMed().isEqual(regelEndring))
                        .findFirst()
                        .isPresent()
        ).as(
                "har underlaget blitt splitta og periodisert når gjeldande beregningsregel endra seg den "
                        + regelEndring + "? (underlag = " + underlag + ")"
        )
                .isTrue();
    }

    /**
     * Verifiserer at underlag for stillingsforhold ikkje inneheld underlagperioder som
     * ligg før stillingsforholdets første stillingsendring, sjølv om ein har referansedata
     * eller regelperioder som startar/sluttar før stillingsforholdet startar.
     * <p>
     * Dette forutsetter at observasjonsperioda startar før stillingsforholdet startar.
     */
    @Test
    public void skalAvgrenseUnderlagetsStartTilStillingsforholdetsFoersteEndring() {
        final LocalDate regelEndring = dato("2005.01.01");

        fasade.addBeregningsregel(new Regelperiode(regelEndring, empty(), new MaskineltGrunnlagRegel()));

        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();
        prosesser(underlagene::put, standardperiode());

        final Underlag underlag = underlagene.get(STILLINGSFORHOLD_A);
        assertThat(underlag
                        .stream()
                        .findFirst()
                        .map(p -> p.fraOgMed())
                        .get()
        ).as(
                "fra og med-dato for underlagets første periode (underlag = " + underlag + ")"
        )
                .isEqualTo(dato("2005.08.15"));
    }

    /**
     * Verifiserer at fasada genererer eit underlag for kvart av stillingsforholda med historikk som medlemmet
     * er eller har vore tilknytta innanfor 10-års perioda frå 1. januar 2005 til 31. desember 2014.
     * <p>
     * TODO: Fjern denne når støtte for medregning er implementert.
     */
    @Test
    public void skalGenerereUnderlagPrStillingsforholdMedHistorikk() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        prosesser(underlagene::put, standardperiode());

        assertThat(underlagene).hasSize(2);

        assertThat(underlagene.get(STILLINGSFORHOLD_A)).hasSize(5 + 6 * 12 + 6); // 5 mnd i 2005 + 6 fulle år + 6 mnd i 2012
        assertThat(underlagene.get(STILLINGSFORHOLD_B)).hasSize(4 + 2 * 12); // 4 mnd i 2012 + 2 fulle år
    }

    /**
     * Verifiserer at fasada genererer eit underlag for kvart av stillingsforholda som medlemmet
     * er eller har vore tilknytta innanfor 10-års perioda frå 1. januar 2005 til 31. desember 2014.
     * <p>
     * TODO: Aktiver denne når støtte for medregning er implementert.
     */
    @Test
    @Ignore
    public void skalGenerereUnderlagPrStillingsforhold() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        prosesser(underlagene::put, standardperiode());

        assertThat(underlagene).hasSize(3);

        assertThat(underlagene.get(STILLINGSFORHOLD_A)).hasSize(14);
        assertThat(underlagene.get(STILLINGSFORHOLD_B)).hasSize(3);
        assertThat(underlagene.get(STILLINGSFORHOLD_C)).hasSize(3);
    }

    /**
     * Verifiserer at fasada ikkje filtrerer bort tomme underlag som blir generert for stillingsforhold
     * som ikkje har vore aktive minst ein dag innanfor observasjonsperioda.
     */
    @Test
    public void skalGenerereTommeUnderlagForStillingsforholdMedHistorikkSomIkkjeHarVoreAktiveIObservasjonsperioda() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        // CSV-fila inneheld 1 stillingsforhold som er aktivt mellom 2005 og 2009 og
        final Observasjonsperiode periode = new Observasjonsperiode(
                new Aarstall(2005).atStartOfYear(),
                new Aarstall(2009).atEndOfYear()
        );

        prosesser(underlagene::put, periode);

        assertThat(underlagene).hasSize(2);
        underlagene
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(STILLINGSFORHOLD_A))
                .forEach(e -> {
                            assertThat(e.getValue()).as("underlag for stillingsforhold " + e.getKey()).isEmpty();
                        }
                );
    }

    /**
     * Verifiserer at fasada ikkje filtrerer bort tomme underlag som blir generert for stillingsforhold
     * som ikkje har vore aktive minst ein dag innanfor observasjonsperioda.
     * <p>
     * TODO: Aktiver denne når støtte for medregning er implementert.
     */
    @Test
    @Ignore
    public void skalGenerereTommeUnderlagForStillingsforholdSomIkkjeHarVoreAktiveIObservasjonsperioda() {
        final Map<StillingsforholdId, Underlag> underlagene = new HashMap<>();

        // CSV-fila inneheld 1 stillingsforhold som er aktivt mellom 2005 og 2009 og 2 som er aktive etter 2009
        final Observasjonsperiode periode = new Observasjonsperiode(
                new Aarstall(2005).atStartOfYear(),
                new Aarstall(2009).atEndOfYear()
        );

        prosesser(underlagene::put, periode);

        assertThat(underlagene).hasSize(3);
        underlagene
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(STILLINGSFORHOLD_A))
                .forEach(e -> {
                            assertThat(e.getValue()).as("underlag for stillingsforhold " + e.getKey()).isEmpty();
                        }
                );
    }

    /**
     * Verifiserer at runtimeexception frå ein callback ikkje fører til at fasada feilar og
     * dermed skippar generering av underlag for andre stillingsforhold enn det som feila.
     */
    @Test
    public void skalSlukeOgIgnorereRuntimeExceptionsGenerertAvCallback() {
        final RuntimeException feil = new RuntimeException(
                "Horribel horribel feil oppstod og eg gir bæng i å " +
                        "handtere den fordi eg syns det er kjedelig"
        );

        final StillingsforholdUnderlagCallback callback = mock(StillingsforholdUnderlagCallback.class);
        doThrow(feil).when(callback).prosesser(eq(STILLINGSFORHOLD_A), any(Underlag.class));

        prosesser(callback, standardperiode());
    }

    /**
     * Verifiserer at fasada ikkje sluker/ignorerer {@link java.lang.Error}s generert av callbacken
     * sidan dette typisk er ein indikasjon på at heile JVMen er screwed og det ikkje har noka hensikt
     * å halde fram med prosesseringa.
     */
    @Test
    public void skalIkkjeSlukeErrorGenerertAvCallback() {
        final Error feil = new OutOfMemoryError(
                "Mmmm, RAM smakar så godt at eg like godt spiste opp heile heapen, yummy!"
        );

        e.expect(OutOfMemoryError.class);
        e.expectMessage(feil.getMessage());

        final StillingsforholdUnderlagCallback callback = mock(StillingsforholdUnderlagCallback.class);
        doThrow(feil).when(callback).prosesser(eq(STILLINGSFORHOLD_A), any(Underlag.class));

        prosesser(callback, standardperiode());
    }

    private void prosesser(final StillingsforholdUnderlagCallback callback, final Observasjonsperiode observasjonsperiode) {
        fasade.prosesser(medlem, callback, observasjonsperiode);
    }

    private Observasjonsperiode standardperiode() {
        return new Observasjonsperiode(new Aarstall(2005).atStartOfYear(), new Aarstall(2014).atEndOfYear());
    }

    private static <T> AbstractListAssert<?, ? extends List<T>, T> assertAnnotasjonFraUnderlagsperioder(
            final Collection<Underlag> underlag, final Class<T> annotasjonsType,
            final Predicate<Underlagsperiode>... predikater
    ) {
        return assertVerdiFraUnderlagsperioder(underlag, paakrevdAnnotasjon(annotasjonsType), predikater).as(
                "annoterte " +
                        annotasjonsType.getSimpleName() +
                        " frå underlagsperiodene i underlaga " + underlag
        );
    }

    private MedlemsdataOversetter<Stillingsendring> stillingsendringOversetter() {
        return (MedlemsdataOversetter<Stillingsendring>) oversettere.get(Stillingsendring.class);
    }

    private <T> List<T> fraMedlemsdata(final MedlemsdataOversetter<Stillingsendring> oversetter,
                                       final Function<Stillingsendring, T> mapper,
                                       final Predicate<Stillingsendring>... predikater) {
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett)
                .filter(and(predikater))
                .map(mapper)
                .collect(toList());
    }
}
