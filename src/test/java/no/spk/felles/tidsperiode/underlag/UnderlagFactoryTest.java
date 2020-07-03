package no.spk.felles.tidsperiode.underlag;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.IntStream.range;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static no.spk.felles.tidsperiode.underlag.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.atIndex;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;
import no.spk.felles.tidsperiode.underlag.Assertions.UnderlagAssertion;
import no.spk.felles.tidsperiode.underlag.Assertions.UnderlagsperiodeAssertion;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * Enheitstestar for UnderlagFactory.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagFactoryTest {
    private Observasjonsperiode grenser;

    @Before
    public void _before() {
        grenser = new Observasjonsperiode(dato("1970.01.01"), now().with(lastDayOfYear()));
    }

    /**
     * Verifiserer at kvar av underlagsperiodene i underlaget blir kobla opp mot
     * alle overlappande tidsperioder brukt ved periodiseringa av underlaget.
     */
    @Test
    public void skalKobleUnderlagsperiodeOppMotOverlappandeTidsperioder() {
        final GenerellTidsperiode a = periode(dato("2005.08.15"), of(dato("2012.06.30")));
        final GenerellTidsperiode b = periode(dato("2005.08.15"), empty());
        final GenerellTidsperiode c = periode(dato("2005.01.01"), empty());
        assertPeriodiser(a, b, c)
                .harPerioder(3)
                .periode(
                        atIndex(0),
                        verifiserKoblingar(
                                koblingar -> koblingar.containsOnly(c)
                        )
                )
                .periode(
                        atIndex(1),
                        verifiserKoblingar(
                                koblingar -> koblingar.containsOnly(a, b, c)
                        )
                )
                .periode(
                        atIndex(2),
                        verifiserKoblingar(
                                actual -> actual.containsOnly(b, c)
                        )
                )
        ;
    }

    @Test
    public void skal_inkludere_filtrere_koblingar_i_periodiseringa_men_ikkje_legge_dei_til_som_koblingar() {
        final int antallPerioder = 30000;
        assertPeriodiser(
                new Observasjonsperiode(LocalDate.MIN, LocalDate.MAX),
                byggIkkjeOverlappandePerioder(antallPerioder),
                kobling -> false
        )
                .harPerioder(antallPerioder)
                .allSatisfy(
                        periode -> periode.manglarKoblingAvType(GenerellTidsperiode.class)
                );
    }

    /**
     * Verifiserer at dersom ingen av tidsperiodene som blir brukt som input til periodiseringa av underlag
     * ovarlappar observasjonsperioda så blir eit tom underlag generert, dvs det er ein normal
     * situasjon som ikkje skal medføre nokon exception.
     */
    @Test
    public void skalIkkjeFeileVissAllePerioderLiggUtanforObservasjonsperioda() {
        assertPeriodiser(
                observasjonsperiode("2014.01.01", "2014.12.31"),
                periode(dato("2005.08.15"), of(dato("2012.06.30"))),
                periode(dato("2015.01.01"), empty())
        )
                .harPerioder(0);
    }

    /**
     * Verifiserer at perioder som ligg utanfor observasjonsperioda, dvs ikkje oerlappar den med minst ein dag,
     * ikkje får sine frå og med- og til og med-datoar brukt i forbindelse med periodiseringa av underlaget.
     */
    @Test
    public void skalIkkjeSplittePaaInputPerioderSomLiggUtanforObservasjonsperioda() {
        assertPeriodiser(
                observasjonsperiode("2014.01.01", "2014.12.31"),
                periode(dato("2005.08.15"), of(dato("2012.06.30"))),
                periode(dato("2012.07.01"), of(dato("2014.10.31"))),
                periode(dato("2015.01.01"), empty())
        )
                .harPerioder(1)
                .harFraOgMed("2014.01.01")
                .harTilOgMed("2014.10.31");
    }

    /**
     * Verifiserer at underlaget som kun er generert ut frå stillingsforholdperioder, får oppretta ei underlagsperiode
     * for kvar frå og med-dato frå alle stillingsforholdperiodene.
     */
    @Test
    public void skalLageUnderlagsperiodeForKvarStillingsendring() {
        assertPeriodiser(
                periode(dato("2005.01.01"), of(dato("2011.12.31"))),
                periode(dato("2012.01.01"), of(dato("2012.06.30")))
        )
                .harPerioder(2)
                .periode(
                        atIndex(0),
                        periode -> periode.harFraOgMed("2005.01.01").harTilOgMed("2011.12.31")
                )
                .periode(
                        atIndex(1),
                        periode -> periode.harFraOgMed("2012.01.01").harTilOgMed("2012.06.30")
                )
        ;
    }

    /**
     * Verifiserer at viss det eksisterer fleire input-perioder med samme fra og med-dato
     * så blir underlaget kun forsøkt splitta ein gang på den aktuelle datoen, ikkje ein gang
     * pr fra og med-dato slik at ein endar opp med å forsøke å konstruere underlagsperioder som har fra og med-dato
     * etter sin til og med-dato.
     */
    @Test
    public void skalKunSplittePaaUnikeEndringsdatoar() {
        assertPeriodiser(
                periode(dato("2001.01.01"), empty()),
                periode(dato("2001.01.01"), empty())
        )
                .harPerioder(1)
                .harFraOgMed("2001.01.01")
        ;
    }

    /**
     * Verifiserer at underlaget blir bygd opp med underlagsperioder i kronologisk rekkefølge sjølv om periodene som
     * blir lagt inn i underlaget blir lagt inn i ei anna rekkefølge.
     */
    @Test
    public void skalByggeOppUnderlagsperiodeneIKronologiskRekkefoelgeSjoelvOmInputPeriodeneKanVereIAnnaRekkefoelge() {
        assertPeriodiser(
                periode(dato("2010.01.01"), of(dato("2012.06.30"))),
                periode(dato("2003.07.13"), of(dato("2009.12.31")))
        )
                .harPerioder(2)
                .periode(atIndex(0), periode -> periode.harFraOgMed("2003.07.13").harTilOgMed("2009.12.31"))
                .periode(atIndex(1), periode -> periode.harFraOgMed("2010.01.01").harTilOgMed("2012.06.30"))
        ;
    }

    @Test
    public void skalByggePeriodeSomErEinDagLang() {
        final String expected = "2001.01.01";
        assertPeriodiser(
                periode(dato(expected), of(dato(expected)))
        )
                .harPerioder(1)
                .harFraOgMed(expected)
                .harTilOgMed(expected)
        ;
    }

    /**
     * Verifiserer at underlagets siste underlagsperiode blir avgrensa til siste dag i observasjonsperioda
     * viss den kronologisk siste tidsperioda brukt for å bygge opp underlaget, er løpande.
     */
    @Test
    public void skalAvslutteSisteUnderlagsperiodaPaaSisteDagIObservasjonsperiodaVissSisteTidsperiodeErLoepande() {
        assertPeriodiser(
                observasjonsperiode("2004.01.01", "2004.03.31"),
                periode(dato("2004.02.29"), empty())
        )
                .harPerioder(1)
                .harTilOgMed("2004.03.31");
    }

    /**
     * Verifiserer at underlagets siste underlagsperiode blir avgrensa til siste dag i observasjonsperioda
     * viss den kronologisk siste tidsperioda brukt for å bygge opp underlaget, er avslutta etter observasjonsperiodas
     * siste dag.
     * <h6>Grafisk illustrasjon</h6>
     * <pre>
     *     Observasjonsperiode:      |============|
     *     Stillingsforholdperiode:    |=================|
     * </pre>
     */
    @Test
    public void skalAvslutteSisteUnderlagsperiodaPaaSisteDagIObservasjonsperiodaVissSisteTidsperiodesErAvsluttaSeinare() {
        assertPeriodiser(
                observasjonsperiode("2004.01.01", "2008.02.28"),
                periode(dato("2004.02.29"), of(dato("2012.06.01")))
        )
                .harPerioder(1)
                .harTilOgMed("2008.02.28");
    }

    /**
     * Verifiserer at vi ikkje har nokon ekle +/- 1 dag feil i handtering av øvre grense for dato i underlaget.
     * <p>
     * Dersom vi har ei periode som har til og med-dato lik observasjonsperiodas til og med-dato og ei underlagsperiode
     * som startar dagen etter så skal det ikkje genererast meir enn ei periode i underlaget.
     * <p>
     * Dersom vi har ei periode som har frå og med-dato lik observasjonsperiodas til og med-dato så skal underlaget
     * splittast på denne dagen så ein endar opp med ei en-dag lang underlagsperiode som siste periode i underlaget.
     */
    @Test
    public void skalHandtereGrenseverdiarVedAvgrensingAvOevredatoGrenseKorrekt() {
        final String expected = "2008.02.28";

        assertPeriodiser(
                observasjonsperiode("2004.01.01", expected),
                periode(dato("2004.02.29"), of(dato("2008.02.28"))),
                periode(dato("2008.03.01"), of(dato("2012.06.01")))
        )
                .harPerioder(1)
                .harFraOgMed("2004.02.29")
                .harTilOgMed(expected)
        ;

        assertPeriodiser(
                observasjonsperiode("2004.01.01", expected),
                periode(dato("2004.02.29"), of(dato("2008.02.27"))),
                periode(dato("2008.02.28"), of(dato("2012.06.01")))
        )
                .harPerioder(2)
                .periode(atIndex(0), periode -> periode.harFraOgMed("2004.02.29").harTilOgMed(dato(expected).minusDays(1)))
                .periode(atIndex(1), periode -> periode.harFraOgMed(expected).harTilOgMed(expected))
        ;
    }

    /**
     * Verifiserer at underlagets første underlagsperiode blir avgrensa til første dag i observasjonsperioda
     * viss den kronologisk første tidsperioda brukt for å bygge opp underlaget, startar før observasjonsperiodas
     * første dag.
     * <h6>Grafisk illustrasjon</h6>
     * <pre>
     *     Observasjonsperiode:               |============|
     *     Stillingsforholdperiode:  |=================|
     * </pre>
     */
    @Test
    public void skalStartFoersteUnderlagsperiodaPaaFoersteDagIObservasjonsperiodaVissFoersteTidsperiodesStartarTidligare() {
        final String expected = "2004.01.01";
        assertPeriodiser(
                observasjonsperiode(expected, "2008.02.28"),
                periode(dato("2000.02.29"), of(dato("2007.06.01")))
        )
                .harPerioder(1)
                .harFraOgMed(expected)
        ;
    }

    /**
     * Verifiserer at underlaget endar opp med kun ei underlagsperiode dersom input periodene som overlappar
     * observasjonsperioda har sine frå og med- og til og med-dato utanfor observasjonsperioda.
     * <p>
     * Hovedintensjonen her er å sikre at periodiseringa ikkje gjer nokon antagelsar om at det alltid vil eksistere
     * datoar å splitte på innanfor observasjonsperioda dersom den har overlappande tidsperioder som input.
     */
    @Test
    public void skalKonstruereUnderlagMedKunEiUnderlagsperiodeVissInputPeriodeneOverlapparMenFraOgMedOgTilOgMedDatoaneAlleLiggUtanforObservasjonsperioda() {
        assertPeriodiser(
                observasjonsperiode("2001.01.01", "2001.12.31"),
                periode(dato("2000.01.01"), of(dato("2002.01.01")))
        )
                .harPerioder(1)
                .harFraOgMed("2001.01.01")
                .harTilOgMed("2001.12.31")
        ;
    }

    /**
     * Verifiserer at eit underlagets grenser, som representerer grenseverdiane for eldste
     * mulige frå og med-dato og yngste mulige til og med-dato, er påkrevd.
     * <br>
     * Intensjonen med denne begrensinga er å forenkle periodiseringa og den seinare bruken av underlaget sidan ein no
     * kan avgrense maksimal lengde og unngå "kanskje er har, kanskje eg ikkje har" sjekkar på sluttdato til siste
     * underlagsperiode.
     */
    @Test
    public void skalKreveEiObservasjonsperiode() {
        assertThatCode(
                () -> new UnderlagFactory(null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("observasjonsperiode er påkrevd, men var null")
        ;
    }

    private GenerellTidsperiode periode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return new GenerellTidsperiode(fraOgMed, tilOgMed);
    }

    private Consumer<UnderlagsperiodeAssertion> verifiserKoblingar(
            final Consumer<ListAssert<GenerellTidsperiode>> assertion
    ) {
        return periode ->
                periode
                        .harKoblingarAvType(
                                GenerellTidsperiode.class,
                                assertion
                        );
    }

    private UnderlagAssertion assertPeriodiser(final Observasjonsperiode observasjonsperiode, final GenerellTidsperiode... perioder) {
        return assertPeriodiser(observasjonsperiode, Stream.of(perioder));
    }

    private UnderlagAssertion assertPeriodiser(final GenerellTidsperiode... perioder) {
        return assertPeriodiser(grenser, Stream.of(perioder));
    }

    private UnderlagAssertion assertPeriodiser(final Observasjonsperiode observasjonsperiode, final Stream<GenerellTidsperiode> perioder) {
        return assertThat(
                new UnderlagFactory(observasjonsperiode)
                        .addPerioder(perioder)
                        .periodiser()
        );
    }

    private UnderlagAssertion assertPeriodiser(
            final Observasjonsperiode observasjonsperiode,
            final Stream<GenerellTidsperiode> perioder,
            final Predicate<Tidsperiode<?>> filter
    ) {
        return assertThat(
                new UnderlagFactory(observasjonsperiode)
                        .addPerioder(perioder)
                        .filtrerKoblinger(filter)
                        .periodiser()
        );
    }

    private Observasjonsperiode observasjonsperiode(final String fraOgMed, final String tilOgMed) {
        return new Observasjonsperiode(dato(fraOgMed), dato(tilOgMed));
    }

    private Stream<GenerellTidsperiode> byggIkkjeOverlappandePerioder(final int antallPerioder) {
        return range(0, Integer.MAX_VALUE)
                .mapToObj(dato("1917.01.01")::plusDays)
                .limit(antallPerioder)
                .map(dato -> new GenerellTidsperiode(dato, of(dato)));
    }
}
