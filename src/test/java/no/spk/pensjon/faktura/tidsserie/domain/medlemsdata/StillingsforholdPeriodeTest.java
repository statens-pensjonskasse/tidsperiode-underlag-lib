package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode.medregning;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.ObjectMother.eiMedregning;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link StillingsforholdPeriode}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPeriodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at ei perioder som er i ei ugyldig tilstand uten medregning eller stillingsendringar, feilar
     * om ein forsøker å sjekke om den tilhøyrer eit stillingsforhold.
     * <p>
     * Oppførselen er valgt for å gjere det enklare å lokalisere/feilsøke problem knytta til feil i periodiseringa av
     * stillingsforholdet som kan føre til denne ugyldige tilstanda.
     */
    @Test
    public void skalFeileTilhoeyrerSjekkDersomTilstandErUgyldig() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Stillingsforholdperioda SP[2005-08-15,->] er i ei ugyldig tilstand, den har verken ei medregning eller ei stillingsendring");
        new StillingsforholdPeriode(dato("2005.08.15"), empty()).tilhoeyrer(new StillingsforholdId(1L));
    }

    @Test
    public void skalFeileTilhoeyrerSjekkDersomTilstandErUgyldig2() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Stillingsforholdperioda SP[2005-08-15,->] er i ei ugyldig tilstand, den har verken ei medregning eller ei stillingsendring");
        new StillingsforholdPeriode(dato("2005.08.15"), empty()).stillingsforhold();
    }

    /**
     * Verifiserer at ei periode tilhøyrer stillingsforholdet som gjeldande stillingsendringa er tilknytta.
     */
    @Test
    public void skalTilhoeyreGjeldandeStillingsendringSittStillingsforhold() {
        final StillingsforholdId expected = new StillingsforholdId(1L);
        final StillingsforholdId notExpected = new StillingsforholdId(2L);

        final StillingsforholdPeriode periode = new StillingsforholdPeriode(
                dato("2005.01.01"), empty())
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(dato("2005.01.01"))
                                .stillingsforhold(expected)
                );
        assertTilhoeyrer(periode, expected).isTrue();
        assertTilhoeyrer(periode, notExpected).isFalse();
    }

    /**
     * Verifiserer at ei periode tilhøyrer stillingsforholdet som medregninga er tilknytta.
     */
    @Test
    public void skalTilhoeyreMedregningaSittStillingsforhold() {
        final StillingsforholdId expected = new StillingsforholdId(1L);
        final StillingsforholdId notExpected = new StillingsforholdId(2L);

        final StillingsforholdPeriode periode = new StillingsforholdPeriode(
                eiMedregning()
                        .stillingsforhold(expected)
                        .bygg()
        );
        assertTilhoeyrer(periode, expected).isTrue();
        assertTilhoeyrer(periode, notExpected).isFalse();
    }

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

    private static Stillingsendring endring() {
        return new Stillingsendring();
    }

    private static AbstractBooleanAssert<?> assertTilhoeyrer(StillingsforholdPeriode periode, StillingsforholdId expected) {
        return assertThat(periode.tilhoeyrer(expected)).as("perioda tilhøyrer " + expected);
    }
}