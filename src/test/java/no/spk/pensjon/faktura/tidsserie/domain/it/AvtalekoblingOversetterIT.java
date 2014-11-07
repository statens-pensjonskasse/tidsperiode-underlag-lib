package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.Datoar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.AvtalekoblingOversetter}.
 *
 * @author Tarjei Skorgenes
 */
public class AvtalekoblingOversetterIT {
    @ClassRule
    public static EksempelDataForMedlem data = new EksempelDataForMedlem();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final AvtalekoblingOversetter oversetter = new AvtalekoblingOversetter();

    /**
     * Verifiserer at oversettinga feilar dersom input-rada ikkje inneheld korrekt antall kolonner.
     */
    @Test
    public void skalFeileMedEinGodBeskrivelseAvFeilenDersomAntallKolonnerErUlik7() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Ei avtalekobling må inneholde følgjande kolonner i angitt rekkefølge");
        e.expectMessage("typeindikator, fødselsdato, personnummer, stillingsforholdnummer, startdato, sluttdato og avtalenummer");
        e.expectMessage("Rada som feila: ");
        e.expectMessage(emptyList().toString());

        oversetter.oversett(emptyList());
    }

    /**
     * Verifiserer at stillingsforholdnummeret blir henta frå kolonne 4 / index 3.
     */
    @Test
    public void skalHenteUtStillingsforholdFraKolonne4() {
        assertThat(
                transform(oversetter::oversett, Avtalekoblingsperiode::stillingsforhold)
        ).as("stillingsforholdnummer frå avtalekoblingane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(3), StillingsforholdId::valueOf)
                );
    }

    /**
     * Verifiserer at startdato blir henta frå kolonne 5 / index 4.
     */
    @Test
    public void skalHenteUtStartdatoFraKolonne5() {
        assertThat(
                transform(oversetter::oversett, Avtalekoblingsperiode::fraOgMed)
        ).as("startdatoar frå avtalekoblingane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(4), Datoar::dato)
                );
    }

    /**
     * Verifiserer at sluttdato blir henta frå kolonne 6 / index 5.
     */
    @Test
    public void skalHenteUtSluttdatoFraKolonne6() {
        assertThat(
                transform(oversetter::oversett, Avtalekoblingsperiode::tilOgMed)
        ).as("sluttdatoar frå avtalekoblingane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(5), t -> ofNullable(dato(t)))
                );
    }

    /**
     * Verifiserer at avtalenummer blir henta frå kolonne 7 / index 6.
     */
    @Test
    public void skalHenteUtAvtalenummerFraKolonne7() {
        assertThat(
                transform(oversetter::oversett, Avtalekoblingsperiode::avtale)
        ).as("avtalenummer frå avtalekoblingane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(6), AvtaleId::valueOf)
                );
    }

    private <T, R> List<R> transform(final Function<List<String>, T> mapper,
                                     final Function<T, R> transformasjon) {
        return avtalekoblingar()
                .map(mapper)
                .map(transformasjon)
                .collect(toList());
    }

    private Stream<List<String>> avtalekoblingar() {
        return data
                .stream()
                .filter(oversetter::supports);
    }
}