package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#YSK}.
 * <br>
 * Merk at det foreløpig er udefinert korleis yrkesskadepremien skal avregnast. Premiebeløpa blir derfor
 * satt lik kr 0 for alle perioder inntil det er avklart.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class YSKPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        final Prosent fakturerbarStillingsandel = periode.beregn(YrkesskadefaktureringRegel.class).andel();
        final Aarsfaktor aarsfaktor = periode.beregn(AarsfaktorRegel.class);
        final GrunnlagForYSK grunnlag = new GrunnlagForYSK(aarsfaktor.tilProsent().multiply(fakturerbarStillingsandel));

        return new YSKPensjonspremier().beregn(periode, grunnlag);
    }
}
