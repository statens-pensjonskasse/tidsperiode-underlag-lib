package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.assertj.core.api.AbstractIntegerAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner}
 *
 * @author Tarjei Skorgenes
 */
public class KronerTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalAvrundeVerdiTilNaermasterHeileKrone() {
        assertThat(new Kroner(1.5d).verdi()).isEqualTo(2L);
        assertThat(new Kroner(1.49d).verdi()).isEqualTo(1L);
        assertThat(new Kroner(0.5d).verdi()).isEqualTo(1L);
        assertThat(new Kroner(0.49d).verdi()).isEqualTo(0L);
        assertThat(new Kroner(-0.5d).verdi()).isEqualTo(0L);
        assertThat(new Kroner(-0.51d).verdi()).isEqualTo(-1L);
        assertThat(new Kroner(-1.5d).verdi()).isEqualTo(-1L);
        assertThat(new Kroner(-1.51d).verdi()).isEqualTo(-2L);
    }

    @Test
    public void skalLeggeSamanKronebeloepaOgGenerereEitNyttKronebeloep() {
        assertThat(new Kroner(10).plus(new Kroner(-2))).isEqualTo(new Kroner(8));
        assertThat(new Kroner(0).plus(new Kroner(1))).isEqualTo(new Kroner(1));
        assertThat(new Kroner(1).plus(new Kroner(0))).isEqualTo(new Kroner(1));
    }

    @Test
    public void skalBenytteAvrundaKroneBeloepVedSamanlikning() {
        assertThat(new Kroner(1.5d)).isEqualTo(new Kroner(2d));
        assertThat(new Kroner(1.49d)).isEqualTo(new Kroner(1d));
        assertThat(new Kroner(0.5d)).isEqualTo(new Kroner(1d));
        assertThat(new Kroner(0.49d)).isEqualTo(new Kroner(0d));
        assertThat(new Kroner(-0.5d)).isEqualTo(new Kroner(0d));
        assertThat(new Kroner(-0.51d)).isEqualTo(new Kroner(-1d));
        assertThat(new Kroner(-1.5d)).isEqualTo(new Kroner(-1d));
        assertThat(new Kroner(-1.51d)).isEqualTo(new Kroner(-2d));
    }

    @Test
    public void skalVereLikSammeInstans() {
        // NB: Ikkje inline veriablen, det stride mot intensjonen til testen
        final Kroner self = new Kroner(0);
        assertThat(self).isEqualTo(self);
    }

    @Test
    public void skalVereLikAnnanKroneInstansMedSammeVerdi() {
        // NB: Ikkje trekk ut uttrykket til ein egen variabel, det stride mot intensjonen til testen
        assertThat(new Kroner(0)).isEqualTo(new Kroner(0));
    }

    @Test
    public void skalVereUlikAnnanKroneInstansMedUlikVerdi() {
        assertThat(new Kroner(10)).isNotEqualTo(new Kroner(20));
    }

    @Test
    public void skalVereUlikNull() {
        assertThat(new Kroner(123)).isNotEqualTo(null);
    }

    @Test
    public void skalVereUlikInstansAvAnnanType() {
        assertThat(new Kroner(123)).isNotEqualTo(new Double(123));
    }

    @Test
    public void skalHaSammeHashCodeVedLikVerdi() {
        assertThat(new Kroner(123).hashCode()).isEqualTo(new Kroner(123).hashCode());
    }

    @Test
    public void skalKreveTallUlikNullVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("beløp er påkrevd, men var null");
        new Kroner(null);
    }

    @Test
    public void skalVereLikeVissCompareToGir0() {
        assertCompareTo(2897123d, 2897123d).isEqualTo(0);
    }

    @Test
    public void skalVereMindreEnnVissCompareToReturnererMindreEnn0() {
        assertCompareTo(1234, 4321).isLessThan(0);
    }

    @Test
    public void skalVereStoerreEnnVissCompareToReturnererStoerreEnn0() {
        assertCompareTo(123789123, 12123).isGreaterThan(0);
    }

    @Test
    public void skalVereLikeSjoelvOmDesimalarErUlikeSaaLengeAvrundingGirLikVerdi() {
        assertCompareTo(1.5d, 2.49d).isEqualTo(0);
    }

    @Test
    public void skalVereUlikeVissHeiltallErLiktMenAvrundingGirUlikVerdiGrunnaDesimalar() {
        assertCompareTo(1.5d, 1.49d).isGreaterThan(0);
        assertCompareTo(0.5d, 0.49d).isGreaterThan(0);
        assertCompareTo(-0.5d, -0.51d).isGreaterThan(0);
        assertCompareTo(-1.5d, -1.51d).isGreaterThan(0);
        assertCompareTo(1.49d, 1.5d).isLessThan(0);
        assertCompareTo(0.49d, 0.5d).isLessThan(0);
        assertCompareTo(-0.51d, -0.5d).isLessThan(0);
        assertCompareTo(-1.51d, -1.5d).isLessThan(0);;
    }

    @Test
    public void skalViseProsentsatsFormatertViaToStringForEnklareLoggingOgDebugging() {
        assertThat(new Kroner(100d).toString()).isEqualTo("kr 100");
        assertThat(new Kroner(1_000_000d).toString()).isEqualTo("kr 1000000");
        assertThat(new Kroner(-200.49d).toString()).isEqualTo("kr -200");
    }

    private AbstractIntegerAssert<?> assertCompareTo(double a, double b) {
        final Kroner x = new Kroner(a);
        final Kroner y = new Kroner(b);
        return assertThat(x.compareTo(y)).as(x + " compareTo " + y);
    }
}