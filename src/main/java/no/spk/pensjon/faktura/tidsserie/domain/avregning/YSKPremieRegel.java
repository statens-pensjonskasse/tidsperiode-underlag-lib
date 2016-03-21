package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#YSK}.
 * <br>
 * Premiebeløp skal ikke beregnes på periodenivå for YSK. Premiebeløpa blir derfor
 * satt lik kr 0 for alle perioder.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class YSKPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        return Premier.premier().bygg();
    }
}
