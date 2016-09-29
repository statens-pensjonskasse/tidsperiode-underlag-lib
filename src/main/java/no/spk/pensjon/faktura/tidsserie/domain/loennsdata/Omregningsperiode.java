package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.LocalDate;
import java.util.Optional;

/**
 * {@link Omregningsperiode} representerer ei tidsperiode der grunnbeløpet i folketrygda har samme verdi kvar dag.
 *
 * @author Tarjei Skorgenes
 */
public class Omregningsperiode extends AbstractTidsperiode<Omregningsperiode> {
    private final Grunnbeloep grunnbeloep;

    /**
     * Konstruerer ei ny omregningsperiode som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed første dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @param beloep grunnbeløp for perioden
     */
    public Omregningsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed, final Kroner beloep) {
        super(fraOgMed, tilOgMed);
        this.grunnbeloep = new Grunnbeloep(beloep);
    }

    /**
     * Annoterer underlagsperiode med gjeldande grunnbeløp.
     *
     * @param periode underlagsperioda som skal annoterast med grunnbeløp
     * @see no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Grunnbeloep.class, grunnbeloep);
    }

    @Override
    public String toString() {
        return "omregningsperiode "
                + fraOgMed()
                + "->"
                + tilOgMed().map(LocalDate::toString)
                + " med "
                + grunnbeloep
                ;
    }
}
