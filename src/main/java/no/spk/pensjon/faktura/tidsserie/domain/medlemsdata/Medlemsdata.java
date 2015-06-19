package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata} representerer all
 * informasjon tilknytta eit bestemt medlem, som er p�krevd for � st�tte premie- og l�nnsberegning for medlemet.
 * <p>
 * Dei tre prim�re datatypene som er p�krevd for dette er stillingsendringar fr� stillingshistorikken,
 * medregningar og avtalekoblingar for alle stillingsforholda tilknytta medlemmet og som ein skal gjere beregningar
 * for.
 * </p>
 * <p>
 * Den medlemsspesifikke informasjonen som objektet inneheld blir internt og eksternt handtert som ei samling med tekstverdiar,
 * organisert som rader og kolonner i form av ei liste som inneheld lister som igjen inneheld strengar.
 * Denne representaasjonen er valgt for � holde serialiseringa enkel og forenkle handtering av stillingshistorikk,
 * medregning og avtalekoblingar utan � m�tte lage spesialisert serialisering for kvar og ein av desse.
 * </p>
 * <p>
 * Kontrakta for korleis kvar rad m� sj� ut, varierer mellom dei 3 forskjellige datatypene. Men 4 felt er likevel
 * p�krevd for at ting skal fungere som �nska:
 * </p>
 * <ol>
 * <li>Typeindikator</li>
 * <li>F�dselsdato</li>
 * <li>Personnummer</li>
 * <li>Stillingsforholdnummer</li>
 * </ol>
 * <h3>Stillingshistorikk</h3>
 * <p>
 * Informasjon henta fr� stillingshistorikken blir mappa om og representert som
 * {@link Stillingsendring}. Dette blir automatisk utf�rt for alle
 * medlemsdata som har verdien <code>0</code> som sin typeindikator.
 * </p>
 * <p>
 * Ansvaret for handtering og mapping fr� r�format til {@link Stillingsendring} er klientane
 * sitt ansvar. Sj� dei konkrete implementasjonane av
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.MedlemsdataOversetter} for informasjon
 * om kontrakta som input-datane m� oppfylle.
 * </p>
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsdata {
    /**
     * Indexen stillingsforholdnummeret blir henta fr� i radene som utgjer medlemsdatane.
     */
    private static final int INDEX_STILLINGSFORHOLD_ID = 3;

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
     * @throws NullPointerException     viss noen av parameterverdiene er <code>null</code>
     * @throws IllegalArgumentException viss <code>medlemsdata</code> ikkje inneheld noko informasjon og er tom
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
     * Listar ut alle avtalekoblingar tilknytta medlemmet sine stillingsforhold, som matchar
     * det angitte s�kekriteriet.
     *
     * @param predikat eit predikat som indikerer kva avtalekoblingar som skal returnerast
     * @return ein straum med alle avtalekoblingane som tilh�yrer medlemmet sine stillingsforhold og som ikkje
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
        // Dersom vi endrar designet til � ogs� ta inn data som ikkje er kobla mot stillingsforhold m� denne straumen
        // filtrerast til kun � inneholde data tilknytta stillingsforhold
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
     * For kvart stillingsforhold blir alle tilknytta stillingsendringar henta ut og brukt for � bygge opp
     * ein ny instans av {@link StillingsforholdPerioder} for kvart
     * stillingsforhold.
     * <p>
     * For stillingsforhold som ikkje har nokon stillingsendringar men som har medregning vil det forel�pig ikkje blir
     * generert nokon periodisering basert p� dette.
     *
     * @return ein straum som inneheld alle stillingsforholdperioder for alle stillingsforhold som vi har stillingsendringar for
     * @see #allePeriodiserbareStillingsforhold()
     */
    public Stream<StillingsforholdPerioder> alleStillingsforholdPerioder() {
        return allePeriodiserbareStillingsforhold()
                .map(this::periodiserStillingsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Periodiserer stillingsendringar eller medregningar tilknytta eit bestemt stillingsforhold.
     *
     * @param stillingsforhold stillingsforholdet som skal periodiserast
     * @return ein periodisert representasjon av stillingsforholdet viss det er tilknytta stillingsendringar eller medregning,
     * eller {@link java.util.Optional#empty()} dersom det ikkje eksisterer nokon informasjon tilknytta stillingsforholdet
     * @throws IllegalStateException viss det eksisterer b�de historikk og medregningar som tilh�yrer stillingsforholdet
     */
    private Optional<StillingsforholdPerioder> periodiserStillingsforhold(final StillingsforholdId stillingsforhold) {
        final Optional<StillingsforholdPerioder> historikk = periodiserHistorikk(stillingsforhold);
        final Optional<StillingsforholdPerioder> medregning = periodiserMedregning(stillingsforhold);

        if (medregning.isPresent() && historikk.isPresent()) {
            throw new IllegalStateException(
                    stillingsforhold + " kan enten vere tilknytta stillingshistorikk eller medregning, " +
                            "ikkje begge deler"
            );
        }
        if (medregning.isPresent()) {
            return medregning;
        }
        return historikk;
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
     * Hentar ut alle medregningsperioder tilknytta medlemmet.
     *
     * @return alle medregningsperioder for medlemmet
     */
    Iterable<Medregningsperiode> alleMedregningsperioder() {
        return finnOgOversett(Medregningsperiode.class).collect(toSet());
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
     * Lokaliserer alle medregningsperioder tilknytta det angitte stillingsforholdet og genererer ei ny samling
     * stillingsforholdperioder for kvar av desse.
     * <p>
     * Periodene som blir returnert blir sortert i henhold til fra og med dato slik at dei kjem i kronologisk
     * rekkef�lge p� samme m�te som perioder generert basert p� stillingshistorikk gjer.
     *
     * @param stillingsforhold stillingsforholdet som skal fors�kast periodisert basert p� medregning
     * @return alle stillingsforholdperioder generert for stillingsforholdet basert p� medregning, eller ingenting
     * dersom stillingsforholdet ikkje har nokon medregningar
     */
    private Optional<StillingsforholdPerioder> periodiserMedregning(final StillingsforholdId stillingsforhold) {
        final PeriodiserMedregning algoritme = new PeriodiserMedregning();
        algoritme.addMedregning(
                finnOgOversett(Medregningsperiode.class)
                        .filter(e -> e.tilhoerer(stillingsforhold))
        );
        return algoritme
                .periodiser()
                .map(list -> new StillingsforholdPerioder(stillingsforhold, list));
    }

    /**
     * Lokaliserer alle stillingsendringar tilknytta det angitte stillingsforholdet og periodiserer
     * stillingsforholdet basert p� dette.
     * <p>
     * Dersom det ikkje eksisterer nokon stillingsendringar tilknytta stillingsforhold blir dette ignorert
     * sidan det h�gst sannsynlig d� vil eksisterer ei medregning tilknytta stillingsforholdet og den
     * forventast handtert av den som kallar denne metoda.
     *
     * @param stillingsforhold stillingsforholdet som skal fors�kast periodisert basert p� stillingsendringar
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

    /**
     * Bygger opp medlemsperioder basert p� alle medlemmets stillingsendringar og medregningar.
     * <p>
     * Avtalekoblingar blir utelatt sidan dei prim�rt har med avtalen og medlemmets stillingsforhold � gjere, ikkje
     * tilstanda til sj�lve medlemmet.
     *
     * @return alle medlemmet sine medlemsperioder, eller ingenting om medlemmet kun har avtalekoblingar
     * utan nokon stillingsendringar eller medregningar
     */
    public Optional<Medlemsperioder> periodiser() {
        return new PeriodiserMedlem()
                .addStillingsforholdperioder(
                        alleStillingsforholdPerioder()
                                .flatMap(StillingsforholdPerioder::stream)
                )
                .periodiser()
                .map(Medlemsperioder::new);
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
