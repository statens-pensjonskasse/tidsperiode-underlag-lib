package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;

/**
 * Enheitstestar av {@link TidsserieUnderlagFacade}.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieUnderlagFacadeTest {
    private final TidsserieUnderlagFacade fasade = new TidsserieUnderlagFacade();

    /**
     * Verifiserer at vi ikkje annoterer nokon underlagsperioder som siste periode dersom stillingsforholdet
     * ikkje har noko sluttmelding.
     */
    @Test
    public void skalIkkjeAnnotereSistePeriodeDersomStillingsforholdErAktivt() {
        final LocalDate aksjonsdato = dato("2012.06.30");
        final StillingsforholdPeriode stilling = new StillingsforholdPeriode(dato("2005.08.15"), of(aksjonsdato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .stillingsprosent(fulltid())
                                .aksjonsdato(aksjonsdato)
                                .aksjonskode("021")
                );
        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2012.01.01"))
                        .tilOgMed(aksjonsdato)
                        .medKobling(
                                stilling
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), SistePeriode.class).isEqualTo(of(SistePeriode.INSTANCE));
    }

    /**
     * Verifiserer at det kun er siste underlagsperiode som blir annotert med {@link SistePeriode} sjølv om
     * det er to eller fleire underlagsperioder som overlappar stillingsforholdperioda som inneheld sluttmeldinga.
     */
    @Test
    public void skalKunAnnotereSisteUnderlagsperiodeSjoelvOmStillingsperiodeMedSluttmeldingOverlapparFleireUnderlagsperioder() {
        final LocalDate sluttDato = dato("2012.06.30");
        final UnderlagsperiodeBuilder builder = eiTomPeriode()
                .medKobling(
                        new StillingsforholdPeriode(dato("2005.08.15"), of(sluttDato))
                                .leggTilOverlappendeStillingsendringer(
                                        new Stillingsendring()
                                                .stillingsprosent(fulltid())
                                                .aksjonsdato(sluttDato)
                                                .aksjonskode("031")
                                )
                );
        final Underlag underlag = annoterAllePerioder(
                builder.kopi()
                        .fraOgMed(dato("2012.01.01"))
                        .tilOgMed(dato("2012.04.30")),
                builder.kopi()
                        .fraOgMed(dato("2012.05.01"))
                        .tilOgMed(dato("2012.05.31")),
                builder.kopi()
                        .fraOgMed(dato("2012.06.01"))
                        .tilOgMed(sluttDato)
        );
        assertAnnotasjon(underlag.toList().get(0), SistePeriode.class).isEqualTo(empty());
        assertAnnotasjon(underlag.toList().get(1), SistePeriode.class).isEqualTo(empty());
        assertAnnotasjon(underlag.toList().get(2), SistePeriode.class).isEqualTo(of(SistePeriode.INSTANCE));
    }


    /**
     * Verifiserer at siste underlagsperiode i stillingsforholdunderlaget, blir annotert med
     * {@link SistePeriode} dersom den overlappande stillingsforholdperioda inneheld ei sluttmelding.
     * <p>
     * Ein underliggande antagelse her er at siste underlagsperiode løper fram til og med den overlappande stillingsforholdperiodas
     * til og med-dato.
     */
    @Test
    public void skalAnnotereSisteUnderlagsperiodeMedSistePeriodeVissOverlappandeStillingsforholdsperiodeInneheldEiSluttmelding() {
        final LocalDate sluttDato = dato("2012.06.30");
        final StillingsforholdPeriode stilling = new StillingsforholdPeriode(dato("2005.08.15"), of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .stillingsprosent(fulltid())
                                .aksjonsdato(sluttDato)
                                .aksjonskode("031")
                );
        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2012.01.01"))
                        .tilOgMed(sluttDato)
                        .medKobling(
                                stilling
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), SistePeriode.class).isEqualTo(of(SistePeriode.INSTANCE));
    }

    private Underlag annoterAllePerioder(final UnderlagsperiodeBuilder... perioder) {
        return fasade.annoter(
                new Underlag(
                        Stream
                                .of(perioder)
                                .map(UnderlagsperiodeBuilder::bygg)
                )
        );
    }

    private static UnderlagsperiodeBuilder eiTomPeriode() {
        return new UnderlagsperiodeBuilder();
    }
}