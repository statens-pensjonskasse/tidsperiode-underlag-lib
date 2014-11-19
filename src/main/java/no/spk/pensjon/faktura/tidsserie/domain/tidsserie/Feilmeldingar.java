package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Feilmeldingar generert av tidsserie-genereringa.
 *
 * @author Tarjei Skorgenes
 */
public class Feilmeldingar {
    /**
     * Feilmelding for tidsperioder som blir overlappa av meir enn 1
     * lønnstrinnperiode tilknytta samme lønnstrinn.
     * <p>
     * Denne situasjonen indikerer at lønntrinna systemet opererer med er
     * inkonsistente ettersom det aldri kan vere to aktive lønnstrinnperioder
     * tilknytta samme lønnstrinn innanfor ei tidsperiode.
     *
     * @param periode            tidsperioda som lønnstrinnperiodene overlappar
     * @param loennstrinn        lønnstrinnet som vi har detektert meir enn ei overlappande
     *                           lønnstrinnperiode for
     * @param loennstrinnperiode lønnstrinnperiodene som alle er tilknytta lønnstrinnet og som
     *                           alle er gyldige innanfor den aktuelle tidsperioda
     * @return ei feilmelding med ein beskrivelse av kva som har feila og kva perioder som førte til feilen
     */
    public static String meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
            final Tidsperiode<?> periode, final Loennstrinn loennstrinn,
            final Tidsperiode<?>... loennstrinnperiode) {
        return "Det er oppdaga fleire lønnstrinnperioder for "
                + loennstrinn
                + " som overlappar perioda "
                + periode
                + ", oppslag av lønn for lønnstrinn krever "
                + "at det kun eksisterer 1 overlappande lønnstrinnperiode pr tidsperiode\n"
                + "Overlappande loennstrinnperiode:\n"
                + asList(loennstrinnperiode)
                .stream()
                .map(p -> "- " + p)
                .collect(joining("\n"));
    }
}