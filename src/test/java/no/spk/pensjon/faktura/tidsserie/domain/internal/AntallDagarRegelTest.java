package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagar;
import static no.spk.pensjon.faktura.tidsserie.domain.internal.Support.periode;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class AntallDagarRegelTest {
    /**
     * Verifiserer at perioder som strekker seg over ein skuddårsdag får korrekt beregna lengde.
     */
    @Test
    public void skalBeregneAntallDagarKorrektVissPeriodaOverlapparFebruarIEitSkuddaar() {
        assertAntallDagar(
                periode("2012.02.28", "2012.03.01")
        ).isEqualTo(antallDagar(3));
    }

    /**
     * Verifiserer at perioder med start og slutt samme dag blir beregna til antall dager lik 1.
     * <br>
     * Denne testen verifiserer dermed også implisitt at både frå og med- og til og med-dato
     * blir inkludert i tellinga av antall dagar.
     */
    @Test
    public void skalBeregneAntallDagarLik1ForPeriodeSomStartarOgSluttarSammeDag() {
        assertAntallDagar(
                periode("2001.01.01", "2001.01.01")
        ).isEqualTo(antallDagar(1));
    }

    /**
     * Verifiserer at regelen etller antall dagar i ei underlagsperiode sjølv om underlagsperioda strekker seg over
     * meir enn eit år.
     */
    @Test
    public void skalBeregneAntallDagarUtanÅFeileVissPeriodaStrekkerSegOverFleireAar() {
        assertAntallDagar(
                periode("2001.01.01", "2003.12.31")
        ).isEqualTo(antallDagar(365 * 3));
    }

    private static AbstractObjectAssert<?, AntallDagar> assertAntallDagar(final UnderlagsperiodeBuilder builder) {
        return assertThat(new AntallDagarRegel().beregn(builder.bygg()));
    }
}