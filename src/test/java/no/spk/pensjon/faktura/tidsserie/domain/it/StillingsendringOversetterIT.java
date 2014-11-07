package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.helpers.Tid.dato;
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

    @Before
    public void _before() {
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
     * Verifiserer at oversettinga hentar aksjonskode frå kolonne nr 5 / index 4.
     */
    @Test
    public void skalHenteUtAksjonskodeFraKolonne5() {
        final List<String> actual = stillingsendringar().map(Stillingsendring::aksjonskode).collect(toList());
        final List<String> expected = lesFraKolonne(4, e -> e).collect(toList());
        assertThat(actual).as("aksjonskoder etter oversetting").containsExactlyElementsOf(expected);
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