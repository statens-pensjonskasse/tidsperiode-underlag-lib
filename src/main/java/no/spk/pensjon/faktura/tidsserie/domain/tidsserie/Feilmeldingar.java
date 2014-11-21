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
     * l�nnstrinnperiode tilknytta samme l�nnstrinn.
     * <p>
     * Denne situasjonen indikerer at l�nntrinna systemet opererer med er
     * inkonsistente ettersom det aldri kan vere to aktive l�nnstrinnperioder
     * tilknytta samme l�nnstrinn innanfor ei tidsperiode.
     *
     * @param periode            tidsperioda som l�nnstrinnperiodene overlappar
     * @param loennstrinn        l�nnstrinnet som vi har detektert meir enn ei overlappande
     *                           l�nnstrinnperiode for
     * @param loennstrinnperiode l�nnstrinnperiodene som alle er tilknytta l�nnstrinnet og som
     *                           alle er gyldige innanfor den aktuelle tidsperioda
     * @return ei feilmelding med ein beskrivelse av kva som har feila og kva perioder som f�rte til feilen
     */
    public static String meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
            final Tidsperiode<?> periode, final Loennstrinn loennstrinn,
            final Tidsperiode<?>... loennstrinnperiode) {
        return "Det er oppdaga fleire l�nnstrinnperioder for "
                + loennstrinn
                + " som overlappar perioda "
                + periode
                + ", oppslag av l�nn for l�nnstrinn krever "
                + "at det kun eksisterer 1 overlappande l�nnstrinnperiode pr tidsperiode\n"
                + "Overlappande loennstrinnperiode:\n"
                + asList(loennstrinnperiode)
                .stream()
                .map(p -> "- " + p)
                .collect(joining("\n"));
    }
}