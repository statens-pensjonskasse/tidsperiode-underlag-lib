package no.spk.pensjon.faktura.tidsserie.domain.it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

/**
 * Støtteklasse for innlesing av data frå semikolon-separerte CSV-filer.
 */
public class CsvFileReader {
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
     * @throws IOException                   dersom ein uventa I/O-feil oppstod
     */
    public static List<List<String>> readFromClasspath(final String resource) throws IOException {
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
        final Optional<InputStream> open = ofNullable(CsvFileReader.class.getResourceAsStream(resource));
        return new BufferedReader(new InputStreamReader(open.orElseThrow(() -> new FileNotFoundException("Klarte ikkje åpne " + resource + ", den eksisterer ikkje på classpathen"))));
    }
}
