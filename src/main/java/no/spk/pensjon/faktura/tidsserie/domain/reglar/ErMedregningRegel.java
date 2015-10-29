package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Beregningsregel som indkerer korvidt stillinga er tilknytta ei medregning i den aktuelle perioda.
 * <br>
 * Perioda blir flagga dersom den har ei {@link Medregning}. Dersom den ikkje har det blir den flagga
 * som at medlemmet ikkje har ei medregning.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class ErMedregningRegel implements BeregningsRegel<Boolean> {
    @Override
    public Boolean beregn(final Beregningsperiode<?> periode) {
        return periode.valgfriAnnotasjonFor(Medregning.class).isPresent();
    }
}
