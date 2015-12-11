package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

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
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertUnikeUnderlagsAnnotasjonar;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.harAnnotasjon;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.or;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.paakrevdAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link ObservasjonsunderlagFactory}.
 *
 * @author Tarjei Skorgenes
 */
public class ObservasjonsunderlagFactoryTest {

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final ObservasjonsunderlagFactory observasjonsunderlagFactory = new ObservasjonsunderlagFactory();

    /**
     * Verifiserer at kvart observasjonsunderlag får kopiert inn alle annotasjonane frå årsunderlaget.
     */
    @Test
    public void skalKopiereOverAlleAnnotasjonaneFraaAarsunderlagetTilObservasjonsunderlaga() {
        final Underlag aarsunderlag = etAarsunderlag()
                .annoter(StillingsforholdId.class, new StillingsforholdId(18971237L))
                .annoter(Integer.class, 123456789);
        final List<Underlag> alle = observasjonsunderlagFactory.genererUnderlagPrMaaned(
                aarsunderlag
        )
                .collect(toList());

        final Predicate<Underlag> manglerAnnotasjonFraAarsunderlagetUnderlaget = or(
                asList(
                        Assertions.<Underlag>harAnnotasjon(StillingsforholdId.class).negate(),
                        Assertions.<Underlag>harAnnotasjon(Integer.class).negate()
                )
        );
        assertThat(
                alle
                        .stream()
                        .filter(manglerAnnotasjonFraAarsunderlagetUnderlaget)
                        .collect(toList())
        )
                .as("observasjonsunderlag utan alle annotasjonar som årsunderlaget var annotert med")
                .hasSize(0);

        assertUnikeUnderlagsAnnotasjonar(alle, Integer.class).contains(123456789);
        assertUnikeUnderlagsAnnotasjonar(alle, StillingsforholdId.class).contains(new StillingsforholdId(18971237L));
    }

    /**
     * Verifiserer at observasjonsunderlaga blir annotert med observasjonsdatoen dei er tilrettelagt for å bli
     * observert på.
     */
    @Test
    public void skalAnnotereObservasjonsunderlagMedAarstall() {
        final List<Underlag> alle = observasjonsunderlagFactory.genererUnderlagPrMaaned(
                underlag(
                        new Aarstall(2000),
                        periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                        periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                        periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH)
                )
        )
                .collect(toList());

        final Predicate<Underlag> predikat = harAnnotasjon(Observasjonsdato.class);
        assertThat(alle.stream().filter(predikat.negate()).collect(toList()))
                .as("observasjonsunderlag som ikkje er annotert med observasjonsdato")
                .isEmpty();

        assertThat(alle.stream().map(paakrevdAnnotasjon(Observasjonsdato.class)).collect(toList()))
                .as("observasjonsdato annotert på årsunderlaga")
                .containsExactly(
                        new Observasjonsdato(dato("2000.01.31")),
                        new Observasjonsdato(dato("2000.02.29")),
                        new Observasjonsdato(dato("2000.03.31")),
                        new Observasjonsdato(dato("2000.04.30")),
                        new Observasjonsdato(dato("2000.05.31")),
                        new Observasjonsdato(dato("2000.06.30")),
                        new Observasjonsdato(dato("2000.07.31")),
                        new Observasjonsdato(dato("2000.08.31")),
                        new Observasjonsdato(dato("2000.09.30")),
                        new Observasjonsdato(dato("2000.10.31")),
                        new Observasjonsdato(dato("2000.11.30")),
                        new Observasjonsdato(dato("2000.12.31"))
                );
    }

    /**
     * Verifiserer at innsending av eit tomt underlag medfører at ingen observasjonsunderlag blir generert.
     */
    @Test
    public void skalIkkjeGenerereNokonObservasjonsunderlagVissAarsunderlagErTomt() {
        assertThat(observasjonsunderlagFactory
                        .genererUnderlagPrMaaned(new Underlag(Stream.empty()).annoter(Aarstall.class, new Aarstall(2007))).collect(toList())
        )
                .as("observasjonsunderlag generert frå tomt årsunderlage")
                .isEmpty();
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

        final List<Underlag> prMnd = observasjonsunderlagFactory
                .genererUnderlagPrMaaned(aarsunderlag)
                .collect(toList());
        rangeClosed(JANUARY.getValue(), NOVEMBER.getValue())
                .forEach(nr -> {
                    // Forventar at mnd nr X inneheld X synlige månedar + 1 fiktiv periode på slutten
                    assertObservasjonsunderlagMedFiktivPeriode(prMnd, nr - 1).hasSize(nr + 1);
                });
    }

    /**
     * Verifiserer at observasjonsunderlaget for desember ikkje er samme instans som årsunderlaget det
     * er generert ut i frå.
     */
    @Test
    public void skalGenerereNyttObservasjonsunderlagForDesemberSjoelvOmStillingaBlirAvsluttaIDesember() {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH),
                periode().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(APRIL),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(MAY),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(JUNE),
                periode().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(JULY),
                periode().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(AUGUST),
                periode().fraOgMed(dato("2000.09.01")).tilOgMed(dato("2000.09.30")).med(SEPTEMBER),
                periode().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(OCTOBER),
                periode().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(NOVEMBER),
                periode().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(DECEMBER)
        );
        final Underlag observasjonsunderlagDesember = observasjonsunderlagFactory
                .genererUnderlagPrMaaned(aarsunderlag)
                .reduce(this::last)
                .get();
        assertThat(observasjonsunderlagDesember)
                .as("observasjonsunderlag for desember skal ikkje vere identisk med årsunderlaget")
                .isNotSameAs(aarsunderlag);
    }

    /**
     * Verifiserer at observasjonsunderlaget for desember blir annotert med 31. desember som observasjonsdato.
     */
    @Test
    public void skalAnnotereObservasjonsunderlagForDesemberMaanedMedObservasjonsdato() {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH),
                periode().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(APRIL),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(MAY),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(JUNE),
                periode().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(JULY),
                periode().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(AUGUST),
                periode().fraOgMed(dato("2000.09.01")).tilOgMed(dato("2000.09.30")).med(SEPTEMBER),
                periode().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(OCTOBER),
                periode().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(NOVEMBER),
                periode().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(DECEMBER)
        );
        assertThat(
                observasjonsunderlagFactory
                        .genererUnderlagPrMaaned(aarsunderlag)
                        .reduce(this::last)
                        .get()
                        .valgfriAnnotasjonFor(Observasjonsdato.class)
        ).as("Observasjonsdato-annotasjon til observasjonsunderlag for desember 2000")
                .isEqualTo(of(new Observasjonsdato(dato("2000.12.31"))));
    }

    /**
     * Verifiserer at observasjonsunderlaget for desember ikkje inneheld ei fiktiv periode ettersom
     * observasjonsunderlaget i desember alltid vil bli likt årsunderlaget.
     */
    @Test
    public void skalIkkjeGenerereFiktivPeriodeUtAaretIObservasjonsunderlagetForDesember() {
        assertThat(
                observasjonsunderlagFactory
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
    public void skalIkkjeGenerereFiktivPeriodeUtAaretDersomSisteUnderlagsperiodeHarTildatoFoerObservasjonsdato() {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH),
                periode().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(APRIL),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.21")).med(MAY)
        );

        final List<Underlag> prMnd = observasjonsunderlagFactory.genererUnderlagPrMaaned(aarsunderlag).collect(toList());
        rangeClosed(JANUARY.getValue(), APRIL.getValue())
                .forEach(nr -> {
                    // Forventar at mnd nr X inneheld X synlige månedar + 1 fiktiv periode for resten av året
                    assertObservasjonsunderlagMedFiktivPeriode(prMnd, nr - 1).hasSize(nr + 1);
                    assertTilOgMed(prMnd.get(nr - 1).last().get()).isEqualTo(of(dato("2000.12.31")));
                });
        rangeClosed(MAY.getValue(), DECEMBER.getValue())
                .forEach(nr -> {
                    assertObservasjonsunderlagUtanFiktivPeriode(prMnd, nr - 1).hasSize(5);
                    assertTilOgMed(prMnd.get(nr - 1).last().get()).isEqualTo(of(dato("2000.05.21")));
                });
    }

    /**
     * Verifiserer at genereringa feilar dersom årsunderlaget inneheld perioder tilknytta meir enn eit årstall.
     */
    @Test
    public void skalFeileDersomAntattAarsunderlagetIkkjeErEitAarsunderlag() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Generering av observasjonsunderlag er kun støtta for årsunderlag");
        e.expectMessage("er ikkje eit årsunderlag sidan det ikkje er annotert med årstall");
        final Underlag underlag = underlag(
                periode().fraOgMed(dato("2001.12.01")).tilOgMed(dato("2001.12.31")).med(new Aarstall(2001)).med(DECEMBER),
                periode().fraOgMed(dato("2002.01.01")).tilOgMed(dato("2002.01.31")).med(new Aarstall(2002)).med(JANUARY)
        );
        observasjonsunderlagFactory.genererUnderlagPrMaaned(underlag);
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
        final Underlag aarsunderlag = underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH),
                periode().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(APRIL),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(MAY),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(JUNE),
                periode().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(JULY),
                periode().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(AUGUST)
        );
        assertObservasjonsunderlag(aarsunderlag).hasSize(12);
    }

    /**
     * Verifiserer at observasjonsunderlaga som blir generert frå og med underlagsperioda som er siste periode i
     * årsunderlaget, perioda som inneheld stillingsforholdets sluttmelding, ikkje får generert nokon fiktive perioder
     * etter siste periodes til og med-dato.
     * <p>
     * Intensjonen her er at inntil vi står etter sluttmeldinga til eit stillingsforhold så skal vi tru at det
     * vil forbli aktivt til evig tid (vel, iallefall resten av året).
     */
    @Test
    public void skalGenerereObservasjonsunderlagAvKorrektLengdeDersomStillingsforholdetBlirAvsluttaILoepetAvAaret() {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2012),
                periode().fraOgMed(dato("2012.01.01")).tilOgMed(dato("2012.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2012.02.01")).tilOgMed(dato("2012.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2012.03.01")).tilOgMed(dato("2012.03.31")).med(MARCH),
                periode().fraOgMed(dato("2012.04.01")).tilOgMed(dato("2012.04.30")).med(APRIL),
                periode().fraOgMed(dato("2012.05.01")).tilOgMed(dato("2012.05.31")).med(MAY),
                periode().fraOgMed(dato("2012.06.01")).tilOgMed(dato("2012.06.09")).med(JUNE),
                periode().fraOgMed(dato("2012.06.10")).tilOgMed(dato("2012.06.15"))
        );
        final List<Underlag> prMnd = observasjonsunderlagFactory.genererUnderlagPrMaaned(aarsunderlag).collect(toList());

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
        final Underlag aarsunderlag = underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(OCTOBER),
                periode().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(NOVEMBER),
                periode().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(DECEMBER)
        );
        assertObservasjonsunderlag(aarsunderlag).hasSize(3);
    }

    /**
     * Verifiserer at det kun blir generert ei fiktiv periode når en synlig periode sluttmeldes før observasjonsdato.
     */
    @Test
    public void skalGenerereFiktivPeriodeForUnderlagSomSluttmeldesPaaObservasjonsdato() throws Exception {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2015),
                periode().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.31")).med(JANUARY)
        );
        final List<Underlag> prMnd = observasjonsunderlagFactory.genererUnderlagPrMaaned(aarsunderlag).collect(toList());
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 0).hasSize(2);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 1).hasSize(1);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 11).hasSize(1);
    }

    /**
     * Verifiserer at det kun blir generert ei fiktiv periode og at den er basert på siste underlagsperiode i den siste
     * synlige månaden for observasjonsunderlaget, sjølv om siste synlig månad inneheld meir enn ei underlagsperiode.
     */
    @Test
    public void skalGenerereFiktivPeriodeUtAaretBasertPaaSisteUnderlagsperiodeISisteSynligeMaanadIkkjeAllePerioderISisteSynligeMaanad() {
        final Underlag aarsunderlag = underlag(
                new Aarstall(2015),
                periode().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.14")).med(JANUARY),
                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2015.02.01")).tilOgMed(dato("2015.02.28")).med(FEBRUARY)
        );
        final List<Underlag> prMnd = observasjonsunderlagFactory.genererUnderlagPrMaaned(aarsunderlag).collect(toList());
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 0).hasSize(3);
        assertObservasjonsunderlagMedFiktivPeriode(prMnd, 1).hasSize(4);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 2).hasSize(3);
        assertObservasjonsunderlagUtanFiktivPeriode(prMnd, 10).hasSize(3);
    }


    private static AbstractIterableAssert<?, ? extends Iterable<? extends Underlagsperiode>, Underlagsperiode> assertObservasjonsunderlagUtanFiktivPeriode(final List<Underlag> prMnd, final int index) {
        final Underlag underlag = prMnd.get(index);
        assertThat(underlag.last().get().valgfriAnnotasjonFor(FiktivPeriode.class))
                .as("FiktivPeriode-annotasjon for siste periode i observasjonsunderlaget " + underlag)
                .isEqualTo(empty());
        return assertThat(underlag).as("Observasjonsunderlag for " + Month.of(index + 1) + " (" + underlag + ")");
    }

    private static UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private static Underlag underlag(final Aarstall aarstall, final UnderlagsperiodeBuilder... perioder) {
        return underlag(perioder).annoter(Aarstall.class, aarstall);
    }

    private static Underlag underlag(final UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map(UnderlagsperiodeBuilder::bygg));
    }

    private AbstractListAssert<?, ? extends List<? extends Underlag>, Underlag> assertObservasjonsunderlag(final Underlag aarsunderlag) {
        return assertThat(
                generer(aarsunderlag).collect(toList())
        ).as("observasjonsunderlag generert basert på " + aarsunderlag);
    }

    private static AbstractIterableAssert<?, ? extends Iterable<? extends Underlagsperiode>, Underlagsperiode> assertObservasjonsunderlagMedFiktivPeriode(
            final List<Underlag> prMnd, final int index) {
        final Underlag underlag = prMnd.get(index);
        return assertThat(underlag).as("Observasjonsunderlag for " + Month.of(index + 1) + " (" + underlag + ")");
    }

    private Stream<Underlag> generer(final Underlag aarsunderlag) {
        return observasjonsunderlagFactory
                .genererUnderlagPrMaaned(aarsunderlag);
    }

    private Underlag etAarsunderlag() {
        return underlag(
                new Aarstall(2000),
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")).med(JANUARY),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.02.29")).med(FEBRUARY),
                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.03.31")).med(MARCH),
                periode().fraOgMed(dato("2000.04.01")).tilOgMed(dato("2000.04.30")).med(APRIL),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(MAY),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.06.30")).med(JUNE),
                periode().fraOgMed(dato("2000.07.01")).tilOgMed(dato("2000.07.31")).med(JULY),
                periode().fraOgMed(dato("2000.08.01")).tilOgMed(dato("2000.08.31")).med(AUGUST),
                periode().fraOgMed(dato("2000.09.01")).tilOgMed(dato("2000.09.30")).med(SEPTEMBER),
                periode().fraOgMed(dato("2000.10.01")).tilOgMed(dato("2000.10.31")).med(OCTOBER),
                periode().fraOgMed(dato("2000.11.01")).tilOgMed(dato("2000.11.30")).med(NOVEMBER),
                periode().fraOgMed(dato("2000.12.01")).tilOgMed(dato("2000.12.31")).med(DECEMBER)
        );
    }

    /**
     * Ein enkel operator som alltid returnerer verdien av parameter nr 2.
     * <p>
     * Kombinert med {@link Stream#reduce(java.util.function.BinaryOperator)} vil den her gjere ein i stand til å
     * hente ut siste verdi frå ein straum på ein enkel måte.
     *
     * @param ignored this value is totally ignored
     * @param value this value is always returned
     * @return <code>value</code>
     */
    private <T> T last(final T ignored, final T value) {
        return value;
    }
}
