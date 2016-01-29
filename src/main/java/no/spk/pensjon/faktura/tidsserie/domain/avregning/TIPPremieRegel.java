package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.TIP;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#TIP}.
 * <br>
 * Merk at det kun er avtalar med fiktivt fond som har tillegspremie-produktet. Sjølv om desse avtalane ikkje skal
 * avregnast blir det beregna årspremieandel for TIP-produktet så ein kan holde oversikt over kor mykje premie
 * avtalen skulle ha betalt for perioda om ein hadde valgt å avregne avtalar med fiktiv fond.
 *
 * @author Tarjei Skorgenes
 * @see PensjonsproduktPensjonspremier#beregn(Beregningsperiode, Produkt)
 * @since 1.2.0
 */
public class TIPPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return new PensjonsproduktPensjonspremier().beregn(periode, TIP);
    }
}
