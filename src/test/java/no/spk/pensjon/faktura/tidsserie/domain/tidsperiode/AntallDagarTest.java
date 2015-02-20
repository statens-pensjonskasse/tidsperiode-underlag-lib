package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagarMellom;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AntallDagarTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Vi skal aldri kunne ha tidsperioder som er kortare enn 1 dag, verifiser at vi feilar viss det blir fors�kt
     * brukt som verdi for antall dagar.
     */
    @Test
    public void skalIkkjeTillateAntallDagarLik0() {
        expectIllegalArgumentException();
        e.expectMessage("0");
        new AntallDagar(0);
    }

    /**
     * Vi skal aldri kunne ha tidsperioder med negativ lengde, verifiser at vi feilar viss ein negativ verdi blir fors�kt
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
     * Verifiserer f�rste del av kontrakta for {@link Object#hashCode()}, at to instansar som er like
     * i henhold til {@link Object#equals(Object)} returnerer samme hashcode.
     */
    @Test
    public void skalHaSammeHashCodeForForskjelligeInstansarMedSammeVerdi() {
        // NB: Ikkje trekk ut verdien til ein variabel her, det stride mot intensjonen i testen
        assertThat(new AntallDagar(123).hashCode()).isEqualTo(new AntallDagar(123).hashCode());
    }

    /**
     * Verifiserer andre del av kontrakta for {@link Object#hashCode()}, at samme instans
     * returnerer samme hashcode viss ein ber om den fleire gangar fr� samme instans
     * innanfor samme k�yring av JVMen.
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
        e.expectMessage("fr� og med-dato m� vere ulik null");
        antallDagarMellom(null, dato("2005.01.01"));
    }

    /**
     * Antall dagar skal kun kunne beregnast for lukka tidsperioder, l�pande tidsperioder skal ikkje vere st�tta.
     */
    @Test
    public void skalIkkjeStoetteTilOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato m� vere ulik null");
        e.expectMessage("l�pande perioder er ikkje st�tta");
        antallDagarMellom(dato("2005.01.01"), null);
    }

    private void expectIllegalArgumentException() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("antall dagar kan ikkje vere kortare enn 1 dag");
    }
}