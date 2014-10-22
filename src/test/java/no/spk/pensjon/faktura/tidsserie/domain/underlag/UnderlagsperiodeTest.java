package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar ikkje feilar dersom perioda ikkje har ein verdi for
     * annotasjonen.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonSomIkkjeEksistererPaaPerioda() {
        assertThat(
                eiPeriode().valgfriAnnotasjonFor(Object.class)
        ).isEqualTo(empty());
    }

    /**
     * Verifiserer at oppslag av valgfri annotasjon fungerer når perioda har ein verdi for annotasjonen.
     */
    @Test
    public void skalKunneHenteUtVerdiarForValgfrieAnnotasjonar() {
        final Object verdi = new String("valgfrie annotasjonar fungerer fint");

        final Underlagsperiode periode = eiPeriode();
        periode.annoter(Object.class, verdi);

        assertThat(periode.valgfriAnnotasjonFor(Object.class)).isEqualTo(of(verdi));
    }

    /**
     * Verifiserer at oppslag av påkrevde annotasjonar via
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#annotasjonFor(Class)} feilar med
     * ein exception dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    public void skalFeileVedOppslagAvPaakrevdAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage("Underlagsperioda frå 2005-01-01 til 2005-12-31 manglar påkrevd annotasjon av type");
        e.expectMessage(Integer.class.getSimpleName());

        create("2005.01.01", "2005.12.31").annotasjonFor(Integer.class);
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar via
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#valgfriAnnotasjonFor(Class)} ikkje
     * feilar og returnerer ein tom verdi dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        assertThat(create("2005.02.02", "2005.03.03").valgfriAnnotasjonFor(Long.class)).isEqualTo(empty());
    }

    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedFraOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("fra og med-dato er påkrevd");
        e.expectMessage("var null");
        create(null, "2007.12.31");
    }

    /**
     * Verifiserer at underlagsperiodene handhevar at til og med-dato ikkje kan vere løpande, dette for å sikre
     * at post-conditionen til oppbygginga av underlaget (som seier at eit underlag alltid skal representere ei lukka
     * tidsperiode) blir handheva.
     */
    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedTilOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato er påkrevd");
        e.expectMessage("var null");
        create("2007.12.31", null);
    }

    private Underlagsperiode create(final String fra, final String til) {
        return new Underlagsperiode(dato(fra), dato(til));
    }

    private Underlagsperiode eiPeriode() {
        return create("2007.01.01", "2007.12.31");
    }

}