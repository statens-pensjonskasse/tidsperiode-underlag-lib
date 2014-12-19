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
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
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
     * Verifiserer at gjeldande stillingsendring kan vere ei sluttmelding viss stillingsforholdet kun inneheld ei
     * sluttmelding og det ikkje eksisterer nokon ingen andre stillingsendringar.
     */
    @Test
    public void skalBrukeSluttmeldingSomGjeldandeEndringDersomStillingaIkkjeInneheldNokonAndreStillingsendringar() {
        final StillingsforholdPeriode periode = new StillingsforholdPeriode(dato("2006.02.01"), of(dato("2006.02.01")));

        final Stillingsendring sluttmelding = endring()
                .aksjonsdato(dato("2006.02.01"))
                .registreringsdato(dato("2006.03.01"))
                .aksjonskode(Aksjonskode.SLUTTMELDING)
                .stillingsprosent(fulltid());
        periode.leggTilOverlappendeStillingsendringer(
                sluttmelding
        );
        assertThat(periode.gjeldendeEndring()).as("gjeldende stillingsendring for " + periode)
                .isEqualTo(of(sluttmelding));
    }


    /**
     * Verifiserer at gjeldande stillingsendring aldri kan peike til ei sluttmelding viss periode inneheld meir enn
     * ei stillingsendring.
     * <p>
     * Bakgrunnen for denne begrensinga er at viss ein plukkar ei sluttmelding som gjeldande stillingsendring
     * for ei periode så misser ein muligheita til å vite kva som har vore gjeldande aksjonskode for perioda.
     * <p>
     * Det vil kunne gi effektar som at ein ikkje er i stand til å sjå at medlemmet har vore ute i permisjon utan
     * lønn heile siste periode av stillingsforholdet før det var avslutta og dermed får beregna ei langt høgare
     * lønn for perioda enn det som ville blitt beregna om ein ikkje hadde plukka sluttmeldinga som gjeldande endring.
     */
    @Test
    public void skalBrukeSistRegistrerteStillingsendringSomIkkjeErSluttmelding() {
        final StillingsforholdPeriode periode = new StillingsforholdPeriode(dato("2006.02.01"), of(dato("2006.12.31")));

        final Stillingsendring permisjon = endring()
                .aksjonsdato(dato("2006.02.01"))
                .registreringsdato(dato("2006.03.01"))
                .aksjonskode(Aksjonskode.PERMISJON_UTAN_LOENN)
                .stillingsprosent(fulltid());
        periode.leggTilOverlappendeStillingsendringer(
                asList(
                        permisjon,
                        endring()
                                .aksjonsdato(dato("2006.12.31"))
                                .registreringsdato(dato("2007.02.15"))
                                .aksjonskode(Aksjonskode.SLUTTMELDING)
                                .stillingsprosent(fulltid())
                )
        );
        assertThat(periode.gjeldendeEndring()).as("gjeldende stillingsendring for " + periode)
                .isEqualTo(of(permisjon));
    }

    /**
     * Verifiserer at gjeldande stillingsendring blir plukka basert på kva endring som er nyligast registrert, under
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
     * Verifiserer at medregning er påkrevd ved konstruksjon og at ønska feilmelding blir generert viss ein prøver
     * å sende inn <code>null</code> som verdi.
     */
    @Test
    public void skalKreveMedregningVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("medregning er påkrevd, men var null");
        new StillingsforholdPeriode(null);
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
        new StillingsforholdPerioder(new StillingsforholdId(1l), null);
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
        assertThat(stillingsforhold.perioder()).hasSize(1);
    }

    private static Stillingsendring endring() {
        return new Stillingsendring();
    }
}