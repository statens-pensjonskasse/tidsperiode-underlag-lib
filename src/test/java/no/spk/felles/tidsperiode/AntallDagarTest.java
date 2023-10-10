package no.spk.felles.tidsperiode;

import static no.spk.felles.tidsperiode.AntallDagar.antallDagar;
import static no.spk.felles.tidsperiode.AntallDagar.antallDagarMellom;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class AntallDagarTest {
    /**
     * Vi skal aldri kunne ha tidsperioder som er kortare enn 1 dag, verifiser at vi feilar viss det blir forsøkt
     * brukt som verdi for antall dagar.
     */
    @Test
    void skalIkkjeTillateAntallDagarLik0() {
        assertThatCode(
                () -> new AntallDagar(0)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("antall dagar kan ikkje vere kortare enn 1 dag")
                .hasMessageContaining("0")
        ;
    }

    /**
     * Vi skal aldri kunne ha tidsperioder med negativ lengde, verifiser at vi feilar viss ein negativ verdi blir forsøkt
     * brukt som verdi for antall dagar.
     */
    @Test
    void skalIkkjeTillateAntallDagarMindreEnn0() {
        assertThatCode(
                () -> new AntallDagar(-1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("antall dagar kan ikkje vere kortare enn 1 dag")
                .hasMessageContaining("-1")
        ;
    }

    @Test
    void skalVereLikSammeInstans() {
        // NB: Ikkje inline den her, det stride mot intensjonen i testen
        final AntallDagar me = new AntallDagar(1);
        assertThat(me).isEqualTo(me);
    }

    @Test
    void skalVereLikAnnanInstansMedSammeVerdi() {
        // NB: Ikkje trekk ut verdien til ein variabel her, det stride mot intensjonen i testen
        assertThat(new AntallDagar(2)).isEqualTo(new AntallDagar(2));
    }

    @Test
    void skalVereUlikNull() {
        assertThat(new AntallDagar(3)).isNotEqualTo(null);
    }

    @Test
    void skalVereUlikAnnanType() {
        assertThat(new AntallDagar(4)).isNotEqualTo(new Object());
    }

    @Test
    void skalVereUlikSammeTypeMedAnnanVerdi() {
        assertThat(new AntallDagar(365)).isNotEqualTo(new AntallDagar(366));
    }

    /**
     * Verifiserer første del av kontrakta for {@link Object#hashCode()}, at to instansar som er like
     * i henhold til {@link Object#equals(Object)} returnerer samme hashcode.
     */
    @Test
    void skalHaSammeHashCodeForForskjelligeInstansarMedSammeVerdi() {
        // NB: Ikkje trekk ut verdien til ein variabel her, det stride mot intensjonen i testen
        assertThat(new AntallDagar(123).hashCode()).isEqualTo(new AntallDagar(123).hashCode());
    }

    /**
     * Verifiserer andre del av kontrakta for {@link Object#hashCode()}, at samme instans
     * returnerer samme hashcode viss ein ber om den fleire gangar frå samme instans
     * innanfor samme køyring av JVMen.
     */
    @Test
    void skalReturnereSammeHashCodeForKvartKall() {
        // NB: Ikkje inline den her, det stride mot intensjonen i testen
        final AntallDagar antall = new AntallDagar(123);
        assertThat(antall.hashCode()).isEqualTo(antall.hashCode());
    }

    /**
     * Antall dagar kan ikkje beregnast viss ein ikkje veit kva som er startdatoen for perioda ein skal telle dagar i.
     */
    @Test
    void skalIkkjeStoetteFraOgMedDatoLikNull() {
        assertThatCode(
                () -> antallDagarMellom(null, dato("2005.01.01"))
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("frå og med-dato må vere ulik null")
        ;
    }

    /**
     * Antall dagar skal kun kunne beregnast for lukka tidsperioder, løpande tidsperioder skal ikkje vere støtta.
     */
    @Test
    void skalIkkjeStoetteTilOgMedDatoLikNull() {
        assertThatCode(
                () -> antallDagarMellom(dato("2005.01.01"), null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("til og med-dato må vere ulik null")
                .hasMessageContaining("løpande perioder er ikkje støtta")
        ;
    }

    /**
     * Antall dagar mellom to like datoar skal vere lik 1 ettersom {@link Tidsperiode#tilOgMed()} er inkludert
     * i tidsperioder ein skal måle antall dagar i.
     */
    @Test
    void skalBeregneLengdeLik1DagForPerioderMedLikFraOgMedOgTilOgMedDato() {
        assertThat(antallDagarMellom(dato("2003.01.01"), dato("2003.01.01"))).isEqualTo(antallDagar(1));
    }

    @Test
    void skalFeileVissFraOgMedDatoErStoerreEnnTilOgMedDato() {
        assertThatCode(
                () -> antallDagarMellom(dato("2000.09.10"), dato("2000.09.09"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fra og med-dato kan ikkje vere etter til og med-dato")
                .hasMessageContaining("2000-09-10 er etter 2000-09-09")
        ;
    }
}