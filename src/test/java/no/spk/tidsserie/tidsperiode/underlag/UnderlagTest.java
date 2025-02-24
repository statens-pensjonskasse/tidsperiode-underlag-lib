package no.spk.tidsserie.tidsperiode.underlag;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.tidsserie.tidsperiode.Datoar.dato;
import static no.spk.tidsserie.tidsperiode.underlag.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.offset;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Enheitstestar for {@link Underlag}.
 *
 * @author Tarjei Skorgenes
 */
class UnderlagTest {
    /**
     * Verifiserer at underlaget blir annotert med alle annotasjonar som kilda er annotert med.
     */
    @Test
    void skalAnnotereUnderlagMedAlleAnnotasjonarSomKildaErAnnotertMed() {
        final String tekst = "Hello World";
        final double flyttall = 1d;
        final long langtTall = Long.MAX_VALUE;

        final Underlag kilde = eitTomtUnderlag();
        kilde.annoter(String.class, tekst);
        kilde.annoter(Long.class, langtTall);
        kilde.annoter(Double.class, of(flyttall));

        final Underlag underlag = eitTomtUnderlag();
        underlag.annoterFra(kilde);

        assertThat(underlag.annotasjonFor(Long.class)).isEqualTo(langtTall);
        assertThat(underlag.annotasjonFor(Double.class)).isEqualTo(flyttall, offset(0.1d));
        assertThat(underlag.annotasjonFor(String.class)).isEqualTo(tekst);
    }

    /**
     * Verifiserer at underlaget generert av {@link Underlag#restrict(java.util.function.Predicate)}
     * er annotert med samme annotasjonar som det opprinnelige underlaget.
     * <p>
     * Merk at denne oppførselen impliserer at klienten er ansvarlig for å sikre eventuelle konsistensproblem
     * viss nokon av annotasjonane til underlaget baserer seg på tilstand henta frå ei underlagsperiode som ikkje
     * lenger inngår i underlaget.
     */
    @Test
    void skalInkludereAnnotasjonarVedAvgrensingAvunderlag() {
        final Integer expected = 1282678961;

        final Underlag old = eitTomtUnderlag();
        old.annoter(Integer.class, expected);
        assertThat(old.restrict(p -> true).annotasjonFor(Integer.class)).isSameAs(expected);
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    void skalEkspandereOptionalSinVerdiVedRegistreringAvAnnotasjon() {
        final Integer expected = 1;

        final Underlag underlag = eitTomtUnderlag();
        underlag.annoter(Integer.class, of(expected));
        assertThat(underlag.annotasjonFor(Integer.class)).isEqualTo(expected);
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    void skalIkkjeRegistrereEinVerdiVissVerdiErEinTomOptional() {
        final Underlag periode = eitTomtUnderlag();
        periode.annoter(Integer.class, empty());
        assertThat(periode.valgfriAnnotasjonFor(Integer.class).isPresent()).isFalse();
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * eit underlag skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    void skalFeileVissAnnotasjonstypeErOptional() {
        assertThatCode(
                () ->
                        eitTomtUnderlag()
                                .annoter(Optional.class, empty())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Annotasjonar av type Optional er ikkje støtta, viss du vil legge til ein valgfri annotasjon må den registrerast under verdiens egen type")
        ;
    }

    /**
     * Verifiserer at oppslag av valgfri annotasjon fungerer når underlaget har ein verdi for annotasjonen.
     */
    @Test
    void skalKunneHenteUtVerdiarForValgfrieAnnotasjonar() {
        final Object verdi = "valgfrie annotasjonar fungerer fint";

        final Underlag underlag = eitTomtUnderlag();
        underlag.annoter(Object.class, verdi);

        assertThat(underlag.valgfriAnnotasjonFor(Object.class)).isEqualTo(of(verdi));
    }

    /**
     * Verifiserer at oppslag av påkrevde annotasjonar via
     * {@link Underlag#annotasjonFor(Class)} feilar med
     * ein exception dersom underlaget ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    void skalFeileVedOppslagAvPaakrevdAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        assertThatCode(
                () ->
                        eitTomtUnderlag()
                                .annotasjonFor(Integer.class)
        )
                .isInstanceOf(PaakrevdAnnotasjonManglarException.class)
                .hasMessageContaining("U[] manglar ein påkrevd annotasjon av type")
                .hasMessageContaining(Integer.class.getSimpleName())
        ;
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar via
     * {@link Underlag#valgfriAnnotasjonFor(Class)} ikkje
     * feilar og returnerer ein tom verdi dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        assertThat(eitTomtUnderlag().valgfriAnnotasjonFor(Long.class)).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlaget validerer og dermed garanterer, at ingen av underlagsperiodene overlappar nokon
     * av dei andre underlagsperiodene i underlaget.
     */
    @Test
    void skalIkkjeKunneKonstruereUnderlagMedOverlappandeUnderlagsperioder() {
        assertThatCode(
                () ->
                        underlag(
                                periode().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.14")),
                                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.01.31")),
                                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.12.31")),
                                periode().fraOgMed(dato("2015.02.01")).tilOgMed(dato("2015.02.28"))
                        )
        )
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Eit underlag kan ikkje inneholde underlagsperioder som overlappar kvarandre")
                .hasMessageContaining("2015-01-15->2015-12-31")
                .hasMessageContaining("2015-01-15->2015-01-31")
                .hasMessageContaining("2015-02-01->2015-02-28")
        ;
    }

    /**
     * Verifiserer at underlaget validerer at underlagsperiodene blir sendt inn i kronologisk rekkefølge.
     */
    @Test
    void skalInneholdeUnderlagsperioderIKronologiskRekkefoelge() {
        assertThatCode(
                () ->
                        underlag(
                                periode().fraOgMed(dato("2000.08.15")).tilOgMed(dato("2000.12.31")),
                                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.02.29")),
                                periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.08.14"))
                        )
        )
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("underlaget krever at underlagsperiodene er sortert i kronologisk rekkefølge")
        ;
    }

    /**
     * Verifiserer at konstruksjon av nye underlag feilar dersom det eksisterer tidsgap mellom ei eller fleire av
     * underlagsperiodene.
     */
    @Test
    void skalIkkjeKunneKonstruereUnderlagMedTidsgapMellomUnderlagsperiodene() {
        assertThatCode(
                () ->
                        underlag(
                                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")),
                                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31"))
                        )
        )
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("kan ikkje inneholde tidsgap")
                .hasMessageContaining("31 dagar tidsgap mellom")
                .hasMessageContaining("2000-01-01->2000-04-30")
                .hasMessageContaining("2000-06-01->2000-12-31")
        ;
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate) avgrensing} av nye underlag feilar dersom
     * det eksisterer tidsgap mellom ei eller fleire av underlagsperiodene etter at predikatet har filtrert bort
     * uønska underlagsperioder.
     */
    @Test
    void skalIkkjeKunneAvgrenseUnderlagSlikAtDetOppstaarTidsgapMellomUnderlagsperiodene() {
        assertThatCode(
                () -> {
                    final Underlag uavgrensa = underlag(
                            periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")).med(2),
                            periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(3),
                            periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31")).med(2)
                    );
                    uavgrensa.restrict(p -> p.annotasjonFor(Integer.class) == 2);
                }
        )
                .isInstanceOf(AssertionError.class);
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate)} fjernar alle uønska underlagsperioder frå
     * det nye underlaget
     */
    @Test
    void skalFjerneAlleUoenskaUnderlagsperioderVedAvgrensing() {
        final Underlag uavgrensa = underlag(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")).med(2),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.12.31")).med(3)
        );
        final Underlag avgrensa = uavgrensa.restrict(p -> p.annotasjonFor(Integer.class) == 2);
        assertThat(
                avgrensa
                        .stream()
                        .map(p -> p.valgfriAnnotasjonFor(Integer.class))
                        .map(Optional::get)
                        .collect(toList())
        ).containsOnly(2);
    }

    /**
     * Verifiserer at {@link Underlag#last()} returnerer den kronologisk siste underlagsperioda i underlaget.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void skalReturnereKronologiskSistePeriodeFraUnderlaget() {
        assertThat(
                underlag(
                        periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")),
                        periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.12.31"))
                )
                        .last()
                        .get()
        )
                .harFraOgMed("2000.02.01");
    }

    /**
     * Verifiserer at {@link Underlag#last()} returnerer ein tom verdi dersom underlaget er tomt.
     */
    @Test
    void skalReturnereEmptyDersomUnderlagetIkkjeInneheldNokonUnderlagsperiode() {
        assertThat(eitTomtUnderlag().last()).isEqualTo(Optional.empty());
    }

    private UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private Underlag underlag(UnderlagsperiodeBuilder... perioder) {
        return new Underlag(Arrays.stream(perioder).map(UnderlagsperiodeBuilder::bygg));
    }

    private static Underlag eitTomtUnderlag() {
        return new Underlag(Stream.empty());
    }
}