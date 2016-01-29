package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.AFP;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#AFP}.
 *
 * @author Tarjei Skorgenes
 * @see PensjonsproduktPensjonspremier#beregn(Beregningsperiode, Produkt)
 * @since 1.2.0
 */
public class AFPPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return new PensjonsproduktPensjonspremier().beregn(periode, AFP);
    }
}
