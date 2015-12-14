package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Beregningsregel som indikerer korvidt medlemmet er ute i permisjon utan lønn frå stillinga i den aktuelle perioda.
 * <br>
 * Medlemmet blir flagga som ute i permisjon utan lønn dersom periodas aksjonskode er lik
 * {@link Aksjonskode#PERMISJON_UTAN_LOENN}.
 * <br>
 * Dersom perioda har ei anna aksjonskode, eller dersom perioda manglar aksjonskode, blir ikkje perioda flagga som
 * ute i permisjon utan lønn.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class ErPermisjonUtanLoennRegel implements BeregningsRegel<Boolean> {
    @Override
    public Boolean beregn(final Beregningsperiode<?> periode) {
        return periode.valgfriAnnotasjonFor(Aksjonskode.class)
                .map(Aksjonskode.PERMISJON_UTAN_LOENN::equals)
                .orElse(false);
    }
}
