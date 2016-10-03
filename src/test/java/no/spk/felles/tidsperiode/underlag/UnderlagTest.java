package no.spk.felles.tidsperiode.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Enheitstestar for {@link Underlag}.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at underlaget blir annotert med alle annotasjonar som kilda er annotert med.
     */
    @Test
    public void skalAnnotereUnderlagMedAlleAnnotasjonarSomKildaErAnnotertMed() {
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
    public void skalInkludereAnnotasjonarVedAvgrensingAvunderlag() {
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
    public void skalEkspandereOptionalSinVerdiVedRegistreringAvAnnotasjon() {
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
    public void skalIkkjeRegistrereEinVerdiVissVerdiErEinTomOptional() {
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
    public void skalFeileVissAnnotasjonstypeErOptional() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Annotasjonar av type Optional er ikkje støtta, viss du vil legge til ein valgfri annotasjon må den registrerast under verdiens egen type");
        eitTomtUnderlag().annoter(Optional.class, empty());
    }

    /**
     * Verifiserer at oppslag av valgfri annotasjon fungerer når underlaget har ein verdi for annotasjonen.
     */
    @Test
    public void skalKunneHenteUtVerdiarForValgfrieAnnotasjonar() {
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
    public void skalFeileVedOppslagAvPaakrevdAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage("U[] manglar ein påkrevd annotasjon av type");
        e.expectMessage(Integer.class.getSimpleName());

        eitTomtUnderlag().annotasjonFor(Integer.class);
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar via
     * {@link Underlag#valgfriAnnotasjonFor(Class)} ikkje
     * feilar og returnerer ein tom verdi dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        assertThat(eitTomtUnderlag().valgfriAnnotasjonFor(Long.class)).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlaget validerer og dermed garanterer, at ingen av underlagsperiodene overlappar nokon
     * av dei andre underlagsperiodene i underlaget.
     */
    @Test
    public void skalIkkjeKunneKonstruereUnderlagMedOverlappandeUnderlagsperioder() {
        e.expect(AssertionError.class);
        e.expectMessage("Eit underlag kan ikkje inneholde underlagsperioder som overlappar kvarandre");
        e.expectMessage("2015-01-15->2015-12-31");
        e.expectMessage("2015-01-15->2015-01-31");
        e.expectMessage("2015-02-01->2015-02-28");
        create(
                periode().fraOgMed(dato("2015.01.01")).tilOgMed(dato("2015.01.14")),
                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.01.31")),
                periode().fraOgMed(dato("2015.01.15")).tilOgMed(dato("2015.12.31")),
                periode().fraOgMed(dato("2015.02.01")).tilOgMed(dato("2015.02.28"))
        );
    }

    /**
     * Verifiserer at underlaget validerer at underlagsperiodene blir sendt inn i kronologisk rekkefølge.
     */
    @Test
    public void skalInneholdeUnderlagsperioderIKronologiskRekkefoelge() {
        e.expect(AssertionError.class);
        e.expectMessage("underlaget krever at underlagsperiodene er sortert i kronologisk rekkefølge");
        final Underlagsperiode b = periode().fraOgMed(dato("2000.03.01")).tilOgMed(dato("2000.08.14")).bygg();
        final Underlagsperiode c = periode().fraOgMed(dato("2000.08.15")).tilOgMed(dato("2000.12.31")).bygg();
        final Underlagsperiode a = periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.02.29")).bygg();
        new Underlag(Stream.of(c, a, b));
    }

    /**
     * Verifiserer at konstruksjon av nye underlag feilar dersom det eksisterer tidsgap mellom ei eller fleire av
     * underlagsperiodene.
     */
    @Test
    public void skalIkkjeKunneKonstruereUnderlagMedTidsgapMellomUnderlagsperiodene() {
        e.expect(AssertionError.class);
        e.expectMessage("kan ikkje inneholde tidsgap");
        e.expectMessage("31 dagar tidsgap mellom");
        e.expectMessage("2000-01-01->2000-04-30");
        e.expectMessage("2000-06-01->2000-12-31");

        create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31"))
        );
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate) avgrensing} av nye underlag feilar dersom
     * det eksisterer tidsgap mellom ei eller fleire av underlagsperiodene etter at predikatet har filtrert bort
     * uønska underlagsperioder.
     */
    @Test
    public void skalIkkjeKunneAvgrenseUnderlagSlikAtDetOppstaarTidsgapMellomUnderlagsperiodene() {
        e.expect(AssertionError.class);

        final Underlag uavgrensa = create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.04.30")).med(2),
                periode().fraOgMed(dato("2000.05.01")).tilOgMed(dato("2000.05.31")).med(3),
                periode().fraOgMed(dato("2000.06.01")).tilOgMed(dato("2000.12.31")).med(2)
        );
        uavgrensa.restrict(p -> p.annotasjonFor(Integer.class) == 2);
    }

    /**
     * Verifiserer at {@link Underlag#restrict(java.util.function.Predicate)} fjernar alle uønska underlagsperioder frå
     * det nye underlaget
     */
    @Test
    public void skalFjerneAlleUoenskaUnderlagsperioderVedAvgrensing() {
        final Underlag uavgrensa = create(
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
    @Test
    public void skalReturnereKronologiskSistePeriodeFraUnderlaget() {
        final Optional<Underlagsperiode> sistePeriode = create(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.01.31")),
                periode().fraOgMed(dato("2000.02.01")).tilOgMed(dato("2000.12.31"))
        ).last();
        assertThat(
                sistePeriode
                        .map(Underlagsperiode::fraOgMed)
        ).as("fra og med-dato for underlagsperiode " + sistePeriode)
                .isEqualTo(of(dato("2000.02.01")));
    }

    /**
     * Verifiserer at {@link Underlag#last()} returnerer ein tom verdi dersom underlaget er tomt.
     */
    @Test
    public void skalReturnereEmptyDersomUnderlagetIkkjeInneheldNokonUnderlagsperiode() {
        assertThat(eitTomtUnderlag().last()).isEqualTo(Optional.empty());
    }

    private UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private Underlag create(UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map(UnderlagsperiodeBuilder::bygg));
    }

    private static Underlag eitTomtUnderlag() {
        return new Underlag(Stream.empty());
    }
}