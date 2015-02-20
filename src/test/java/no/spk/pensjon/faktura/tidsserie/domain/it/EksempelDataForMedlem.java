package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import org.junit.rules.ExternalResource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem} implementerer ein testregel
 * som tar seg av innlesing av eksempeldata tilknytta eit medlem.
 * <p>
 * Regelen baserer seg på å lese inn ei CSV-fil som inneheld eit datasett som simulerer rådataformatet som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata} deserialiserer informasjon om eit medlem
 * frå.
 *
 * @author Tarjei Skorgenes
 */
class EksempelDataForMedlem extends ExternalResource {
    /**
     * {@link Stillingsforhold} inneheld ei kortfatta oversikt over stillingsforholda ein forventar skal eksistere
     * i testdatasettet som {@link EksempelDataForMedlem} leser inn.
     */
    public static enum Stillingsforhold {
        /**
         * Stillingsforhold med historikk, aktivt frå 15. august 2005 til 30. juni 2012
         */
        A(999999999999L),
        /**
         * Stillingsforhold med historikk, aktivt frå 3. september 2012.
         */
        B(888888888888L),
        /**
         * Stillingsforhold med medregning, aktivt frå 1. juli 2010 til 31. mai 2016.
         */
        C(777777777777L);

        private final StillingsforholdId id;

        Stillingsforhold(long id) {
            this.id = new StillingsforholdId(id);
        }

        public StillingsforholdId id() {
            return id;
        }
    }

    /**
     * @see no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.Stillingsforhold#A
     */
    public static StillingsforholdId STILLING_A = Stillingsforhold.A.id();

    /**
     * @see no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.Stillingsforhold#B
     */
    public static StillingsforholdId STILLING_B = Stillingsforhold.B.id();

    /**
     * @see no.spk.pensjon.faktura.tidsserie.domain.it.EksempelDataForMedlem.Stillingsforhold#C
     */
    public static StillingsforholdId STILLING_C = Stillingsforhold.C.id();

    private final String ressurs;

    private List<List<String>> data;

    /**
     * Opprettar ein ny regel som leser inn testdata ei CSV-fil på classpathen.
     */
    public EksempelDataForMedlem() {
        ressurs = "/csv/medlem-1-stillingsforhold-3.csv";
    }

    @Override
    protected void before() throws Throwable {
        data = readFromClasspath(ressurs);
        assertThat(data).as("medlemsdata frå CSV-fil " + ressurs).isNotEmpty();
    }

    /**
     * @see java.util.List#stream()
     * @see #toList()
     */
    public Stream<List<String>> stream() {
        return data.stream();
    }

    /**
     * Returnerer ein ny straum som lar ein gå gjennom innslaga i eksempeldatane.
     *
     * @return alle radene i datasettet som regelen har lest inn
     */
    public List<List<String>> toList() {
        return Collections.unmodifiableList(data);
    }

    /**
     * Leser inn ei CSV-fil navngitt av <code>resource</code> frå classpathen og konverterer den til ei liste bygd opp
     * med ei rad pr linje i fila der kvar rad er bygd opp med eit element pr kolonne i tilhøyrande rad i fila.
     * <p>
     * Innlesinga forventar at CSV-fil ikkje har nokon header, den støttar og at radene kan ha forskjellig antall kolonner.
     * <p>
     * Fila kan inneholde kommentarlinjer som startar med #-tegnet, desse vil bli ignorert av innlesinga.
     *
     * @param resource navnet på ressursen som skal lesast inn via classpathen
     * @return ei liste som inneheld ei rad pr linje i <code>resource</code>, der kvar rad er bygd opp som ei liste med eit element pr kolonne
     * @throws java.io.FileNotFoundException dersom <code>resource</code> ikkje eksisterer
     * @throws java.io.IOException           dersom ein uventa I/O-feil oppstod
     */
    private static List<List<String>> readFromClasspath(final String resource) throws IOException {
        final List<List<String>> data = new ArrayList<>();
        try (final BufferedReader reader = open(resource)) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().startsWith("#")) {
                    data.add(asList(line.split(";")));
                }
                line = reader.readLine();
            }
        }
        return data;
    }

    private static BufferedReader open(final String resource) throws IOException {
        final Optional<InputStream> open = ofNullable(EksempelDataForMedlem.class.getResourceAsStream(resource));
        return new BufferedReader(new InputStreamReader(open.orElseThrow(fileNotFound(resource))));
    }

    private static Supplier<FileNotFoundException> fileNotFound(final String resource) {
        return () -> new FileNotFoundException("Klarte ikkje åpne " + resource + ", den eksisterer ikkje på classpathen");
    }
}
