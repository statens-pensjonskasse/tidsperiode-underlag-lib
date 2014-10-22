package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertFraOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for UnderlagFactory.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagFactoryTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private Observasjonsperiode grenser;

    @Before
    public void _before() {
        grenser = new Observasjonsperiode(dato("1970.01.01"), now().with(lastDayOfYear()));
    }

    /**
     * Verifiserer at dersom ingen av tidsperiodene som blir brukt som input til periodiseringa av underlag
     * ovarlappar observasjonsperioda så blir eit tom underlag generert, dvs det er ein normal
     * situasjon som ikkje skal medføre nokon exception.
     */
    @Test
    public void skalIkkjeFeileVissAllePerioderLiggUtanforObservasjonsperioda() {
        final Underlag underlag = create("2014.01.01", "2014.12.31")
                .addPerioder(
                        new StillingsforholdPeriode(dato("2005.08.15"), of(dato("2012.06.30"))),
                        new StillingsforholdPeriode(dato("2015.01.01"), empty())
                )
                .periodiser();
        assertThat(underlag).hasSize(0);
    }

    /**
     * Verifiserer at perioder som ligg utanfor observasjonsperioda, dvs ikkje oerlappar den med minst ein dag,
     * ikkje får sine frå og med- og til og med-datoar brukt i forbindelse med periodiseringa av underlaget.
     */
    @Test
    public void skalIkkjeSplittePaaInputPerioderSomLiggUtanforObservasjonsperioda() {
        final Underlag underlag = create("2014.01.01", "2014.12.31")
                .addPerioder(
                        new StillingsforholdPeriode(dato("2005.08.15"), of(dato("2012.06.30"))),
                        new StillingsforholdPeriode(dato("2012.07.01"), of(dato("2014.10.31"))),
                        new StillingsforholdPeriode(dato("2015.01.01"), empty())
                )
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertFraOgMed(underlag, 0).isEqualTo(dato("2014.01.01"));
        assertTilOgMed(underlag, 0).isEqualTo(of(dato("2014.10.31")));
    }

    /**
     * Verifiserer at det blir betrakta som ein feil å ikkje legge til nokon tidsperioder som input
     * før konstruksjon av nytt underlag for blir forsøkt utført.
     */
    @Test
    public void skalFeileDersomIngenPerioderErLagtTilFoerPeriodiseringBlirForsoektUtfoert() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Periodisering av underlag krever minst ei tidsperiode som input");
        e.expectMessage("fabrikken er satt opp uten nokon tidsperioder");
        create().periodiser();
    }

    /**
     * Verifiserer at underlaget som kun er generert ut frå stillingsforholdperioder, får oppretta ei underlagsperiode
     * for kvar frå og med-dato frå alle stillingsforholdperiodene.
     */
    @Test
    public void skalLageUnderlagsperiodeForKvarStillingsendring() {
        final Underlag underlag = create()
                .addPerioder(
                        new StillingsforholdPeriode(dato("2005.01.01"), of(dato("2011.12.31"))),
                        new StillingsforholdPeriode(dato("2012.01.01"), of(dato("2012.06.30")))
                ).periodiser();
        assertThat(underlag).hasSize(2);

        assertFraOgMed(underlag, 0).isEqualTo(dato("2005.01.01"));
        assertTilOgMed(underlag, 0).isEqualTo(of(dato("2011.12.31")));

        assertFraOgMed(underlag, 1).isEqualTo(dato("2012.01.01"));
        assertTilOgMed(underlag, 1).isEqualTo(of(dato("2012.06.30")));
    }

    /**
     * Verifiserer at viss det eksisterer fleire input-perioder med samme fra og med-dato
     * så blir underlaget kun forsøkt splitta ein gang på den aktuelle datoen, ikkje ein gang
     * pr fra og med-dato slik at ein endar opp med å forsøke å konstruere underlagsperioder som har fra og med-dato
     * etter sin til og med-dato.
     */
    @Test
    public void skalKunSplittePaaUnikeEndringsdatoar() {
        LocalDate duplisertDato = dato("2001.01.01");
        Underlag underlag = create()
                .addPerioder(
                        new StillingsforholdPeriode(duplisertDato, empty()),
                        new StillingsforholdPeriode(duplisertDato, empty())
                ).periodiser();
        assertThat(underlag).hasSize(1);
        assertFraOgMed(underlag, 0).isEqualTo(dato("2001.01.01"));
        assertThat(underlag.toList().get(0).tilOgMed().get()).isGreaterThan(duplisertDato);
    }

    /**
     * Verifiserer at underlaget blir bygd opp med underlagsperioder i kronologisk rekkefølge sjølv om periodene som
     * blir lagt inn i underlaget blir lagt inn i ei anna rekkefølge.
     */
    @Test
    public void skalByggeOppUnderlagsperiodeneIKronologiskRekkefoelgeSjoelvOmInputPeriodeneKanVereIAnnaRekkefoelge() {
        Underlag underlag = create()
                .addPerioder(
                        new StillingsforholdPeriode(dato("2010.01.01"), of(dato("2012.06.30"))),
                        new StillingsforholdPeriode(dato("2003.07.13"), of(dato("2009.12.31")))
                )
                .periodiser();
        assertThat(underlag).hasSize(2);
        assertFraOgMed(underlag, 0).isEqualTo(dato("2003.07.13"));
        assertTilOgMed(underlag, 1).isEqualTo(of(dato("2012.06.30")));
    }

    @Test
    public void skalByggePeriodeSomErEinDagLang() {
        final LocalDate expected = dato("2001.01.01");
        final Underlag underlag = create()
                .addPerioder(
                        new StillingsforholdPeriode(expected, of(expected))
                )
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertFraOgMed(underlag, 0).isEqualTo(expected);
        assertTilOgMed(underlag, 0).isEqualTo(of(expected));
    }

    /**
     * Verifiserer at underlagets siste underlagsperiode blir avgrensa til siste dag i observasjonsperioda
     * viss den kronologisk siste tidsperioda brukt for å bygge opp underlaget, er løpande.
     */
    @Test
    public void skalAvslutteSisteUnderlagsperiodaPaaSisteDagIObservasjonsperiodaVissSisteTidsperiodeErLoepande() {
        final String expected = "2004.03.31";
        Underlag underlag = create("2004.01.01", expected)
                .addPerioder(new StillingsforholdPeriode(dato("2004.02.29"), empty()))
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertTilOgMed(underlag, 0).isEqualTo(of(dato(expected)));
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
        final String expected = "2008.02.28";

        final Underlag underlag = create("2004.01.01", expected)
                .addPerioder(new StillingsforholdPeriode(dato("2004.02.29"), of(dato("2012.06.01"))))
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertTilOgMed(underlag, 0).isEqualTo(of(dato(expected)));
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
        final Underlag underlag = create(expected, "2008.02.28")
                .addPerioder(new StillingsforholdPeriode(dato("2000.02.29"), of(dato("2007.06.01"))))
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertFraOgMed(underlag, 0).isEqualTo(dato(expected));
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
        Underlag underlag = create("2001.01.01", "2001.12.31")
                .addPerioder(new StillingsforholdPeriode(dato("2000.01.01"), of(dato("2002.01.01"))))
                .periodiser();
        assertThat(underlag).hasSize(1);
        assertFraOgMed(underlag, 0).isEqualTo(dato("2001.01.01"));
        assertTilOgMed(underlag, 0).isEqualTo(of(dato("2001.12.31")));
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
        e.expect(NullPointerException.class);
        e.expectMessage("observasjonsperiode er påkrevd, men var null");
        new UnderlagFactory(null);
    }

    private UnderlagFactory create(final String fraOgMed, final String tilOgMed) {
        return new UnderlagFactory(
                new Observasjonsperiode(dato(fraOgMed), dato(tilOgMed))
        );
    }

    private UnderlagFactory create() {
        return new UnderlagFactory(grenser);
    }
}
