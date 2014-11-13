package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPerioder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
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
 * Kontrakta for korleis kvar rad må sjå ut, varierer mellom dei 3 forskjellige datatypene. Men 4 felt er likevel
 * påkrevd for at ting skal fungere som ønska:
 * <ol>
 * <li>Typeindikator</li>
 * <li>Fødselsdato</li>
 * <li>Personnummer</li>
 * <li>Stillingsforholdnummer</li>
 * </ol>
 * <p>
 * <h4>Stillingshistorikk</h4>
 * <p>
 * Informasjon henta frå stillingshistorikken blir mappa om og representert som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring}. Dette blir automatisk utført for alle
 * medlemsdata som har verdien <code>0</code> som sin typeindikator.
 * <p>
 * Sjå {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter} for meir detaljert
 * informasjon om konktrakta på formatet som slike endringar må oppfylle.
 * <p>
 * <h4>Avtalekobling</h4>
 * <p>
 * Informasjon henta frå stillingsforholdets avtalekoblingar blir mappa om og representert som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode}. Dette blir automatisk utført for
 * alle medlemsdata som har verdien <code>1</code> som sin typeindikator.
 * <p>
 * Sjå {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter} for meir detaljert
 * informasjon om konktrakta på formatet som slike endringar må oppfylle.
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsdata {
    /**
     * Indexen stillingsforholdnummeret blir henta frå i radene som utgjer medlemsdatane.
     */
    private static final int INDEX_STILLINGSFORHOLD_ID = 3;

    private final Map<?, MedlemsdataOversetter<?>> oversettere;

    private final List<List<String>> data;

    /**
     * Konstruerer eit nytt sett med medlemsdata basert på <code>medlemsdata</code>.
     * <p>
     * Referansen til datasettet som blir sendt inn blir brukt direkte for å unngå allokering og kopiering til ei ny
     * samling, data gjort på <code>medlemsdata</code> etter konstruksjon vil derfor vere direkte synlig for Medlemsdata.
     * <p>
     * Ettersom kontrakta for formatet på verdiane i <code>medlemsdata</code> er at alt skal representerast som svakt typa
     * lister av tekst, er det implementert støtte for å konvertere frå desse tekstlige verdaine til sterkt typa
     * datatyper som er bedre tilrettelagt for vidare behdnaling. Denne konverteringa blir ikkje utført ved konstruksjon,
     * den blir utført kvar gang metoder som returnerer sterkt typa data, blir kalla på det konstruerte objektet.
     *
     * @param medlemsdata datasettet som inneholder den medlemsspesifikke informasjonen som skal behandles
     * @param oversettere oversettere som er ansvarlige for å konvertere radene i <code>medlemsdata</code> til sterkt
     *                    typa verdiar ved senere behandling
     * @throws java.lang.NullPointerException     viss noen av parameterverdiene er <code>null</code>
     * @throws java.lang.IllegalArgumentException viss <code>medlemsdata</code> ikkje inneheld noko informasjon og er tom
     */
    public Medlemsdata(final List<List<String>> medlemsdata, final Map<Class<?>, MedlemsdataOversetter<?>> oversettere) {
        requireNonNull(medlemsdata, () -> "medlemsdata er påkrevd, men var null");
        requireNonNull(oversettere, () -> "oversettere er påkrevd, men var null");
        if (medlemsdata.isEmpty()) {
            throw new IllegalArgumentException(
                    "medlemsdata må inneholde minst ei stillingsendring, medregning eller avtalekobling, men var tom"
            );
        }
        this.oversettere = oversettere;
        this.data = medlemsdata;
    }

    /**
     * Listar ut alle avtalekoblingar tilknytta medlemmet sine stillingsforhold, som matchar
     * det angitte søkekriteriet.
     *
     * @param predikat eit predikat som indikerer kva avtalekoblingar som skal returnerast
     * @return ein straum med alle avtalekoblingane som tilhøyrer medlemmet sine stillingsforhold og som ikkje
     * blir filtrert bort
     */
    public Stream<Avtalekoblingsperiode> avtalekoblingar(final Predicate<Avtalekoblingsperiode> predikat) {
        return finnOgOversett(Avtalekoblingsperiode.class).filter(predikat);
    }

    /**
     * Listar ut alle unike stillingsforhold som vi har medlemsdata tilknytta.
     *
     * @return ein straum med alle dei unike stillingsforholda som medlemmet er tilknytta
     */
    public Stream<StillingsforholdId> allePeriodiserbareStillingsforhold() {
        // Dersom vi endrar designet til å også ta inn data som ikkje er kobla mot stillingsforhold må denne straumen
        // filtrerast til kun å inneholde data tilknytta stillingsforhold
        return data
                .stream()
                .map(e -> e.get(INDEX_STILLINGSFORHOLD_ID))
                .map(Long::valueOf)
                .map(StillingsforholdId::new)
                .distinct();
    }

    /**
     * Periodiserer alle unike stillingsforhold som vi har stillingsendringar tilknytta.
     * <p>
     * For kvart stillingsforhold blir alle tilknytta stillingsendringar henta ut og brukt for å bygge opp
     * ein ny instans av {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPerioder} for kvart
     * stillingsforhold.
     * <p>
     * For stillingsforhold som ikkje har nokon stillingsendringar men som har medregning vil det foreløpig ikkje blir
     * generert nokon periodisering basert på dette.
     *
     * @return ein straum som inneheld alle stillingsforholdperioder for alle stillingsforhold som vi har stillingsendringar for
     * @see #allePeriodiserbareStillingsforhold()
     */
    public Stream<StillingsforholdPerioder> alleStillingsforholdPerioder() {
        return allePeriodiserbareStillingsforhold()
                .map(id -> periodiserStillingsforhold(id))
                .filter(o -> o.isPresent())
                .map(o -> o.get());
    }

    /**
     * Periodiserer stillingsendringar eller medregningar tilknytta eit bestemt stillingsforhold.
     * <p>
     * TODO: Implementere støtte for periodisering basert på medregning.
     *
     * @param stillingsforhold stillingsforholdet som skal periodiserast
     * @return ein periodisert representasjon av stillingsforholdet viss det er tilknytta stillingsendringar eller medregning,
     * eller {@link java.util.Optional#empty()} dersom det ikkje eksisterer nokon informasjon tilknytta stillingsforholdet
     */
    private Optional<StillingsforholdPerioder> periodiserStillingsforhold(final StillingsforholdId stillingsforhold) {
        return periodiserHistorikk(stillingsforhold);
    }

    /**
     * Hentar ut alle stillingsendringar tilknytta medlemmet.
     *
     * @return alle stillingsendring for medlemmet
     */
    Iterable<Stillingsendring> alleStillingsendringar() {
        return finnOgOversett(Stillingsendring.class).collect(toSet());
    }

    /**
     * Hentar ut alle avtalekoblingar tilknytta medlemmet.
     *
     * @return alle avtalekoblingar for medlemmet
     */
    Iterable<Avtalekoblingsperiode> alleAvtalekoblingsperioder() {
        return finnOgOversett(Avtalekoblingsperiode.class).collect(toSet());
    }

    /**
     * Lokaliserer alle stillingsendringar tilknytta det angitte stillingsforholdet og periodiserer
     * stillingsforholdet basert på dette.
     * <p>
     * Dersom det ikkje eksisterer nokon stillingsendringar tilknytta stillingsforhold blir dette ignorert
     * sidan det høgst sannsynlig då vil eksisterer ei medregning tilknytta stillingsforholdet og den
     * forventast handtert av den som kallar denne metoda.
     *
     * @param stillingsforhold stillingsforholdet som skal forsøkast periodisert basert på stillingsendringar
     * @return alle stillingsforholdperioder generert for stillingsforholdet, eller ingenting dersom det ikkje
     * eksisterer nokon stillingsendringar som er tilknytta stillingsforholdet
     */
    private Optional<StillingsforholdPerioder> periodiserHistorikk(final StillingsforholdId stillingsforhold) {
        final PeriodiserStillingshistorikk algoritme = new PeriodiserStillingshistorikk();
        algoritme.addEndring(
                finnOgOversett(Stillingsendring.class)
                        .filter(e -> e.tilhoerer(stillingsforhold))

        );
        return algoritme
                .periodiser()
                .map(list -> new StillingsforholdPerioder(stillingsforhold, list));
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
    private <T> Stream<T> finnOgOversett(final Class<T> type) {
        final MedlemsdataOversetter<T> oversetter = lookup(type).orElse(new NullOversetter<T>());
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett);
    }

    @SuppressWarnings("unchecked")
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
