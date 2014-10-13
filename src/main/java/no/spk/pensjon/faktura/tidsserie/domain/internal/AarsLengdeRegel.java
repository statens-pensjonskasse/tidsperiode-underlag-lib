package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import static no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagar.antallDagarMellom;

/**
 * Beregningsregel som reknar ut lengda p� �ret
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode underlagsperioda} er tilknytta.
 *
 * @author Tarjei Skorgenes
 */
public class AarsLengdeRegel implements BeregningsRegel<AntallDagar> {
    /**
     * Beregnar antall dagar i �rstallet underlagsperioda er tilknytta.
     * <br>
     * Underlagsperioda blir tilknytta eit �r via ein p�krevd annotasjon av type {@link Aarstall}.
     *
     * @param periode underlagsperioda som er annotert med �rstallet lengda skal beregnast for
     * @return antall dagar i �ret underlagsperioda er annotert med
     * @throws PaakrevdAnnotasjonManglarException dersom perioda ikkje er annotert med {@link Aarstall}
     * @see Aarstall#atStartOfYear()
     * @see Aarstall#atEndOfYear()
     */
    @Override
    public AntallDagar beregn(final Underlagsperiode periode) throws PaakrevdAnnotasjonManglarException {
        final Aarstall aar = periode.annotasjonFor(Aarstall.class);
        return antallDagarMellom(
                aar.atStartOfYear(),
                aar.atEndOfYear()
        );
    }
}
