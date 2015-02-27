package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractDoubleAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsverkRegelTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Perioder utan stillingsprosent skal ikkje føre til at beregningane feilar, dei skal
     * då generere 0 årsverk som svar.
     * <p>
     * Strategien er valgt for at vi skal kunne handtere/ignorere underlagsperioder tilknytta medregning. Medregningane
     * har ikkje nokon stillingsprosent så vi er dermed ikkje i stand til å beregne årsverk for dei.
     */
    @Test
    public void skalBeregneIngenAarsverkVedManglandeStillingsprosent() {
        assertAarsverk(periode("2000.01.01", "2000.01.31").uten(Stillingsprosent.class)).isEqualTo(0d);
    }

    @Test
    public void skalBeregneEitHalvttAarsverkVed50ProsentStillingHeileAaret() {
        assertAarsverk(
                periode("2000.01.01", "2000.12.31")
                        .med(new Stillingsprosent(prosent("50%")))
        ).isEqualTo(prosent("50%").toDouble(), offset(0.0001));
    }

    @Test
    public void skalBeregneEitHeiltAarsverkVedFullStillingHeileAaret() {
        final UnderlagsperiodeBuilder p = periode("2001.01.01", "2001.12.31");
        assertAarsverk(p)
                .isEqualTo(prosent("100%").toDouble(), offset(0.0001));
    }

    @Test
    public void skalBeregneForskjelligVerdiForFebruarISkuddAarVersusVanligeAar() {
        assertAarsverk(periode("2000.02.01", "2000.02.28"))
                .isNotEqualTo(
                        beregn(periode("2001.02.01", "2001.02.28")).tilProsent().toDouble()
                );
    }

    @Test
    public void skalFeileDersomFraOgMedOgTilOgMedTilhoererForskjelligeAarstall() {
        e.expect(IllegalStateException.class);
        e.expectMessage("årsverk kan kun beregnast for underlagsperioder som startar og sluttar innanfor samme årstall");
        beregn(periode("2000.12.31", "2001.01.01"));
    }

    private UnderlagsperiodeBuilder periode(final String fraOgMed, final String tilOgMed) {
        final LocalDate startDato = dato(fraOgMed);
        return new UnderlagsperiodeBuilder()
                .fraOgMed(startDato)
                .tilOgMed(dato(tilOgMed))

                .med(new Stillingsprosent(prosent("100%")))

                .med(new Aarstall(startDato.getYear()))
                .med(new AarsfaktorRegel())
                .med(new AarsLengdeRegel())
                .med(new AntallDagarRegel())
                .med(new AarsverkRegel())
                ;
    }

    private static AbstractDoubleAssert<?> assertAarsverk(UnderlagsperiodeBuilder p) {
        return assertThat(beregn(p).tilProsent().toDouble());
    }

    private static Aarsverk beregn(final UnderlagsperiodeBuilder p) {
        return p.bygg().beregn(AarsverkRegel.class);
    }
}