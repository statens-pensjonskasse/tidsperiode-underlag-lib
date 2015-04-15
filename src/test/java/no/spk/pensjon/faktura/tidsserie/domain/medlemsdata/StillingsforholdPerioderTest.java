package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPerioder}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPerioderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at frå og med- og til og med-dato blir henta frå henholdsvis første og siste periode.
     */
    @Test
    public void skalHenteStillingsforholdetSinFraaOgMedOgTilOgMedDatoFraaFoersteOgSistePeriode() {
        final LocalDate fraOgMed = dato("2005.08.15");
        final Optional<LocalDate> tilogMed = empty();

        final StillingsforholdPerioder stillingsforhold = new StillingsforholdPerioder(
                new StillingsforholdId(2L),
                Stream.of(
                        new StillingsforholdPeriode(fraOgMed, of(dato("2005.12.31"))),
                        new StillingsforholdPeriode(dato("2006.01.01"), of(dato("2015.12.01"))),
                        new StillingsforholdPeriode(dato("2012.12.02"), tilogMed)

                )
        );
        assertThat(stillingsforhold.fraOgMed()).as("stillingsforholdet sin fra og med-dato").isEqualTo(fraOgMed);
        assertThat(stillingsforhold.tilOgMed()).as("stillingsforholdet sin til og med-dato").isEqualTo(tilogMed);
    }

    /**
     * Verifiserer at det ikkje er mulig  å opprette ein ny instans av StillingsforholdPerioder utan 1 eller fleire
     * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode}r.
     */
    @Test
    public void skalKreveMinstEiPeriodeVedKonstruksjon() {
        assumeAssertionsEnabled();

        try {
            new StillingsforholdPerioder(new StillingsforholdId(1L), Stream.empty());
            fail("StillingsforholdPerioder skal kreve minst 1 periode ved konstruksjon, men feila ikkje når vi sendte inn 0 perioder");
        } catch (final AssertionError e) {
            // Brukar ikkje ExpectedException her sidan den ikkje plukkar opp dersom feilmeldinga som blir kasta er
            // noko anna enn forventa når det er AssertionError som blir kasta
            assertThat(e).hasMessageContaining("Forventa minst 1 stillingsforholdperiode, men var 0");
        }
    }

    /**
     * Verifiserer at det blir verifisert at periodene blir sendt inn i kronologisk sortert rekkefølge.
     * <p>
     * Merk at dette kravet av ytelsesmessige hensyn kun gjeld når assertions er aktivert.
     */
    @Test
    public void skalKreveAtPeriodeneErSortertKronologisk() {
        assumeAssertionsEnabled();

        try {
            new StillingsforholdPerioder(
                    new StillingsforholdId(1L),
                    Stream.of(
                            new StillingsforholdPeriode(dato("2000.01.01"), empty()),
                            new StillingsforholdPeriode(dato("1917.01.01"), of(dato("1999.12.31")))
                    )
            );
            fail("StillingsforholdPerioder skal kreve at periodene er sortert, men den gjorde ikkje det");
        } catch (final AssertionError e) {
            // Brukar ikkje ExpectedException her sidan den ikkje plukkar opp dersom feilmeldinga som blir kasta er
            // noko anna enn forventa når det er AssertionError som blir kasta
            assertThat(e).hasMessageContaining("Stillingsforholdperiodene må vere sortert i kronologisk rekkefølge, men var ikkje det");
        }
    }

    /**
     * Verifiserer at stillingsforholdnummer er påkrevd ved konstruksjon slik at ein unngår at alle brukarane av klassa
     * må legge til null-sjekkar kvar gang dei skal prøve å bruke denne verdien seinare.
     */
    @Test
    public void skalKreveStillingsforholdIdVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("stillingsforhold er påkrevd, men var null");
        new StillingsforholdPerioder(null, new ArrayList<>());
    }

    /**
     * Verifiserer at ei samling med perioder er påkrevd ved konstruksjon slik at ein unngår at alle brukarane av klassa
     * må legge til null-sjekkar kvar gang dei skal prøve å bruke denne verdien seinare.
     */
    @Test
    public void skalKrevePerioderVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("perioder er påkrevd, men var null");
        new StillingsforholdPerioder(new StillingsforholdId(1l), (List) null);
    }

    @Test
    public void skalTaVarePåPeriodeneSomBlirSendtInnVedKonstruksjon() {
        final StillingsforholdPerioder stillingsforhold = new StillingsforholdPerioder(
                new StillingsforholdId(1L),
                asList(
                        new StillingsforholdPeriode(dato("2000.01.01"), empty()
                        )
                )
        );
        assertThat(stillingsforhold.stream().collect(toList())).hasSize(1);
    }

    private static Stillingsendring endring() {
        return new Stillingsendring();
    }

    private static void assumeAssertionsEnabled() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true; // http://stackoverflow.com/questions/13029915/how-to-programmatically-test-if-assertions-are-enabled
        assumeTrue("Testen blir kun verifisert når assertions er aktivert", assertsEnabled);
    }
}