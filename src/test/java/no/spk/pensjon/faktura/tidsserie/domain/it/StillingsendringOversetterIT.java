package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.domain.it.CsvFileReader.readFromClasspath;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsendringOversetterIT {
    private List<List<String>> data;
    private StillingsendringOversetter oversetter;

    @Before
    public void _before() throws IOException {
        final String ressurs = "/csv/medlem-1-stillingsforhold-3.csv";
        data = readFromClasspath(ressurs);
        assertThat(data).as("medlemsdata frå CSV-fil " + ressurs).isNotEmpty();

        oversetter = new StillingsendringOversetter();
    }

    /**
     * Verifiserer at oversettinga hentar stillingsforholdnummer frå kolonne nr 4 / index 3.
     */
    @Test
    public void skalHenteUtStillingsforholdNummerFraKolonne4() {
        final List<StillingsforholdId> actual = stillingsendringar()
                .map(Stillingsendring::stillingsforhold)
                .distinct()
                .collect(toList());
        assertThat(actual).containsOnlyElementsOf(
                lesFraKolonne(3, StillingsforholdId::valueOf)
                        .distinct()
                        .collect(toList())
        );
    }

    /**
     * Verifiserer at oversettinga hentar aksjonsdato frå kolonne nr 15 / index 14.
     */
    @Test
    public void skalHenteUtAksjonsdatoFraKolonne15() {
        final List<LocalDate> actual = stillingsendringar()
                .map(Stillingsendring::aksjonsdato)
                .distinct()
                .collect(toList());
        assertThat(actual).containsOnlyElementsOf(
                lesFraKolonne(14, e -> dato(e))
                        .distinct()
                        .collect(toList())
        );
    }

    private Stream<Stillingsendring> stillingsendringar() {
        return kunStillingsendringar()
                .map(oversetter::oversett);
    }

    private <T> Stream<T> lesFraKolonne(final int index, final Function<String, T> mapper) {
        return kunStillingsendringar()
                .map(e -> e.get(index))
                .map(mapper);
    }

    private Stream<List<String>> kunStillingsendringar() {
        return data
                .stream()
                .filter(oversetter::supports);
    }
}