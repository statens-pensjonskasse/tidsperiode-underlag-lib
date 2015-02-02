package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link OevreLoennsgrenseRegel} representerer regelen som avgjer kva som er �vre grense for l�nna som
 * skal kunne inng� i maskinelt grunnlag.
 * <p>
 * Dei tre ordningane som er st�tta brukar alle l�nnsgrenser som er relativ til grunnbel�pet i folketrygda.
 * For SPK- og Opera-ordningane er det 12G som har vore �vre l�nnsgrense dei siste 20-30 �ra.
 * <p>
 * For Apotekordninga er det fr� 1. januar 2008, 10G som er �vre l�nnsgrense. F�r denne datoen brukte ein ikkje ei
 * grense som er relativ til grunnbel�pet, desse eldre reglane er det ikkje implementert st�tte for hittil.
 *
 * @author Tarjei Skorgenes
 */
public class OevreLoennsgrenseRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar kva som er gjeldande �vre grense for �rsl�nn innanfor den aktuelle tidsperioda.
     * <p>
     * For medregningar er ikkje konseptet �vre grense godt nok definert, regelen returnerer derfor fulltidsgrensa for
     * ordninga dersom perioda er tilknytta medregning.
     * <p>
     * NB: Grenseverdien blir ikkje avkorta i henhold til �rsfaktor, verdien som blir returnert av regelen er alts�
     * gjeldande grenseverdi for eit heilt �r, basert p� verdiane som er gjeldande innanfor den aktuelle tidsperioda.
     * <p>
     * Eksempel:
     * <p>
     * Ei underlagsperiode strekker seg fr� 1. januar 2011 til 30. april 2011, grunnbel�pet er i perioda lik kr 50 000,
     * stillingsprosenten er 100% og gjeldande ordning er SPK.
     * <p>
     * Verdien returnert av regelen bli i dette tilfellet lik kr 600 000, ikkje 120/365 * kr 600 000.
     *
     * @param periode underlagsperioda som inneheld informasjonen som blir brukt for � utlede �vre grense
     * @return gjeldande �vre grense for maskinelt grunnlag innanfor den aktuelle perioda
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        final Kroner fulltidsgrense = grenseForFulltidsstilling(periode);
        if (periode.valgfriAnnotasjonFor(Medregning.class).isPresent()) {
            return fulltidsgrense;
        }
        final Stillingsprosent stillingsprosent = periode.annotasjonFor(Stillingsprosent.class);
        return fulltidsgrense.multiply(stillingsprosent.prosent());
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
        // Fallback, vi st�ttar ingen andre ordningar enn dei 3 over, ergo skal ingen andre ordningar kunne f� ein verdi
        // st�rre enn 0 med fastsats-metodikken
        return new Kroner(0);
    }

    private boolean erOrdning(final Underlagsperiode periode, final Ordning ordning) {
        return ordning.equals(periode.annotasjonFor(Ordning.class));
    }
}
