package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

import java.time.Month;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StandardTidsserieAnnoteringTest {
    private final StandardTidsserieAnnotering annotator = new StandardTidsserieAnnotering();

    private final Underlag underlag = mock(Underlag.class);

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med stillingsprosent dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedStillingsprosentDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Stillingsprosent.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med deltidsjustert lønn dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedDeltidsjustertLoennDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), DeltidsjustertLoenn.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med lønnstrinn dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedLoennstinnDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Loennstrinn.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperiode ikkje blir annotert med årstall dersom
     * den ikkje har ei kobling til månedsperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedMaanedIAarDersomMaanedsperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Month.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med årstall dersom
     * den ikkje har ei kobling til årsperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedAarstallDersomAarsperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Aarstall.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med lønnstrinn dersom den er tilknytta
     * ei stillingsforholdperiode der gjeldande stillingsendring er innrapportert med lønnstrinn.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedLoennstrinnFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode().bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1985.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1985.01.01"))
                        .loennstrinn(of(new Loennstrinn(32)))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), Loennstrinn.class)
                .isEqualTo(of(new Loennstrinn(32)));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med deltidsjustert lønn dersom den er tilknytta
     * ei stillingsforholdperiode der gjeldande stillingsendring er innrapportert med lønn.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedDeltidsjustertLoennFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode().bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1995.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1995.01.01"))
                        .loenn(of(new DeltidsjustertLoenn(new Kroner(600_000))))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), DeltidsjustertLoenn.class)
                .isEqualTo(of(new DeltidsjustertLoenn(new Kroner(600_000))));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med stillingsprosent dersom den er tilknytta
     * ei stillingsforholdperiode.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedStillingsprosentFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode().bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1985.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1985.01.01"))
                        .stillingsprosent(new Stillingsprosent(new Prosent("14%")))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), Stillingsprosent.class)
                .isEqualTo(of(new Stillingsprosent(new Prosent("14%"))));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med samme årstall som årsperioda den er tilknytta.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedMaanedIAarFraTilkoblaMaanedsperiode() {
        final Underlagsperiode periode = new UnderlagsperiodeBuilder()
                .fraOgMed(dato("1990.04.01"))
                .tilOgMed(dato("1990.04.30"))
                .bygg();
        periode.kobleTil(new Maaned(new Aarstall(1990), Month.APRIL));

        assertAnnotasjon(annoter(periode), Month.class).isEqualTo(of(Month.APRIL));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med samme årstall som årsperioda den er tilknytta.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedAarstallFraTilkoblaAarsperiode() {
        final Underlagsperiode periode = new UnderlagsperiodeBuilder()
                .fraOgMed(dato("1990.01.01"))
                .tilOgMed(dato("1990.12.31")).bygg();
        periode.kobleTil(new Aar(new Aarstall(1990)));

        assertAnnotasjon(annoter(periode), Aarstall.class).isEqualTo(of(new Aarstall(1990)));
    }

    private Underlagsperiode annoter(final Underlagsperiode periode) {
        annotator.annoter(underlag, periode);
        return periode;
    }

    private <T> AbstractObjectAssert<?, Optional<T>> assertAnnotasjon(final Underlagsperiode periode, final Class<T> type) {
        return assertThat(periode.valgfriAnnotasjonFor(type)).as(type.getSimpleName() + "-annotasjon for periode " + periode);
    }

    private Underlagsperiode eiAnnotertPeriodeUtanKoblingar() {
        return annoter(eiPeriode().bygg());
    }

    private Stillingsendring eiStillingsendring() {
        return new Stillingsendring()
                .stillingsprosent(new Stillingsprosent(new Prosent("100%")))
                .registreringsdato(dato("2099.01.01"));
    }

    private static UnderlagsperiodeBuilder eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("1990.01.01"))
                .tilOgMed(dato("1990.12.31"));
    }
}