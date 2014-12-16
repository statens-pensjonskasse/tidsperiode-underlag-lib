package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.Datoar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Medregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedregningsOversetter;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.MedregningsOversetter}.
 *
 * @author Tarjei Skorgenes
 */
public class MedregningsOversetterIT {
    @ClassRule
    public static EksempelDataForMedlem data = new EksempelDataForMedlem();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final MedregningsOversetter oversetter = new MedregningsOversetter();

    @Test
    public void skalFeileMedEinGodBeskrivelseAvFeilenDersomAntallKolonnerErMindreEnn8() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Ei medregningsperiode må inneholde følgjande kolonner i angitt rekkefølge");
        e.expectMessage(
                "typeindikator, fødselsdato, personnummer, stillingsforhold, frå og med-dato, til og med-dato, " +
                        "medregningskode og lønn"
        );
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
                transform(oversetter::oversett, Medregningsperiode::stillingsforhold)
        ).as("stillingsforhold frå medregningsperioda")
                .containsOnlyElementsOf(
                        transform(rad -> rad.get(3), StillingsforholdId::valueOf)
                );
    }

    /**
     * Verifiserer at oversettinga hentar frå og med-dato frå kolonne nr 5 / index 4.
     */
    @Test
    public void skalHenteUtAksjonskodeFraKolonne5() {
        assertThat(
                transform(oversetter::oversett, Medregningsperiode::fraOgMed)
        ).as("aksjonskoder frå medregningsperioda")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(4), Datoar::dato)
                );
    }

    /**
     * Verifiserer at oversettinga hentar til og med-dato frå kolonne nr 6 / index 5.
     */
    @Test
    public void skalHenteUtStillingsprosentFraKolonne6() {
        assertThat(
                transform(oversetter::oversett, Medregningsperiode::tilOgMed)
        ).as("stillingsprosent frå medregningsperioda")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(5), text -> valgfri(text).map(Datoar::dato))
                );
    }

    /**
     * Verifiserer at oversettinga hentar medregningskode frå kolonne nr 7 / index 6.
     */
    @Test
    public void skalHenteUtLoennstrinnFraKolonne7() {
        assertThat(
                transform(oversetter::oversett, Medregningsperiode::kode)
        ).as("medregningskode frå medregningsperioda")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(6), this::tilKode)
                );
    }

    /**
     * Verifiserer at oversettinga hentar medregningas lønn frå kolonne nr 8 / index 7.
     */
    @Test
    public void skalHenteUtLoennFraKolonne8() {
        assertThat(
                transform(oversetter::oversett, Medregningsperiode::beloep)
        ).as("lønn frå medregningsperioda")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(7), this::tilBeloep)
                );
    }

    private Medregningskode tilKode(final String text) {
        return valgfri(text).map(Medregningskode::valueOf).get();
    }

    private Medregning tilBeloep(final String text) {
        return valgfri(text).map(Long::valueOf).map(Kroner::new).map(Medregning::new).get();
    }

    private Optional<String> valgfri(String text) {
        return ofNullable(text)
                .filter(t -> !t.trim().isEmpty());
    }

    private <T, R> List<R> transform(final Function<List<String>, T> mapper,
                                     final Function<T, R> transformasjon) {
        return medregningsperioder()
                .map(mapper)
                .map(transformasjon)
                .collect(toList());
    }

    private Stream<List<String>> medregningsperioder() {
        return data
                .stream()
                .distinct()
                .filter(oversetter::supports);
    }
}
