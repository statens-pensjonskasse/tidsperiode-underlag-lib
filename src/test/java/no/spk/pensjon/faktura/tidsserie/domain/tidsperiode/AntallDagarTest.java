package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagar;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagarMellom;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AntallDagarTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Vi skal aldri kunne ha tidsperioder som er kortare enn 1 dag, verifiser at vi feilar viss det blir forsøkt
     * brukt som verdi for antall dagar.
     */
    @Test
    public void skalIkkjeTillateAntallDagarLik0() {
        expectIllegalArgumentException();
        e.expectMessage("0");
        new AntallDagar(0);
    }

    /**
     * Vi skal aldri kunne ha tidsperioder med negativ lengde, verifiser at vi feilar viss ein negativ verdi blir forsøkt
     * brukt som verdi for antall dagar.
     */
    @Test
    public void skalIkkjeTillateAntallDagarMindreEnn0() {
        expectIllegalArgumentException();
        e.expectMessage("-1");
        new AntallDagar(-1);
    }

    @Test
    public void skalVereLikSammeInstans() {
        // NB: Ikkje inline den her, det stride mot intensjonen i testen
        final AntallDagar me = new AntallDagar(1);
        assertThat(me).isEqualTo(me);
    }

    @Test
    public void skalVereLikAnnanInstansMedSammeVerdi() {
        // NB: Ikkje trekk ut verdien til ein variabel her, det stride mot intensjonen i testen
        assertThat(new AntallDagar(2)).isEqualTo(new AntallDagar(2));
    }

    @Test
    public void skalVereUlikNull() {
        assertThat(new AntallDagar(3)).isNotEqualTo(null);
    }

    @Test
    public void skalVereUlikAnnanType() {
        assertThat(new AntallDagar(4)).isNotEqualTo(new Object());
    }

    @Test
    public void skalVereUlikSammeTypeMedAnnanVerdi() {
        assertThat(new AntallDagar(365)).isNotEqualTo(new AntallDagar(366));
    }

    /**
     * Verifiserer første del av kontrakta for {@link Object#hashCode()}, at to instansar som er like
     * i henhold til {@link Object#equals(Object)} returnerer samme hashcode.
     */
    @Test
    public void skalHaSammeHashCodeForForskjelligeInstansarMedSammeVerdi() {
        // NB: Ikkje trekk ut verdien til ein variabel her, det stride mot intensjonen i testen
        assertThat(new AntallDagar(123).hashCode()).isEqualTo(new AntallDagar(123).hashCode());
    }

    /**
     * Verifiserer andre del av kontrakta for {@link Object#hashCode()}, at samme instans
     * returnerer samme hashcode viss ein ber om den fleire gangar frå samme instans
     * innanfor samme køyring av JVMen.
     */
    @Test
    public void skalReturnereSammeHashCodeForKvartKall() {
        // NB: Ikkje inline den her, det stride mot intensjonen i testen
        final AntallDagar antall = new AntallDagar(123);
        assertThat(antall.hashCode()).isEqualTo(antall.hashCode());
    }

    /**
     * Antall dagar kan ikkje beregnast viss ein ikkje veit kva som er startdatoen for perioda ein skal telle dagar i.
     */
    @Test
    public void skalIkkjeStoetteFraOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("frå og med-dato må vere ulik null");
        antallDagarMellom(null, dato("2005.01.01"));
    }

    /**
     * Antall dagar skal kun kunne beregnast for lukka tidsperioder, løpande tidsperioder skal ikkje vere støtta.
     */
    @Test
    public void skalIkkjeStoetteTilOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato må vere ulik null");
        e.expectMessage("løpande perioder er ikkje støtta");
        antallDagarMellom(dato("2005.01.01"), null);
    }

    /**
     * Antall dagar mellom to like datoar skal vere lik 1 ettersom {@link Tidsperiode#tilOgMed()} er inkludert
     * i tidsperioder ein skal måle antall dagar i.
     */
    @Test
    public void skalBeregneLengdeLik1DagForPerioderMedLikFraOgMedOgTilOgMedDato() {
        assertThat(antallDagarMellom(dato("2003.01.01"), dato("2003.01.01"))).isEqualTo(antallDagar(1));
    }

    @Test
    public void skalFeileVissFraOgMedDatoErStoerreEnnTilOgMedDato() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("fra og med-dato kan ikkje vere etter til og med-dato");
        e.expectMessage("2000-09-10 er etter 2000-09-09");

        antallDagarMellom(dato("2000.09.10"), dato("2000.09.09"));
    }

    private void expectIllegalArgumentException() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("antall dagar kan ikkje vere kortare enn 1 dag");
    }
}