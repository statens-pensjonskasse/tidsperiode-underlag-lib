package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertFraOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring.stillingsendring;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.PeriodiserStillingshistorikk}.
 *
 * @author Tarjei Skorgenes
 */
public class PeriodiserStillingshistorikkTest {
    @Test
    public void skalDannePerioderMellomKvarUnikeAksjonsdato() {
        final List<StillingsforholdPeriode> perioder = periodiser(
                stillingsendring().aksjonsdato(dato("2001.01.01")),
                stillingsendring().aksjonsdato(dato("2001.03.07")).aksjonskode("012"),
                stillingsendring().aksjonsdato(dato("2001.03.07")).aksjonskode("021"),
                stillingsendring().aksjonsdato(dato("2011.12.11"))
        );

        assertFraOgMed(perioder.get(0)).isEqualTo(dato("2001.01.01"));
        assertTilOgMed(perioder.get(0)).isEqualTo(of(dato("2001.03.06")));

        assertFraOgMed(perioder.get(1)).isEqualTo(dato("2001.03.07"));
        assertTilOgMed(perioder.get(1)).isEqualTo(of(dato("2011.12.10")));

        assertFraOgMed(perioder.get(2)).isEqualTo(dato("2011.12.11"));
        assertTilOgMed(perioder.get(2)).isEqualTo(empty());
    }

    @Test
    public void skalBehandleKronologiskFoersteEndringSomStartMeldingSelvOmEndringIkkeTilhoererEnStartmelding() {
        final List<StillingsforholdPeriode> perioder = periodiser(
                stillingsendring().aksjonsdato(dato("2011.12.11")).aksjonskode("011"),
                stillingsendring().aksjonsdato(dato("2001.03.07")).aksjonskode("021"),
                stillingsendring().aksjonsdato(dato("2001.01.01")).aksjonskode("031")
        );

        assertFraOgMed(perioder.get(0)).isEqualTo(dato("2001.01.01"));
        assertFraOgMed(perioder.get(1)).isEqualTo(dato("2001.03.07"));
        assertFraOgMed(perioder.get(2)).isEqualTo(dato("2011.12.11"));
        assertTilOgMed(perioder.get(2)).isEqualTo(empty());
    }

    @Test
    public void skalSpesialhandtereSistePeriodesTilOgMedDatoVissDetErRegistrertSluttmeldingSammeDag() {
        List<StillingsforholdPeriode> perioder = periodiser(
                stillingsendring().aksjonsdato(dato("2012.06.30")).aksjonskode("031")
        );
        assertTilOgMed(perioder.get(0)).isEqualTo(of(dato("2012.06.30")));
    }

    @Test
    public void skalGenerereLoependeSistePeriodesVissDetIkkeErRegistrertSluttmeldingPaSisteAksjonsdato() {
        List<StillingsforholdPeriode> perioder = periodiser(
                stillingsendring().aksjonsdato(dato("2012.06.30")).aksjonskode("021")
        );
        assertTilOgMed(perioder.get(0)).isEqualTo(empty());
    }

    /**
     * Verifiserer at periodiseringa ikkje tar hensyn til aksjonskodene når den periodiserer alle andre perioder enn
     * den siste (der den skal spesialhandtere 031 på siste aksjonsdato).
     * <p>
     * Dette betyr at den ikkje tar hensyn til 031 i tidligare perioder, eller 011, 012, 023, 028, 029, alle desse blir behandla som 021 og første
     * aksjonsdato vi har ei endring på, blir tolka som startdato for første periode.
     */
    @Test
    public void skalHandtereAlleAndreAksjonskoderSomEndring() {
        asList("011", "012", "013", "021", "023", "028", "029", "031")
                .forEach(aksjonskode -> {
                    List<StillingsforholdPeriode> perioder = periodiser(
                            stillingsendring().aksjonsdato(dato("2000.01.01")).aksjonskode(aksjonskode),
                            stillingsendring().aksjonsdato(dato("2001.01.01")).aksjonskode("021")
                    );
                    assertFraOgMed(perioder.get(0)).isEqualTo(dato("2000.01.01"));
                    assertTilOgMed(perioder.get(0)).isEqualTo(of(dato("2000.12.31")));
                    assertFraOgMed(perioder.get(1)).isEqualTo(dato("2001.01.01"));
                    assertTilOgMed(perioder.get(1)).isEqualTo(empty());
                });
    }

    /**
     * Verifiserer at kvar periode blir kobla opp mot historikkendringene som skjer på periodens fra og med- eller
     * til og med-dato.
     */
    @Test
    public void skalKobleOppPerioderTilEndringerSomDeOverlapper() {
        List<Stillingsendring> endringer = asList(
                stillingsendring().aksjonsdato(dato("2005.08.15")),
                stillingsendring().aksjonsdato(dato("2006.01.01")),
                stillingsendring().aksjonsdato(dato("2007.08.01")),
                stillingsendring().aksjonsdato(dato("2010.01.01")),
                stillingsendring().aksjonsdato(dato("2012.06.30")).aksjonskode("031")
        );

        List<StillingsforholdPeriode> perioder = periodiser(endringer);
        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(0).endringer()).hasSize(1).contains(endringer.get(0));
        assertThat(perioder.get(1).endringer()).hasSize(1).contains(endringer.get(1));
        assertThat(perioder.get(2).endringer()).hasSize(1).contains(endringer.get(2));
        assertThat(perioder.get(3).endringer()).hasSize(2).contains(endringer.get(3), endringer.get(4));
    }

    private List<StillingsforholdPeriode> periodiser(final Stillingsendring... c) {
        return periodiser(asList(c));
    }

    private List<StillingsforholdPeriode> periodiser(final Iterable<Stillingsendring> endringer) {
        return new PeriodiserStillingshistorikk()
                .addEndring(endringer)
                .periodiser().get();
    }
}
