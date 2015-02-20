package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

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

    /**
     * Opprettar ein ny reducer som feilar umiddelbart viss den blir kalla ein eller fleire gangar.
     * <p>
     * Feilen den genererer oppstår dersom det eksisterer meir enn ein avtaleversjon tilknytta <code>avtale</code>
     * innanfor tidsperioda angitt av <code>periode</code>. Det blir då umulig å avgjere kva for ein av dei to
     * avtaleversjonane som skal benyttast ved vidare annotering av underlagsperioda.
     *
     * @param avtale  avtalen
     * @param periode underlagsperioda som det eksisterer meir enn ein gjeldande avtaleversjon for
     *                og der alle versjonane er tilknytta <code>avtale</code>
     * @return ein ny reducer som feilar dersom den blir kalla ein eller fleire gangar
     */
    public static BinaryOperator<Avtaleversjon> feilDersomPeriodaOverlapparMeirEnnEinAvtaleversjon(
            final AvtaleId avtale, final Tidsperiode<?> periode) {
        return (a, b) -> {
            final StringBuilder builder = new StringBuilder();
            builder.append("Klarer ikkje å entydig avgjere kva som er gjeldande avtaleversjon for ")
                    .append(avtale)
                    .append(" i perioda ")
                    .append(periode)
                    .append(".\nAvtaleversjonar som overlappar perioda:\n");
            Stream.of(a, b).forEach(versjon -> {
                builder.append("- ");
                builder.append(versjon.toString());
                builder.append('\n');
            });
            throw new IllegalStateException(builder.toString());
        };
    }
}