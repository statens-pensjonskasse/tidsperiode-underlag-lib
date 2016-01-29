package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#PEN}.
 *
 * @author Tarjei Skorgenes
 * @see PensjonsproduktPensjonspremier#beregn(Beregningsperiode, Produkt)
 * @since 1.2.0
 */
public class PENPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return new PensjonsproduktPensjonspremier().beregn(periode, PEN);
    }
}