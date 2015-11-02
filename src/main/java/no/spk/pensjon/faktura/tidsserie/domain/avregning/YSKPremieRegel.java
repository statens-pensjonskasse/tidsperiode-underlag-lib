package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#YSK}.
 * <br>
 * Merk at det forel�pig er udefinert korleis yrkesskadepremien skal avregnast. Premiebel�pa blir derfor
 * satt lik kr 0 for alle perioder inntil det er avklart.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class YSKPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return premier().bygg();
    }
}
