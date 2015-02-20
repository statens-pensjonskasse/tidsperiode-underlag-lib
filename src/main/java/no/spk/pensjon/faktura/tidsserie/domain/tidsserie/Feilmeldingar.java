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
     * l�nnstrinnperiode tilknytta samme l�nnstrinn.
     * <p>
     * Denne situasjonen indikerer at l�nntrinna systemet opererer med er
     * inkonsistente ettersom det aldri kan vere to aktive l�nnstrinnperioder
     * tilknytta samme l�nnstrinn innanfor ei tidsperiode.
     * <p>
     * For apotekordninga er det ein heilt normal situasjon � finne fleire gjeldande l�nnstrinnperioder med samme trinn
     * i samme periode, men der l�nnstrinna er tilknytta forskjellige stillingskoder. Stillingskode blir derfor tatt
     * hensyn til kun for POA og b�r kun sendast inn til feilmeldinga for oppslag som feilar for POA.
     *
     * @param periode            tidsperioda som l�nnstrinnperiodene overlappar
     * @param ordning            pensjonsordninga som l�nnstrinnperiodene tilh�yrer
     * @param loennstrinn        l�nnstrinnet som vi har detektert meir enn ei overlappande
     *                           l�nnstrinnperiode for
     * @param stillingskode      den valgfrie stillingskoda som kan ha p�virka kva l�nnstrinntabell ein har sl�tt opp l�nnstrinnperiodene fr� for apotekordninga
     * @param loennstrinnperiode l�nnstrinnperiodene som alle er tilknytta l�nnstrinnet og som
     *                           alle er gyldige innanfor den aktuelle tidsperioda  @return ei feilmelding med ein beskrivelse av kva som har feila og kva perioder som f�rte til feilen
     */
    public static String meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
            final Tidsperiode<?> periode,
            final Ordning ordning, final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode,
            final Tidsperiode<?>... loennstrinnperiode) {
        return "Det er oppdaga fleire l�nnstrinnperioder for "
                + loennstrinn
                + stillingskode.map(k -> " for " + k).orElse("")
                + " tilknytta ordning "
                + ordning
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

    /**
     * Opprettar ein ny reducer som feilar umiddelbart viss den blir kalla ein eller fleire gangar.
     * <p>
     * Feilen den genererer oppst�r dersom det eksisterer meir enn ein avtaleversjon tilknytta <code>avtale</code>
     * innanfor tidsperioda angitt av <code>periode</code>. Det blir d� umulig � avgjere kva for ein av dei to
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
            builder.append("Klarer ikkje � entydig avgjere kva som er gjeldande avtaleversjon for ")
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