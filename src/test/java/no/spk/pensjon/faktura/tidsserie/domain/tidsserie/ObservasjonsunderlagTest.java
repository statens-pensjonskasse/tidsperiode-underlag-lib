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
     * Verifiserer at innsending av eit tomt underlag medf�rer at ingen observasjonsunderlag blir generert.
     */
    @Test
    public void skalIkkjeGenerereNokonObservasjonsunderlagVissAarsunderlagErTomt() {
        assertThat(observasjonsunderlag.genererUnderlagPrMaaned(new Underlag(Stream.empty())).collect(toList()))
                .as("observasjonsunderlag generert fr� tomt �rsunderlage").isEmpty();
    }

    /**
     * Verifiserer at observasjonsunderlaget inneheld ei ny fiktiv periode fr� og med dagen etter
     * siste synlige periode i �rsunderlaget, til og med �rets slutt, for alle
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
                    // Forventar at mnd nr X inneheld X synlige m�nedar + 1 fiktiv periode p� slutten
                    assertObservasjonsunderlagMedFiktivPeriode(prMnd, nr - 1).hasSize(nr + 1);
                });
    }

    /**
     * Verifiserer at observasjonsunderlaget for desember ikkje inneheld ei fiktiv periode ettersom
     * observasjonsunderlaget i desember alltid vil bli likt �rsunderlaget.
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
     * Verifiserer at observasjonsunderlaget ikkje f�r noka fiktiv periode generert etter observasjonsdatoen
     * dersom siste underlagsperiode i �rsunderlaget er annotert med SistePeriode.
     * <p>
     * Intensjonen med dette er at n�r stillingsforholdet blir avslutta innanfor �ret s� skal vi slutte � generere
     * fiktive perioder i observasjonsunderlaga som blir generert fr� og med den m�neden som overlappar
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
                    // Forventar at mnd nr X inneheld X synlige m�nedar + 1 fiktiv periode for resten av �ret
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
     * Verifiserer at genereringa feilar dersom det antatte �rsunderlaget ikkje inneheld
     * perioder annotert med �rstall.
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
     * Verifiserer at genereringa feilar dersom �rsunderlaget inneheld perioder tilknytta meir enn eit �rstall.
     */
    @Test
    public void skalFeileDersomAntattAarsunderlagetIkkjeErEitAarsunderlag() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Generering av observasjonsunderlag er ikkje st�tta for �rsunderlag som dekker meir enn eit �r om gangen");
        e.expectMessage("fann 2 unike �rstall i underlaget");
        final Underlag underlag = underlag(
                periode().fraOgMed(dato("2001.12.01")).tilOgMed(dato("2001.12.31")).med(new Aarstall(2001)).med(Month.DECEMBER),
                periode().fraOgMed(dato("2002.01.01")).tilOgMed(dato("2002.01.31")).med(new Aarstall(2002)).med(Month.JANUARY)
        );
        observasjonsunderlag.genererUnderlagPrMaaned(underlag);
    }

    /**
     * Verifiserer at det blir generert eit nytt observasjonsunderlag for kvar
     * unike m�ned som det finnes perioder for i �rsunderlaget.
     */
    @Test
    public void skalGenerereEitUnderlagPrUnikeMaanedIAarsunderlaget() {
        assertObservasjonsunderlag(etAarsunderlag()).hasSize(12);
    }

    /**
     * Verifiserer at dersom �rsunderlagets underlagsperioder sluttar eit par m�nedar ut i �ret,
     * s� blir det framleis generert eit observasjonsunderlag for m�nedane som det ikkje er tilknytta nokon
     * underlagsperioder til p� slutten av �ret.
     * <p>
     * Intensjonen med dette er at maskinelt grunnlag gjeld for heile �ret, s� sj�lv om stillingsforholdet
     * er avslutta midt i �ret s� skal ein framleis ta hensyn til dei tidligare periodenes bidrag til �rets totale
     * maskinelle grunnlag n�r ein utf�rer ein observasjon i m�nedane som gjennst�r ut �ret.
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
     * Verifiserer at observasjonsunderlaga som blir generert fr� og med underlagsperioda som er siste periode i
     * �rsunderlaget, perioda som inneheld stillingsforholdets sluttmelding, ikkje f�r generert nokon fiktive perioder
     * etter siste periodes til og med-dato.
     * <p>
     * Intensjonen her er at inntil vi st�r p� eller etter sluttmeldinga til eit stillingsforhold s� skal vi tru at det
     * vil forbli aktivt til evig tid (vel, iallefall resten av �ret).
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

        // 5 synlige periode + ei fiktiv periode ut �ret
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 4).hasSize(5 + 1);
        assertThat(prMnd.get(4).last().get().tilOgMed()).isEqualTo(of(dato("2012.12.31")));

        // 7 synlige periode, inga fiktiv periode sidan siste synlige periode er siste periode i �rsunderlaget
        assertThat(prMnd.get(5)).hasSize(7);
        assertThat(prMnd.get(5).last().get().tilOgMed()).isEqualTo(of(dato("2012.06.15")));
    }

    /**
     * Verifiserer at dersom �rsunderlagets underlagsperioder ikkje startar f�r eit par m�nedar ut i �ret,
     * s� blir det ikkje generert eit observasjonsunderlag for m�nedane som det ikkje er tilknytta nokon
     * underlagsperioder til i starten av �ret.
     * <p>
     * Intensjonen med dette er at f�r stillingsforholdet startar s� skal det maskinelle grunnlaget for �ret som
     * heilheit, ikkje ta hensyn til framtidige endringar. Ergo er det �nska at maskinelt grunnlag ikkje skal bli
     * generert f�r ein er p� eller har passert stillingsforholdets startdato.
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
     * Verifiserer at det kun blir generert ei fiktiv periode og at den er basert p� siste underlagsperiode i den siste
     * synlige m�naden for observasjonsunderlaget, sj�lv om siste synlig m�nad inneheld meir enn ei underlagsperiode.
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
        ).as("observasjonsunderlag generert basert p� " + aarsunderlag);
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
     * Kombinert med {@link Stream#reduce(java.util.function.BinaryOperator)} vil den her gjere ein i stand til �
     * hente ut siste verdi fr� ein straum p� ein enkel m�te.
     *
     * @param ignored this value is totally ignored
     * @param value   this value is always returned
     * @return <code>value</code>
     */
    private <T> T last(final T ignored, final T value) {
        return value;
    }
}
