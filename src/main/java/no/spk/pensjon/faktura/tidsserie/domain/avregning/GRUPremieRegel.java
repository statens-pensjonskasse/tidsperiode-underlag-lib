package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Premieberegningsregel for {@link Produkt#GRU}.
 * <br>
 * Merk at det foreløpig er udefinert korleis gruppelivspremien skal avregnast. Premiebeløpa blir derfor
 * satt lik kr 0 for alle perioder inntil det er avklart.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class GRUPremieRegel implements BeregningsRegel<Premier> {
    @Override
    public Premier beregn(final Beregningsperiode<?> periode) {
        final FaktureringsandelStatus faktureringsandel = periode.beregn(GruppelivsfaktureringRegel.class);
        final Aarsfaktor aarsfaktor = periode.beregn(AarsfaktorRegel.class);
        final GrunnlagForGRU grunnlag = new GrunnlagForGRU(aarsfaktor, faktureringsandel);

        return new GRUpremier().beregn(periode, grunnlag);
    }
}
