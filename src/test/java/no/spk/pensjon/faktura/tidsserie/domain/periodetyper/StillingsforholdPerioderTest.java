package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPerioder}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPerioderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at gjeldande stillingsendring blir plukka basert p� kva endring som er nyligast registrert, under
     * antagelsen om at den trulig er mest korrekt.
     */
    @Test
    public void skalBrukeSistRegistrerteStillingsendringSomGjeldendeEndringVissPeriodenOverlapperMerEnnEnEndring() {
        final StillingsforholdPeriode periode = new StillingsforholdPeriode(dato("2006.02.01"), empty());

        final Stillingsendring sistRegistrerteEndring = endring()
                .aksjonsdato(dato("2006.02.01"))
                .registreringsdato(dato("2012.10.07"))
                .aksjonskode(Aksjonskode.ENDRINGSMELDING)
                        .stillingsprosent(new Stillingsprosent(new Prosent("50%")));
        periode.leggTilOverlappendeStillingsendringer(
                asList(
                        endring()
                                .aksjonsdato(dato("2006.02.01"))
                                .registreringsdato(dato("2006.09.15"))
                                .aksjonskode(Aksjonskode.NYTILGANG)
                                .stillingsprosent(new Stillingsprosent(new Prosent("100%"))),
                        sistRegistrerteEndring,
                        endring()
                                .aksjonsdato(dato("2006.02.01"))
                                .registreringsdato(dato("2006.10.07"))
                                .aksjonskode(Aksjonskode.NYTILGANG)
                                .stillingsprosent(new Stillingsprosent(new Prosent("50%")))
                )
        );
        assertThat(periode.gjeldendeEndring().map(Stillingsendring::registreringsdato)).as("gjeldende stillingsendring for " + periode)
                .isEqualTo(of(sistRegistrerteEndring.registreringsdato()));
    }

    /**
     * Verifiserer at medregning er p�krevd ved konstruksjon og at �nska feilmelding blir generert viss ein pr�ver
     * � sende inn <code>null</code> som verdi.
     */
    @Test
    public void skalKreveMedregningVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("medregning er p�krevd, men var null");
        new StillingsforholdPeriode(null);
    }

    /**
     * Verifiserer at stillingsforholdnummer er p�krevd ved konstruksjon slik at ein unng�r at alle brukarane av klassa
     * m� legge til null-sjekkar kvar gang dei skal pr�ve � bruke denne verdien seinare.
     */
    @Test
    public void skalKreveStillingsforholdIdVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("stillingsforhold er p�krevd, men var null");
        new StillingsforholdPerioder(null, new ArrayList<>());
    }

    /**
     * Verifiserer at ei samling med perioder er p�krevd ved konstruksjon slik at ein unng�r at alle brukarane av klassa
     * m� legge til null-sjekkar kvar gang dei skal pr�ve � bruke denne verdien seinare.
     */
    @Test
    public void skalKrevePerioderVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("perioder er p�krevd, men var null");
        new StillingsforholdPerioder(new StillingsforholdId(1l), null);
    }

    @Test
    public void skalTaVareP�PeriodeneSomBlirSendtInnVedKonstruksjon() {
        final StillingsforholdPerioder stillingsforhold = new StillingsforholdPerioder(
                new StillingsforholdId(1L),
                asList(
                        new StillingsforholdPeriode(dato("2000.01.01"), empty()
                        )
                )
        );
        assertThat(stillingsforhold.perioder()).hasSize(1);
    }

    private static Stillingsendring endring() {
        return new Stillingsendring();
    }
}