package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

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
    /**
     * Beregnar kva som er gjeldande øvre grense for årslønn innanfor den aktuelle tidsperioda.
     * <p>
     * For medregningar er ikkje konseptet øvre grense godt nok definert, regelen returnerer derfor fulltidsgrensa for
     * ordninga dersom perioda er tilknytta medregning.
     * <p>
     * NB: Grenseverdien blir ikkje avkorta i henhold til årsfaktor, verdien som blir returnert av regelen er altså
     * gjeldande grenseverdi for eit heilt år, basert på verdiane som er gjeldande innanfor den aktuelle tidsperioda.
     * <p>
     * Eksempel:
     * <p>
     * Ei underlagsperiode strekker seg frå 1. januar 2011 til 30. april 2011, grunnbeløpet er i perioda lik kr 50 000,
     * stillingsprosenten er 100% og gjeldande ordning er SPK.
     * <p>
     * Verdien returnert av regelen bli i dette tilfellet lik kr 600 000, ikkje 120/365 * kr 600 000.
     *
     * @param periode underlagsperioda som inneheld informasjonen som blir brukt for å utlede øvre grense
     * @return gjeldande øvre grense for maskinelt grunnlag innanfor den aktuelle perioda
     */
    @Override
    public Kroner beregn(final Beregningsperiode<?> periode) {
        final Kroner fulltidsgrense = grenseForFulltidsstilling(periode);
        if (periode.beregn(ErMedregningRegel.class)) {
            return fulltidsgrense;
        }
        final Stillingsprosent stillingsprosent = periode.annotasjonFor(Stillingsprosent.class);
        return fulltidsgrense.multiply(stillingsprosent.prosent());
    }

    private Kroner grenseForFulltidsstilling(final Beregningsperiode<?> periode) {
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

    private boolean erOrdning(final Beregningsperiode<?> periode, final Ordning ordning) {
        return ordning.equals(periode.annotasjonFor(Ordning.class));
    }

}
