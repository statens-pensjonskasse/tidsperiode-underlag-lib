package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Beregningsregel som indikerer korvidt stillinga er under minstegrensa i den aktuelle perioda.
 * <br>
 * Dersom stillinga er {@link ErMedregningRegel flagga} som ei medregning, blir det tolka som at stillinga
 * ikkje er under minstegrensa.
 * <br>
 * Dersom stillinga ikkje er flagga som medregning, blir gjeldande minstegrense for perioda
 * {@link MedregningsRegel henta ut} og samanlikna med stillinga sin {@link Stillingsprosent} i perioda.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class ErUnderMinstegrensaRegel implements BeregningsRegel<Boolean> {
    @Override
    public Boolean beregn(final Beregningsperiode<?> periode) {
        if (periode.beregn(ErMedregningRegel.class)) {
            return false;
        }
        return periode.beregn(MinstegrenseRegel.class).erUnderMinstegrensa(periode.annotasjonFor(Stillingsprosent.class));
    }
}
