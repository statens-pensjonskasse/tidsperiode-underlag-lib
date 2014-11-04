package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata} representerer all
 * informasjon tilknytta eit bestemt medlem, som er p�krevd for � st�tte premie- og l�nnsberegning for medlemet.
 * <p>
 * Dei tre prim�re datatypene som er p�krevd for dette er stillingsendringar fr� stillingshistorikken,
 * medregningar og avtalekoblingar for alle stillingsforholda tilknytta medlemmet og som ein skal gjere beregningar
 * for.
 * <p>
 * Den medlemsspesifikke informasjonen som objektet inneheld blir internt og eksternt handtert som ei samling med tekstverdiar,
 * organisert som rader og kolonner i form av ei liste som inneheld lister som igjen inneheld strengar.
 * Denne representaasjonen er valgt for � holde serialiseringa enkel og forenkle handtering av stillingshistorikk,
 * medregning og avtalekoblingar utan � m�tte lage spesialisert serialisering for kvar og ein av desse.
 * <p>
 * Kontrakta for korleis kvar rad m� sj� ut, varierer mellom dei 3 forskjellige datatypene.
 * <p>
 * <h4>Avtalekobling</h4>
 * Ei avtalekobling skal inneholde f�lgjande verdiar, alle representert som tekst:
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
 * <td>F�dselsdato for medlem</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>5-sifra tall</td>
 * <td>Personnummer for medlem</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>yyyy.MM.dd</td>
 * <td>Startdato, f�rste dag i perioda stillingsforholdet er tilknytta avtalen</td>
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
    private final List<List<String>> endringar;

    /**
     * Konstruerer eit nytt sett med medlemsdata basert p� <code>medlemsdata</code>.
     * <p>
     * Referansen til datasettet som blir sendt inn blir brukt direkte for � unng� allokering og kopiering til ei ny
     * samling, endringar gjort p� <code>medlemsdata</code> etter konstruksjon vil derfor vere direkte synlig for Medlemsdata.
     *
     * @param medlemsdata
     * @throws java.lang.NullPointerException     viss <code>medlemsdata</code> er <code>null</code>
     * @throws java.lang.IllegalArgumentException viss <code>medlemsdata</code> ikkje inneheld noko informasjon og er tom
     */
    public Medlemsdata(final List<List<String>> medlemsdata) {
        requireNonNull(medlemsdata, () -> "medlemsdata er p�krevd, men var null");
        if (medlemsdata.isEmpty()) {
            throw new IllegalArgumentException(
                    "medlemsdata m� inneholde minst ei stillingsendring, medregning eller avtalekobling, men var tom"
            );
        }

        this.endringar = medlemsdata;
    }

    /**
     * Hentar ut alle stillingsendringar tilknytta medlemmet.
     *
     * @return alle stillingsendring for medlemmet
     */
    Iterable<Stillingsendring> alleStillingsendringar() {
        return endringar
                .stream()
                .filter(e -> "0".equals(e.get(0)))
                .map(e -> new Stillingsendring())
                .collect(toSet());
    }
}
