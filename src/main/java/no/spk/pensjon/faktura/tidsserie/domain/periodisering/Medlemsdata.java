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
 * <h4>Stillingshistorikk</h4>
 * <p>
 * Informasjon henta frå stillingshistorikken skal inneholde følgjande verdiar, alle representert som tekst:
 * <table>
 * <thead>
 * <tr>
 * <td>Index</td>
 * <td>Verdi / Format</td>
 * <td>Beskrivelse</td>
 * <td>Kilde</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>0</td>
 * <td>0</td>
 * <td>Typeindikator som identifiserer rada som ei stillingsendring</td>
 * <td>Hardkoda</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>yyyyMMdd</td>
 * <td>Fødselsdato for medlem</td>
 * <td>TORT016.DAT_KUNDE_FOEDT_NUM</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>5-sifra tall</td>
 * <td>Personnummer for medlem</td>
 * <td>TORT016.IDE_KUNDE_PRSNR</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Long</td>
 * <td>Stillingsforholdnr</td>
 * <td>TORT016.IDE_SEKV_TORT125</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>4-sifra kode</td>
 * <td>Aksjonskoda som nærmare beskrive kva type stillingsendring det er snakk om</td>
 * <td>TORT016.TYP_AKSJONSKODE</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>Long</td>
 * <td>Organisasjonsnummer, ikkje i bruk</td>
 * <td>TORT016.IDE_ARBGIV_NR</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>3-sifra kode</td>
 * <td>Permisjonsavtale for stillingsendringar med aksjonskode 028, 029, 012</td>
 * <td>TORT016.TYP_PERMAVT</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>yyyyMMdd</td>
 * <td>Registreringsdato for når stillingsendringa vart registrert i PUMA</td>
 * <td>TORT016.DAT_REGISTRERT</td>
 * </tr>
 * <tr>
 * <td>8</td>
 * <td>Double</td>
 * <td>Stillingsprosent for stillinga endringa er tilknytta, er normalt sett ein verdi mellom 0 og 100, men kan for visse historiske årgangar og yrkesgrupper vere større enn 100</td>
 * <td>TORT016.RTE_DELTID</td>
 * </tr>
 * <tr>
 * <td>9</td>
 * <td>Integer</td>
 * <td>Lønnstrinn, for stillingar som ikkje innrapporterer lønn blir lønna innrapportert som lønnstrinn som kan benyttast for å slå opp lønn i 100% stilling.</td>
 * <td>TORT016.NUM_LTR</td>
 * </tr>
 * <tr>
 * <td>10</td>
 * <td>Integer</td>
 * <td>Deltidsjustert, innrapportert lønn for stillingar som ikkje blir innrapportert med lønnstrinn</td>
 * <td>TORT016.BEL_LONN</td>
 * </tr>
 * <tr>
 * <td>11</td>
 * <td>Integer</td>
 * <td>Faste lønnstillegg som blir utbetalt i tillegg til grunnlønna, skal innrapporterast deltidsjustert.</td>
 * <td>TORT016.BEL_FTILL</td>
 * </tr>
 * <tr>
 * <td>12</td>
 * <td>Integer</td>
 * <td>Variable lønnstillegg som blir utbetalt i tillegg til grunnlønna, skal innrapporterast deltidsjustert.</td>
 * <td>TORT016.BEL_VTILL</td>
 * </tr>
 * <tr>
 * <td>13</td>
 * <td>Integer</td>
 * <td>Funksjonstillegg som blir utbetalt i tillegg til grunnlønna, skal ikkje innrapporterast deltidsjustert.</td>
 * <td>TORT016.BEL_FUTILL</td>
 * </tr>
 * <tr>
 * <td>14</td>
 * <td>yyyyMMdd</td>
 * <td>Aksjonsdato, datoen stillingsendringa trer i kraft</td>
 * <td>TORT016.DAT_AKSJON</td>
 * </tr>
 * </tbody>
 * </table>
 * <h4>Avtalekobling</h4>
 * Ei avtalekobling skal inneholde følgjande verdiar, alle representert som tekst:
 * <table>
 * <thead>
 * <tr>
 * <td>Index</td>
 * <td>Verdi / Format</td>
 * <td>Beskrivelse</td>
 * <td>Kilde</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>0</td>
 * <td>1</td>
 * <td>Typeindikator som identifiserer rada som ei avtalekobling</td>
 * <td>Hardkoda</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>yyyyMMdd</td>
 * <td>Fødselsdato for medlem</td>
 * <td>TORT126.DAT_KUNDE_FOEDT_NUM</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>5-sifra tall</td>
 * <td>Personnummer for medlem</td>
 * <td>TORT126.IDE_KUNDE_PRSNR</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Long</td>
 * <td>Stillingsforholdnr</td>
 * <td>TORT126.IDE_SEKV_TORT125</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>yyyy.MM.dd</td>
 * <td>Startdato, første dag i perioda stillingsforholdet er tilknytta avtalen</td>
 * <td>TORT126.DAT_START</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>yyyy.MM.dd</td>
 * <td>Sluttdato, siste dag i perioda stillingsforholdet er tilknytta avtalen</td>
 * <td>TORT126.DAT_SLUTT</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>6-sifra tall</td>
 * <td>Avtalenummer, avtalen stillingsforholdet er tilknytta i den aktuelle perioda</td>
 * <td>TORT126.NUM_AVTALE_ID</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
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
