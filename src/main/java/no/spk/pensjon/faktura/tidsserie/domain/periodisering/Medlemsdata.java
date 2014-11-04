package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata} representerer all
 * informasjon tilknytta eit bestemt medlem, som er påkrevd for å støtte premie- og lønnsberegning for medlemet.
 * <p>
 * Dei tre primære datatypene som er påkrevd for dette er stillingsendringar frå stillingshistorikken,
 * medregningar og avtalekoblingar for alle stillingsforholda tilknytta medlemmet og som ein skal gjere beregningar
 * for.
 * <p>
 * Den medlemsspesifikke informasjonen som objektet inneheld blir internt og eksternt handtert som ei samling med tekstverdiar,
 * organisert som rader og kolonner i form av ei liste som inneheld lister som igjen inneheld strengar.
 * Denne representaasjonen er valgt for å holde serialiseringa enkel og forenkle handtering av stillingshistorikk,
 * medregning og avtalekoblingar utan å måtte lage spesialisert serialisering for kvar og ein av desse.
 * <p>
 * Kontrakta for korleis kvar rad må sjå ut, varierer mellom dei 3 forskjellige datatypene.
 * <p>
 * <h4>Avtalekobling</h4>
 * Ei avtalekobling skal inneholde følgjande verdiar, alle representert som tekst:
 * <table>
 * <thead>
 * <tr>
 * <td>Index</td>
 * <td>Verdi / Format</td>
 * <td>Beskrivelse</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>0</td>
 * <td>1</td>
 * <td>Typeindikator som identifiserer rada som ei avtalekobling</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>Long</td>
 * <td>Stillingsforholdnr</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>yyyyMMdd</td>
 * <td>Fødselsdato for medlem</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>5-sifra tall</td>
 * <td>Personnummer for medlem</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>yyyy.MM.dd</td>
 * <td>Startdato, første dag i perioda stillingsforholdet er tilknytta avtalen</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>yyyy.MM.dd</td>
 * <td>Sluttdato, siste dag i perioda stillingsforholdet er tilknytta avtalen</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>6-sifra tall</td>
 * <td>Avtalenummer, avtalen stillingsforholdet er tilknytta i den aktuelle perioda</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsdata {
    private final List<List<String>> data;

    /**
     * Konstruerer eit nytt sett med medlemsdata basert på <code>medlemsdata</code>.
     * <p>
     * Referansen til datasettet som blir sendt inn blir brukt direkte for å unngå allokering og kopiering til ei ny
     * samling, data gjort på <code>medlemsdata</code> etter konstruksjon vil derfor vere direkte synlig for Medlemsdata.
     *
     * @param medlemsdata
     * @throws java.lang.NullPointerException     viss <code>medlemsdata</code> er <code>null</code>
     * @throws java.lang.IllegalArgumentException viss <code>medlemsdata</code> ikkje inneheld noko informasjon og er tom
     */
    public Medlemsdata(final List<List<String>> medlemsdata) {
        requireNonNull(medlemsdata, () -> "medlemsdata er påkrevd, men var null");
        if (medlemsdata.isEmpty()) {
            throw new IllegalArgumentException(
                    "medlemsdata må inneholde minst ei stillingsendring, medregning eller avtalekobling, men var tom"
            );
        }

        this.data = medlemsdata;
    }

    /**
     * Hentar ut alle stillingsendringar tilknytta medlemmet.
     *
     * @return alle stillingsendring for medlemmet
     */
    Iterable<Stillingsendring> alleStillingsendringar() {
        return data
                .stream()
                .filter(e -> "0".equals(e.get(0)))
                .map(e -> new Stillingsendring())
                .collect(toSet());
    }

    /**
     * Hentar ut alle avtalekoblingar tilknytta medlemmet.
     *
     * @return alle avtalekoblingar for medlemmet
     */
    Iterable<Avtalekoblingsperiode> alleAvtalekoblingsperioder() {
        return data
                .stream()
                .filter(e -> "1".equals(e.get(0)))
                .map(e -> new Avtalekoblingsperiode())
                .collect(toSet());
    }

    /**
     * Genererer ei tekstlig oversikt over all medlemsinformasjonen, gruppert på type.
     *
     * @return ein tekstlig representaasjon av alle medlemsdatane som er tilgjengelig for medlemmet
     */
    @Override
    public String toString() {
        final Map<String, List<List<String>>> gruppert = data
                .stream()
                .collect(
                        groupingBy(e -> e.get(0))
                );
        final StringBuilder builder = new StringBuilder();
        builder.append("Medlemsdata for medlem ").append("XXXXXXXXXXX").append('\n');

        for (final Map.Entry<String, List<List<String>>> e : gruppert.entrySet()) {
            builder.append("Type ").append(e.getKey()).append(":\n");
            List<List<String>> value = e.getValue();
            for (List<String> row : value) {
                builder
                        .append('\t')
                        .append("- ")
                        .append(row)
                        .append('\n');
            }
        }
        return builder.toString();
    }
}
