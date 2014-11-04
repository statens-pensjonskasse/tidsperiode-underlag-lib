package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.domain.it.CsvFileReader.readFromClasspath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstest som verifiserer at {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata}
 * er i stand til å bygge opp
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode stillingsforholdperioder} ut
 * frå {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring} tilnytta medlemmet.
 *
 * @author Tarjei Skorgenes
 */
public class MedlemsdataPeriodiseringIT {
    private final HashMap<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();

    private List<List<String>> data;

    @Before
    public void _before() throws IOException {
        final String ressurs = "/csv/medlem-1-stillingsforhold-3.csv";
        this.data = readFromClasspath(ressurs);
        assertThat(data).as("medlemsdata frå CSV-fil " + ressurs).isNotEmpty();

        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());
    }

    /**
     * Verifiserer at {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata#allePeriodiserbareStillingsforhold()}
     * klarer å finne dei 3 unike stillingsforholda som medlem-1-stillingsforhold-3.csv inneheld.
     */
    @Test
    public void skalFinne3UnikeStillingsforhold() {
        final Medlemsdata medlem = new Medlemsdata(data, oversettere);
        assertThat(medlem.allePeriodiserbareStillingsforhold().collect(toList())).hasSize(3);
    }
}
