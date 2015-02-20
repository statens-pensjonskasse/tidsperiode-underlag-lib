package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn.loennstrinn;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;

/**
 * Enheitstestar for {@link Stillingsendring}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsendringTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalFeileDersomStillingsprosentManglar() {
        e.expect(NoSuchElementException.class);
        new Stillingsendring().annoter(eiPeriode());
    }

    @Test
    public void skalAnnotereMedStillingsprosentVissEndringaErInnrapportertMedDet() {
        final Underlagsperiode periode = eiPeriode();
        eiEndring().annoter(periode);
        assertAnnotasjon(periode, Stillingsprosent.class).isEqualTo(of(fulltid()));
    }

    /**
     * Verifiserer at aksjonskode alltid blir annotert på underlagsperiode, enten med innrapportert verdi
     * eller som ukjent viss endringa er innrapportert utan aksjonskode.
     */
    @Test
    public void skalAnnotereMedAksjonskode() {
        final Underlagsperiode periodeUtanAksjonskode = eiPeriode();
        eiEndring().annoter(periodeUtanAksjonskode);
        assertAnnotasjon(periodeUtanAksjonskode, Aksjonskode.class).isEqualTo(of(Aksjonskode.UKJENT));

        final Underlagsperiode periodeMedAksjonskode = eiPeriode();
        eiEndring().aksjonskode(Aksjonskode.NYTILGANG).annoter(periodeMedAksjonskode);
        assertAnnotasjon(periodeMedAksjonskode, Aksjonskode.class).isEqualTo(of(Aksjonskode.NYTILGANG));
    }

    /**
     * Verifiserer at underlagsperiodene ikkje blir annotert med lønnstrinn sjølv for
     * endringar som er innrapportert med lønnstrinn.
     * <p>
     * Vi annoterer ikkje med lønnstrinn sidan det skal handterast av lønnstrinnoppslaget slik at både lønnstrinn
     * og gjeldande lønnstrinnbeløp blir annotert som ei atomisk enheit.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedLoennstrinn() {
        final Underlagsperiode periodeUtanLoennstrinn = eiPeriode();
        eiEndring().annoter(periodeUtanLoennstrinn);
        assertAnnotasjon(periodeUtanLoennstrinn, Loennstrinn.class).isEqualTo(empty());

        final Underlagsperiode periodeMedLoennstrinn = eiPeriode();
        eiEndring().loennstrinn(of(loennstrinn(45))).annoter(periodeMedLoennstrinn);
        assertAnnotasjon(periodeMedLoennstrinn, Loennstrinn.class).isEqualTo(of(loennstrinn(45)));
    }

    @Test
    public void skalAnnotereMedDeltidsjustertLoennVissEndringaErInnrapportertMedDet() {
        verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
                DeltidsjustertLoenn.class,
                new DeltidsjustertLoenn(kroner(10_000)),
                Stillingsendring::loenn
        );
    }

    @Test
    public void skalAnnotereMedVariabletilleggVissEndringaErInnrapportertMedDet() {
        verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
                Variabletillegg.class,
                new Variabletillegg(kroner(10_000)),
                Stillingsendring::variabletillegg
        );
    }

    @Test
    public void skalAnnotereMedFastetilleggVissEndringaErInnrapportertMedDet() {
        verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
                Fastetillegg.class,
                new Fastetillegg(kroner(10_000)),
                Stillingsendring::fastetillegg
        );
    }

    @Test
    public void skalAnnotereMedFunksjonstilleggVissEndringaErInnrapportertMedDet() {
        verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
                Funksjonstillegg.class,
                new Funksjonstillegg(kroner(10_000)),
                Stillingsendring::funksjonstillegg
        );
    }

    @Test
    public void skalAnnotereMedStillingskodeVissEndringaErInnrapportertMedDet() {
        verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
                Stillingskode.class,
                Stillingskode.K_STIL_APO_FARMASOYT,
                Stillingsendring::stillingskode
        );
    }

    private static Underlagsperiode eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2010.01.01"))
                .tilOgMed(dato("2010.01.31"))
                .bygg();
    }

    /**
     * Opprettar ei ny stillingsendring med alle påkrevde verdiar populert.
     *
     * @return
     */
    private static Stillingsendring eiEndring() {
        return new Stillingsendring()
                .stillingsprosent(fulltid());
    }

    private static <T> void verifiserAtAnnoteringTaklarBaadeManglandeOgTilstadeverandeVerdi(
            final Class<T> type, final T value,
            final BiFunction<Stillingsendring, Optional<T>, ?> populator) {
        verifiserAnnotering(type, empty(), populator);
        verifiserAnnotering(type, of(value), populator);
    }

    private static <T> void verifiserAnnotering(
            final Class<T> type, final Optional<T> value,
            final BiFunction<Stillingsendring, Optional<T>, ?> populator) {
        final Stillingsendring endring = eiEndring();
        populator.apply(endring, value);

        final Underlagsperiode periode = eiPeriode();
        endring.annoter(periode);
        assertAnnotasjon(periode, type).isEqualTo(value);
    }
}