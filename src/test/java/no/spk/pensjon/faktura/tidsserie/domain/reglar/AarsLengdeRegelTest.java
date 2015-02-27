package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagar;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.Support.periode;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsLengdeRegelTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at regelen beregnar antall dagar basert på årstallet periode er annotert med, ikkje lengda på
     * underlagsperioda.
     */
    @Test
    public void skalBeregneLengdeUtFraaAarstalletUnderlagsperiodaErAnnotertMed() {
        assertAntallDagar(
                periode("2005.01.01", "2005.03.31")
                        .med(new Aarstall(2005))
        ).isEqualTo(antallDagar(365));
    }

    /**
     * Verifiserer at periode må vere annotert med årstall for at regelen skal kunne beregne eit resultat.
     */
    @Test
    public void skalFeileDersomUnderlagsperiodaIkkjeErAnnotertMedAarstall() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        beregn(
                periode("2005.01.01", "2005.02.01")
                        .uten(Aarstall.class)
        );
    }

    private static AbstractObjectAssert<?, AntallDagar> assertAntallDagar(final UnderlagsperiodeBuilder builder) {
        return assertThat(beregn(builder));
    }

    private static AntallDagar beregn(final UnderlagsperiodeBuilder builder) {
        return new AarsLengdeRegel().beregn(builder.bygg());
    }
}