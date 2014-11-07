package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.Datoar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsendringOversetterIT {
    private StillingsendringOversetter oversetter;

    @ClassRule
    public static EksempelDataForMedlem data = new EksempelDataForMedlem();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Before
    public void _before() {
        oversetter = new StillingsendringOversetter();
    }

    @Test
    public void skalFeileMedEinGodBeskrivelseAvFeilenDersomAntallKolonnerErUlik7() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Ei stillingsendring må inneholde følgjande kolonner i angitt rekkefølge");
        e.expectMessage("typeindikator, fødselsdato, personnummer, aksjonskode, arbeidsgivar, permisjonsavtale, registreringsdato, lønnstrinn, lønn, faste tillegg, variable tillegg, funksjonstillegg og aksjonsdato");
        e.expectMessage("Rada som feila: ");
        e.expectMessage(emptyList().toString());

        oversetter.oversett(emptyList());
    }

    /**
     * Verifiserer at oversettinga hentar stillingsforholdnummer frå kolonne nr 4 / index 3.
     */
    @Test
    public void skalHenteUtStillingsforholdNummerFraKolonne4() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::stillingsforhold)
        ).as("stillingsforhold frå stillingsendringane")
                .containsOnlyElementsOf(
                        transform(rad -> rad.get(3), StillingsforholdId::valueOf)
                );
    }

    /**
     * Verifiserer at oversettinga hentar aksjonskode frå kolonne nr 5 / index 4.
     */
    @Test
    public void skalHenteUtAksjonskodeFraKolonne5() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::aksjonskode)
        ).as("aksjonskoder frå stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(4), s -> s)
                );
    }

    /**
     * Verifiserer at oversettinga hentar aksjonsdato frå kolonne nr 15 / index 14.
     */
    @Test
    public void skalHenteUtAksjonsdatoFraKolonne15() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::aksjonsdato)
        ).as("aksjonskoder frå stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(14), Datoar::dato)
                );
    }

    private <T, R> List<R> transform(final Function<List<String>, T> mapper,
                                     final Function<T, R> transformasjon) {
        return stillingsendringar()
                .map(mapper)
                .map(transformasjon)
                .collect(toList());
    }

    private Stream<List<String>> stillingsendringar() {
        return data
                .stream()
                .distinct()
                .filter(oversetter::supports);
    }

}