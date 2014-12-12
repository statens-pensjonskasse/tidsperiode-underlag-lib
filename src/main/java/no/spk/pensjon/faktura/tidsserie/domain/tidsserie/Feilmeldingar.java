package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import java.util.Optional;

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
     * <p>
     * For apotekordninga er det ein heilt normal situasjon å finne fleire gjeldande lønnstrinnperioder med samme trinn
     * i samme periode, men der lønnstrinna er tilknytta forskjellige stillingskoder. Stillingskode blir derfor tatt
     * hensyn til kun for POA og bør kun sendast inn til feilmeldinga for oppslag som feilar for POA.
     *
     * @param periode            tidsperioda som lønnstrinnperiodene overlappar
     * @param ordning            pensjonsordninga som lønnstrinnperiodene tilhøyrer
     * @param loennstrinn        lønnstrinnet som vi har detektert meir enn ei overlappande
     *                           lønnstrinnperiode for
     * @param stillingskode      den valgfrie stillingskoda som kan ha påvirka kva lønnstrinntabell ein har slått opp lønnstrinnperiodene frå for apotekordninga
     * @param loennstrinnperiode lønnstrinnperiodene som alle er tilknytta lønnstrinnet og som
     *                           alle er gyldige innanfor den aktuelle tidsperioda  @return ei feilmelding med ein beskrivelse av kva som har feila og kva perioder som førte til feilen
     */
    public static String meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
            final Tidsperiode<?> periode,
            final Ordning ordning, final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode,
            final Tidsperiode<?>... loennstrinnperiode) {
        return "Det er oppdaga fleire lønnstrinnperioder for "
                + loennstrinn
                + stillingskode.map(k -> " for " + k).orElse("")
                + " tilknytta ordning "
                + ordning
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