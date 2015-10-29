package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.AFP;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#AFP}.
 *
 * @author Tarjei Skorgenes
 * @see Pensjonspremier#beregn(Beregningsperiode, Produkt)
 */
public class AFPPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return new Pensjonspremier().beregn(periode, AFP);
    }
}
