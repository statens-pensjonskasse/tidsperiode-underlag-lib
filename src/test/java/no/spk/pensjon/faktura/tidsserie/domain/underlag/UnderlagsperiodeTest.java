package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedFraOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("fra og med-dato er påkrevd");
        e.expectMessage("var null");
        create(null, "2007.12.31");
    }

    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedTilOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato er påkrevd");
        e.expectMessage("var null");
        create("2007.12.31", null);
    }

    @Test
    public void skalIkkjeKunneOpprettUnderlagsperiodeMedFraOgMedDatoEtterTilOgMedDato() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("fra og med-dato kan ikkje vere etter til og med-dato");
        e.expectMessage("2005-12-30 er etter 2005-01-01");

        create("2005.12.30", "2005.01.01");
    }

    private Underlagsperiode create(final String fra, final String til) {
        return new Underlagsperiode(dato(fra), dato(til));
    }

}