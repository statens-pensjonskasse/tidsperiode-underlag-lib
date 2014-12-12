package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link OevreLoennsgrenseRegel} representerer regelen som avgjer kva som er øvre grense for lønna som
 * skal kunne inngå i maskinelt grunnlag.
 * <p>
 * Dei tre ordningane som er støtta brukar alle lønnsgrenser som er relativ til grunnbeløpet i folketrygda.
 * For SPK- og Opera-ordningane er det 12G som har vore øvre lønnsgrense dei siste 20-30 åra.
 * <p>
 * For Apotekordninga er det frå 1. januar 2008, 10G som er øvre lønnsgrense. Før denne datoen brukte ein ikkje ei
 * grense som er relativ til grunnbeløpet, desse eldre reglane er det ikkje implementert støtte for hittil.
 *
 * @author Tarjei Skorgenes
 */
public class OevreLoennsgrenseRegel implements BeregningsRegel<Kroner> {
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        final Stillingsprosent stillingsprosent = periode.annotasjonFor(Stillingsprosent.class);
        return grenseForFulltidsstilling(periode).multiply(stillingsprosent.prosent);
    }

    private Kroner grenseForFulltidsstilling(final Underlagsperiode periode) {
        if (erOrdning(periode, Ordning.SPK)) {
            return periode.annotasjonFor(Grunnbeloep.class).multiply(12);
        }
        if (erOrdning(periode, Ordning.OPERA)) {
            return periode.annotasjonFor(Grunnbeloep.class).multiply(12);
        }
        if (erOrdning(periode, Ordning.POA)) {
            return periode.annotasjonFor(Grunnbeloep.class).multiply(10);
        }
        // Fallback, vi støttar ingen andre ordningar enn dei 3 over, ergo skal ingen andre ordningar kunne få ein verdi
        // større enn 0 med fastsats-metodikken
        return new Kroner(0);
    }

    private boolean erOrdning(final Underlagsperiode periode, final Ordning ordning) {
        return ordning.equals(periode.annotasjonFor(Ordning.class));
    }
}
