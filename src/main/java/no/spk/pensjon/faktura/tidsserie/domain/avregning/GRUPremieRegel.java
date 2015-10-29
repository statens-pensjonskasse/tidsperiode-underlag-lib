package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#GRU}.
 * <br>
 * Merk at det forel�pig er udefinert korleis gruppelivspremien skal avregnast. Premiebel�pa blir derfor
 * satt lik kr 0 for alle perioder inntil det er avklart.
 *
 * @author Tarjei Skorgenes
 */
public class GRUPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return premier().bygg();
    }
}
