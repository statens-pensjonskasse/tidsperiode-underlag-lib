package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StillingsforholdUnderlagCallback;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.valueOf;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private Map<Class<?>, MedlemsdataOversetter<?>> oversettere;

    private TidsserieUnderlagFacade fasade;

    private Medlemsdata medlem;

    @Before
    public void _before() throws IOException {
        oversettere = new HashMap<>();
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());

        final List<List<String>> medlemsdata = CsvFileReader.readFromClasspath("/csv/medlem-1-stillingsforhold-3.csv");
        medlem = new Medlemsdata(medlemsdata, oversettere);

        fasade = new TidsserieUnderlagFacade();
    }

    /**
     * Verifiserer at periodiseringa av underlag tar hensyn til endringar i gjeldande beregningsreglar og splittar også blir periodisert ut frå regelperioder som representerer perioder med potensielt
     * sett forskjellige beregningsreglar.
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

        assertThat(underlagene.get(STILLINGSFORHOLD_A)).hasSize(14);
        assertThat(underlagene.get(STILLINGSFORHOLD_B)).hasSize(3);
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

        assertThat(underlagene.get(valueOf(999999999999L))).hasSize(14);
        assertThat(underlagene.get(valueOf(888888888888L))).hasSize(3);
        assertThat(underlagene.get(valueOf(777777777777L))).hasSize(3);
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
                .filter(e -> !e.getKey().equals(valueOf(999999999999L)))
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
                .filter(e -> !e.getKey().equals(valueOf(999999999999L)))
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
        doThrow(feil).when(callback).prosesser(eq(valueOf(999999999999L)), any(Underlag.class));

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
        doThrow(feil).when(callback).prosesser(eq(valueOf(999999999999L)), any(Underlag.class));

        prosesser(callback, standardperiode());
    }

    private void prosesser(final StillingsforholdUnderlagCallback callback, Observasjonsperiode observasjonsperiode) {
        fasade.prosesser(medlem, callback, observasjonsperiode);
    }

    private Observasjonsperiode standardperiode() {
        return new Observasjonsperiode(new Aarstall(2005).atStartOfYear(), new Aarstall(2014).atEndOfYear());
    }
}
