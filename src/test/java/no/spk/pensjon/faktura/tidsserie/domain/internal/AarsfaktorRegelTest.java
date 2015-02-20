package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.data.Offset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Enheitstestar for beregningsregelen for årsfaktor.
 */
public class AarsfaktorRegelTest {
    private static final Offset<Double> PRESISJON = offset(0.00001);

    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalBeregneFullAarsfaktorVissPeriodaDekkerHeileAaret() {
        assertAarsfaktor(
                periode("2005.01.01", "2005.12.31")
                .med(new Aarstall(2005))
        ).isEqualTo(1.0d, PRESISJON);
    }

    @Test
    public void skalBeregneAarsfaktorVissPeriodaEr1DagLang() {
        assertAarsfaktor(
                periode("2005.01.01", "2005.01.01")
                .med(new Aarstall(2005))
        ).isEqualTo(1.0d / 365d, offset(0.00001));
    }

    @Test
    public void skalFeileVedForsoekPaaBeregningAvAarsfaktorForPerioderLengerEnnEitAar() {
        e.expectMessage("årsfaktor");
        e.expectMessage("kan kun beregnast for perioder på 1 år eller kortare");
        e.expectMessage("perioda var " + (365 * 3) + " dagar lang");

        beregn(periode("2001.01.01", "2003.12.31").med(new Aarstall(2001)));
    }

    @Test
    public void skalFeileDersomPeriodaIkkjeHarEinBeregningsRegelForAntallDager() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        beregn(enperiode().uten(AntallDagarRegel.class));
    }

    @Test
    public void skalFeileDersomPeriodaIkkjeHarEinBeregningsRegelForAarslengde() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        beregn(enperiode().uten(AarsLengdeRegel.class));
    }

    /**
     * Tiltenkt brukt for testar som ikkje er avhengig eller interessert i sjølve tidsintervallet underlagsperioda
     * strekker seg over.
     *
     * @return ei ny underlagsperiodebuilder med fra og med- og til og med-dato + alle nødvendige reglar satt
     */
    private static UnderlagsperiodeBuilder enperiode() {
        return periode("2000.01.01", "2000.01.12");
    }

    private static UnderlagsperiodeBuilder periode(final String fra, final String til) {
        return Support.periode(fra, til)
                .med(new AarsfaktorRegel())
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel());
    }

    private static AbstractDoubleAssert<?> assertAarsfaktor(UnderlagsperiodeBuilder periode) {
        return assertThat(beregn(periode).verdi());
    }

    private static Aarsfaktor beregn(UnderlagsperiodeBuilder periode) {
        return new AarsfaktorRegel().beregn(periode.bygg());
    }
}
