package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Observasjonsunderlag}.
 *
 * @author Tarjei Skorgenes
 */
public class ObservasjonsunderlagTest {

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final Observasjonsunderlag observasjonsunderlag = new Observasjonsunderlag();

    /**
     * Verifiserer at innsending av eit tomt underlag medfører at ingen observasjonsunderlag blir generert.
     */
    @Test
    public void skalIkkjeGenerereNokonObservasjonsunderlagVissAarsunderlagErTomt() {
        assertThat(observasjonsunderlag.genererUnderlagPrMaaned(new Underlag(Stream.empty())).collect(toList()))
                .as("observasjonsunderlag generert frå tomt årsunderlage").isEmpty();
    }

    /**
     * Verifiserer at observasjonsunderlaget inneheld ei ny fiktiv periode frå og med dagen etter
     * siste synlige periode i årsunderlaget, til og med årets slutt, for alle
     * observasjonsunderlaga fram til og med observasjonsunderlaget for november.
     */
    @Test
    public void skalGenerereFiktivPeriodeFraOgMedDagenEtterObservasjonsunderlagetsMaanedsSisteDagOgUtAaret() {
        final Underlag aarsunderlag = etAarsunderlag();
        assertObservasjonsunderlag(aarsunderlag).hasSize(12);

        final List<Underlag> prMnd = observasjonsunderlag
                .genererUnderlagPrMaaned(aarsunderlag)
                .collect(toList());
        rangeClosed(Month.JANUARY.getValue(), Month.NOVEMBER.getValue())
                .forEach(nr -> {
                    // Forventar at mnd nr X inneheld X synlige månedar + 1 fiktiv periode på slutten
                    assertObservasjonsunderlagMedFiktivPeriode(prMnd, nr - 1).hasSize(nr + 1);
                });
    }

    /**
     * Verifiserer at observasjonsunderlaget for desember ikkje inneheld ei fiktiv periode ettersom
     * observasjonsunderlaget i desember alltid vil bli likt årsunderlaget.
     */
    @Test
    public void skalIkkjeGenerereFiktivPeriodeUtAaretIObservasjonsunderlagetForDesember() {
        assertThat(
                observasjonsunderlag
                        .genererUnderlagPrMaaned(etAarsunderlag())
                        .reduce(this::last)
                        .get()
                        .last()
                        .get()
                        .valgfriAnnotasjonFor(FiktivPeriode.class)
        ).as("FiktivPeriode-annotasjon for siste underlagsperiode i observasjonsunderlaget for desember")
                .isEqualTo(empty());
    }

    /**
     * Verifiserer at observasjonsunderlaget ikkje får noka fiktiv periode generert etter observasjonsdatoen
     * dersom siste underlagsperiode i årsunderlaget er annotert med SistePeriode.
     * <p>
     * Intensjonen med dette er at når stillingsforholdet blir avslutta innanfor året så skal vi slutte å generere
     * fiktive perioder i observasjonsunderlaga som blir generert frå og med den måneden som overlappar
     * stillingsforholdets sluttdato.
     */
    @Test
    public void skalIkkjeGenerereFiktivPeriodeUtAaretDersomSisteUnderlagsperiodeIAarsunderlagetErAnnotertMedSistePeriode() {
        final UnderlagsperiodeBuilder builder = periode()
                .med(new Aarstall(2000));
        final Underlag aarsunderlag = underlag(
                builder.kopi().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(Month.FEBRUARY),
                builder.kopi().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(Month.MARCH),
                builder.kopi().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(Month.APRIL),
                builder.kopi().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.21")).med(Month.MAY)
                        .med(SistePeriode.INSTANCE)
        );

        final List<Underlag> prMnd = observasjonsunderlag.genererUnderlagPrMaaned(aarsunderlag).collect(toList());
        rangeClosed(Month.JANUARY.getValue(), Month.APRIL.getValue())
                .forEach(nr -> {
                    // Forventar at mnd nr X inneheld X synlige månedar + 1 fiktiv periode for resten av året
                    assertObservasjonsunderlagMedFiktivPeriode(prMnd, nr - 1).hasSize(nr + 1);
                    assertTilOgMed(prMnd.get(nr - 1).last().get()).isEqualTo(of(dato("2000.12.31")));
                });
        rangeClosed(Month.MAY.getValue(), Month.DECEMBER.getValue())
                .forEach(nr -> {
                    assertObservasjonsunderlagUtanFiktivPeriode(prMnd, nr - 1).hasSize(5);
                    assertTilOgMed(prMnd.get(nr - 1).last().get()).isEqualTo(of(dato("2000.05.21")));
                });
    }

    /**
     * Verifiserer at genereringa feilar dersom det antatte årsunderlaget ikkje inneheld
     * perioder annotert med årstall.
     */
    @Test
    public void skalFeileDersomAntattAarsunderlagInneheldPerioderUtanAarstallAnnotasjon() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        final Underlag underlag = underlag(
                periode().fraOgMed(dato("2001.12.01")).tilOgMed(dato("2001.12.31")).med(Month.DECEMBER)
        );
        observasjonsunderlag.genererUnderlagPrMaaned(underlag);
    }

    /**
     * Verifiserer at genereringa feilar dersom årsunderlaget inneheld perioder tilknytta meir enn eit årstall.
     */
    @Test
    public void skalFeileDersomAntattAarsunderlagetIkkjeErEitAarsunderlag() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Generering av observasjonsunderlag er ikkje støtta for årsunderlag som dekker meir enn eit år om gangen");
        e.expectMessage("fann 2 unike årstall i underlaget");
        final Underlag underlag = underlag(
                periode().fraOgMed(dato("2001.12.01")).tilOgMed(dato("2001.12.31")).med(new Aarstall(2001)).med(Month.DECEMBER),
                periode().fraOgMed(dato("2002.01.01")).tilOgMed(dato("2002.01.31")).med(new Aarstall(2002)).med(Month.JANUARY)
        );
        observasjonsunderlag.genererUnderlagPrMaaned(underlag);
    }

    /**
     * Verifiserer at det blir generert eit nytt observasjonsunderlag for kvar
     * unike måned som det finnes perioder for i årsunderlaget.
     */
    @Test
    public void skalGenerereEitUnderlagPrUnikeMaanedIAarsunderlaget() {
        assertObservasjonsunderlag(etAarsunderlag()).hasSize(12);
    }

    /**
     * Verifiserer at dersom årsunderlagets underlagsperioder sluttar eit par månedar ut i året,
     * så blir det framleis generert eit observasjonsunderlag for månedane som det ikkje er tilknytta nokon
     * underlagsperioder til på slutten av året.
     * <p>
     * Intensjonen med dette er at maskinelt grunnlag gjeld for heile året, så sjølv om stillingsforholdet
     * er avslutta midt i året så skal ein framleis ta hensyn til dei tidligare periodenes bidrag til årets totale
     * maskinelle grunnlag når ein utfører ein observasjon i månedane som gjennstår ut året.
     */
    @Test
    public void skalGenerereObservasjonsunderlagForMaanedarEtterSistePeriodesTilknyttaMaaned() {
        final UnderlagsperiodeBuilder builder = periode().med(new Aarstall(2000));
        final Underlag aarsunderlag = underlag(
                builder.kopi().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(Month.FEBRUARY),
                builder.kopi().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(Month.MARCH),
                builder.kopi().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(Month.APRIL),
                builder.kopi().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(Month.MAY),
                builder.kopi().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(Month.JUNE),
                builder.kopi().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(Month.JULY),
                builder.kopi().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(Month.AUGUST)
        );
        assertObservasjonsunderlag(aarsunderlag).hasSize(12);
    }

    /**
     * Verifiserer at observasjonsunderlaga som blir generert frå og med underlagsperioda som er siste periode i
     * årsunderlaget, perioda som inneheld stillingsforholdets sluttmelding, ikkje får generert nokon fiktive perioder
     * etter siste periodes til og med-dato.
     * <p>
     * Intensjonen her er at inntil vi står på eller etter sluttmeldinga til eit stillingsforhold så skal vi tru at det
     * vil forbli aktivt til evig tid (vel, iallefall resten av året).
     */
    @Test
    public void skalGenerereObservasjonsunderlagAvKorrektLengdeDersomStillingsforholdetBlirAvsluttaILoepetAvAaret() {
        final UnderlagsperiodeBuilder builder = periode()
                .med(new Aarstall(2012));
        final Underlag aarsunderlag = underlag(
                builder.kopi().fraOgMed(dato("2012.01.01")).tilOgMed(dato("2012.01.31")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2012.02.01")).tilOgMed(dato("2012.02.29")).med(Month.FEBRUARY),
                builder.kopi().fraOgMed(dato("2012.03.01")).tilOgMed(dato("2012.03.31")).med(Month.MARCH),
                builder.kopi().fraOgMed(dato("2012.04.01")).tilOgMed(dato("2012.04.30")).med(Month.APRIL),
                builder.kopi().fraOgMed(dato("2012.05.01")).tilOgMed(dato("2012.05.31")).med(Month.MAY),
                builder.kopi().fraOgMed(dato("2012.06.01")).tilOgMed(dato("2012.06.09")).med(Month.JUNE),
                builder.kopi().fraOgMed(dato("2012.06.10")).tilOgMed(dato("2012.06.15"))
                        .med(Month.JUNE).med(SistePeriode.INSTANCE)
        );
        final List<Underlag> prMnd = observasjonsunderlag.genererUnderlagPrMaaned(aarsunderlag).collect(toList());

        // 5 synlige periode + ei fiktiv periode ut året
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 4).hasSize(5 + 1);
        assertThat(prMnd.get(4).last().get().tilOgMed()).isEqualTo(of(dato("2012.12.31")));

        // 7 synlige periode, inga fiktiv periode sidan siste synlige periode er siste periode i årsunderlaget
        assertThat(prMnd.get(5)).hasSize(7);
        assertThat(prMnd.get(5).last().get().tilOgMed()).isEqualTo(of(dato("2012.06.15")));
    }

    /**
     * Verifiserer at dersom årsunderlagets underlagsperioder ikkje startar før eit par månedar ut i året,
     * så blir det ikkje generert eit observasjonsunderlag for månedane som det ikkje er tilknytta nokon
     * underlagsperioder til i starten av året.
     * <p>
     * Intensjonen med dette er at før stillingsforholdet startar så skal det maskinelle grunnlaget for året som
     * heilheit, ikkje ta hensyn til framtidige endringar. Ergo er det ønska at maskinelt grunnlag ikkje skal bli
     * generert før ein er på eller har passert stillingsforholdets startdato.
     */
    @Test
    public void skalIkkjeGenerereObservasjonsunderlagForMaanedarFoerFoerstePeriodesTilknyttaMaaned() {
        final UnderlagsperiodeBuilder builder = periode().med(new Aarstall(2000));
        final Underlag aarsunderlag = underlag(
                builder.kopi().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(Month.OCTOBER),
                builder.kopi().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(Month.NOVEMBER),
                builder.kopi().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(Month.DECEMBER)
        );
        assertObservasjonsunderlag(aarsunderlag).hasSize(3);
    }

    /**
     * Verifiserer at det kun blir generert ei fiktiv periode og at den er basert på siste underlagsperiode i den siste
     * synlige månaden for observasjonsunderlaget, sjølv om siste synlig månad inneheld meir enn ei underlagsperiode.
     */
    @Test
    public void skalGenerereFiktivPeriodeUtAaretBasertPaaSisteUnderlagsperiodeISisteSynligeMaanadIkkjeAllePerioderISisteSynligeMaanad() {
        final UnderlagsperiodeBuilder builder = periode().med(new Aarstall(2015));
        final Underlag aarsunderlag = underlag(
                builder.kopi().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.14")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.01.31")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2015.02.01")).tilOgMed(dato("2015.02.28")).med(Month.FEBRUARY)
                        .med(SistePeriode.INSTANCE)
        );
        final List<Underlag> prMnd = observasjonsunderlag.genererUnderlagPrMaaned(aarsunderlag).collect(toList());
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 0).hasSize(3);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 1).hasSize(3);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 2).hasSize(3);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 10).hasSize(3);
    }

    private static AbstractIterableAssert<?, ? extends Iterable<Underlagsperiode>, Underlagsperiode> assertObservasjonsunderlagUtanFiktivPeriode(final List<Underlag> prMnd, final int index) {
        final Underlag underlag = prMnd.get(index);
        assertThat(underlag.last().get().valgfriAnnotasjonFor(FiktivPeriode.class))
                .as("FiktivPeriode-annotasjon for siste periode i observasjonsunderlaget " + underlag)
                .isEqualTo(empty());
        return assertThat(underlag).as("Observasjonsunderlag for " + Month.of(index + 1) + " (" + underlag + ")");
    }

    private static UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private static Underlag underlag(final UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map(UnderlagsperiodeBuilder::bygg));
    }

    private AbstractListAssert<?, ? extends List<Underlag>, Underlag> assertObservasjonsunderlag(final Underlag aarsunderlag) {
        return assertThat(
                generer(aarsunderlag).collect(toList())
        ).as("observasjonsunderlag generert basert på " + aarsunderlag);
    }

    private static AbstractIterableAssert<?, ? extends Iterable<Underlagsperiode>, Underlagsperiode> assertObservasjonsunderlagMedFiktivPeriode(
            final List<Underlag> prMnd, final int index) {
        final Underlag underlag = prMnd.get(index);
        assertThat(underlag.last().get().valgfriAnnotasjonFor(FiktivPeriode.class))
                .as("annotasjon for siste periode i observasjonsunderlaget " + underlag)
                .isEqualTo(of(FiktivPeriode.FIKTIV));
        return assertThat(underlag).as("Observasjonsunderlag for " + Month.of(index + 1) + " (" + underlag + ")");
    }

    private Stream<Underlag> generer(final Underlag aarsunderlag) {
        return observasjonsunderlag
                .genererUnderlagPrMaaned(aarsunderlag);
    }

    private Underlag etAarsunderlag() {
        final UnderlagsperiodeBuilder builder = periode()
                .med(new Aarstall(2000));
        return underlag(
                builder.kopi().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(Month.JANUARY),
                builder.kopi().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(Month.FEBRUARY),
                builder.kopi().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(Month.MARCH),
                builder.kopi().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(Month.APRIL),
                builder.kopi().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(Month.MAY),
                builder.kopi().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(Month.JUNE),
                builder.kopi().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(Month.JULY),
                builder.kopi().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(Month.AUGUST),
                builder.kopi().fraOgMed(dato("2000.09.01")).tilOgMed(dato("2000.09.30")).med(Month.SEPTEMBER),
                builder.kopi().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(Month.OCTOBER),
                builder.kopi().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(Month.NOVEMBER),
                builder.kopi().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(Month.DECEMBER)
        );
    }

    /**
     * Ein enkel operator som alltid returnerer verdien av parameter nr 2.
     * <p>
     * Kombinert med {@link Stream#reduce(java.util.function.BinaryOperator)} vil den her gjere ein i stand til å
     * hente ut siste verdi frå ein straum på ein enkel måte.
     *
     * @param ignored this value is totally ignored
     * @param value   this value is always returned
     * @return <code>value</code>
     */
    private <T> T last(final T ignored, final T value) {
        return value;
    }
}
