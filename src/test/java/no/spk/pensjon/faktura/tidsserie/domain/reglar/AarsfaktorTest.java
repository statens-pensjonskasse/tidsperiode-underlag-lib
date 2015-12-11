package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsfaktorTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeKunneVereStoerreEnn1() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere mindre enn eller lik 1, men var 2");
        new Aarsfaktor(2d);
    }

    @Test
    public void skalIkkjeKunneVereLik0() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere større enn 0, men var 0");
        new Aarsfaktor(0d);
    }

    @Test
    public void skalIkkjeKunneVereNegativ() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("årsfaktor må vere større enn 0, men var -2");
        new Aarsfaktor(-2d);
    }

    @Test
    public void skalGangeBeloepMedAarsfaktor() {
        final Kroner aarsloenn = new Kroner(365_000);
        assertThat(new Aarsfaktor(1d / 365d).multiply(aarsloenn)).isEqualTo(new Kroner(1_000));
        assertThat(new Aarsfaktor(365d / 365d).multiply(aarsloenn)).isEqualTo(aarsloenn);
    }

    @Test
    public void skalMultiplisereMedBeloepMenAldriReturnereBeloepStoerreEnnInputBeloepet() {
        final Kroner aarsloenn = new Kroner(366_000);
        for (int dagar = 1; dagar <= 366; dagar++) {
            final Kroner resultat = new Aarsfaktor(1d / dagar).multiply(aarsloenn);
            assertThat(resultat.compareTo(aarsloenn))
                    .as("årslønn avkorta til " + dagar + " dagar kan ikkje overstige den totale årslønna for 366 dagar")
                    .isLessThanOrEqualTo(0);
        }
    }
}