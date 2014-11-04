package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
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
 * <h4>Stillingshistorikk</h4>
 * <p>
 * Informasjon henta fr� stillingshistorikken blir mappa om og representert som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring}. Dette blir automatisk utf�rt for alle
 * medlemsdata som har verdien <code>0</code> som sin typeindikator.
 * <p>
 * Sj� {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter} for meir detaljert
 * informasjon om konktrakta p� formatet som slike endringar m� oppfylle.
 * <p>
 * <h4>Avtalekobling</h4>
 * <p>
 * Informasjon henta fr� stillingsforholdets avtalekoblingar blir mappa om og representert som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode}. Dette blir automatisk utf�rt for
 * alle medlemsdata som har verdien <code>1</code> som sin typeindikator.
 * <p>
 * Sj� {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter} for meir detaljert
 * informasjon om konktrakta p� formatet som slike endringar m� oppfylle.
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsdata {
    private final Map<?, MedlemsdataOversetter<?>> oversettere;

    private final List<List<String>> data;

    /**
     * Konstruerer eit nytt sett med medlemsdata basert p� <code>medlemsdata</code>.
     * <p>
     * Referansen til datasettet som blir sendt inn blir brukt direkte for � unng� allokering og kopiering til ei ny
     * samling, data gjort p� <code>medlemsdata</code> etter konstruksjon vil derfor vere direkte synlig for Medlemsdata.
     * <p>
     * Ettersom kontrakta for formatet p� verdiane i <code>medlemsdata</code> er at alt skal representerast som svakt typa
     * lister av tekst, er det implementert st�tte for � konvertere fr� desse tekstlige verdaine til sterkt typa
     * datatyper som er bedre tilrettelagt for vidare behdnaling. Denne konverteringa blir ikkje utf�rt ved konstruksjon,
     * den blir utf�rt kvar gang metoder som returnerer sterkt typa data, blir kalla p� det konstruerte objektet.
     *
     * @param medlemsdata datasettet som inneholder den medlemsspesifikke informasjonen som skal behandles
     * @param oversettere oversettere som er ansvarlige for � konvertere radene i <code>medlemsdata</code> til sterkt
     *                    typa verdiar ved senere behandling
     * @throws java.lang.NullPointerException     viss noen av parameterverdiene er <code>null</code>
     * @throws java.lang.IllegalArgumentException viss <code>medlemsdata</code> ikkje inneheld noko informasjon og er tom
     */
    public Medlemsdata(final List<List<String>> medlemsdata, final Map<Class<?>, MedlemsdataOversetter<?>> oversettere) {
        requireNonNull(medlemsdata, () -> "medlemsdata er p�krevd, men var null");
        requireNonNull(oversettere, () -> "oversettere er p�krevd, men var null");
        if (medlemsdata.isEmpty()) {
            throw new IllegalArgumentException(
                    "medlemsdata m� inneholde minst ei stillingsendring, medregning eller avtalekobling, men var tom"
            );
        }
        this.oversettere = oversettere;
        this.data = medlemsdata;
    }

    /**
     * Hentar ut alle stillingsendringar tilknytta medlemmet.
     *
     * @return alle stillingsendring for medlemmet
     */
    Iterable<Stillingsendring> alleStillingsendringar() {
        return finnOgOversett(Stillingsendring.class);
    }

    /**
     * Hentar ut alle avtalekoblingar tilknytta medlemmet.
     *
     * @return alle avtalekoblingar for medlemmet
     */
    Iterable<Avtalekoblingsperiode> alleAvtalekoblingsperioder() {
        return finnOgOversett(Avtalekoblingsperiode.class);
    }

    /**
     * Genererer ei tekstlig oversikt over all medlemsinformasjonen, gruppert p� type.
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

    /**
     * Lokaliserer alle rader som inneheld informasjon som kan konverterast til
     * datatypen angitt av <code>type</code> og oversetter kvar rad til denne typen.
     * <p>
     * Dersom det ikkje er registrert nokon oversetter for <code>type</code> vil denne metode ikkje feile, men kun returnere
     * ei tom samling.
     *
     * @param type datatypen som lokaliserast og konverterast
     * @param <T>  datatypen som lokaliserast og konverterast
     * @return alle medlemsdata for den angitte typen
     */
    private <T> Iterable<T> finnOgOversett(final Class<T> type) {
        final MedlemsdataOversetter<T> oversetter = lookup(type).orElse(new NullOversetter<T>());
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett)
                .collect(toSet());
    }

    private <T> Optional<MedlemsdataOversetter<T>> lookup(final Class<? extends T> datatype) {
        return ofNullable((MedlemsdataOversetter<T>) oversettere.get(datatype));
    }

    private static class NullOversetter<T> implements MedlemsdataOversetter<T> {
        @Override
        public T oversett(List<String> rad) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean supports(List<String> rad) {
            return false;
        }
    }
}
