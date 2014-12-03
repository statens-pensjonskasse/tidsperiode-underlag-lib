package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.Datoar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.StillingsendringOversetter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
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
        e.expectMessage("Ei stillingsendring m� inneholde f�lgjande kolonner i angitt rekkef�lge");
        e.expectMessage(
                "typeindikator, f�dselsdato, personnummer, stillingsforhold, aksjonskode, arbeidsgivar, " +
                        "permisjonsavtale, registreringsdato, l�nnstrinn, l�nn, faste tillegg, variable tillegg, " +
                        "funksjonstillegg og aksjonsdato"
        );
        e.expectMessage("Rada som feila: ");
        e.expectMessage(emptyList().toString());

        oversetter.oversett(emptyList());
    }

    /**
     * Verifiserer at oversettinga hentar stillingsforholdnummer fr� kolonne nr 4 / index 3.
     */
    @Test
    public void skalHenteUtStillingsforholdNummerFraKolonne4() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::stillingsforhold)
        ).as("stillingsforhold fr� stillingsendringane")
                .containsOnlyElementsOf(
                        transform(rad -> rad.get(3), StillingsforholdId::valueOf)
                );
    }

    /**
     * Verifiserer at oversettinga hentar aksjonskode fr� kolonne nr 5 / index 4.
     */
    @Test
    public void skalHenteUtAksjonskodeFraKolonne5() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::aksjonskode)
        ).as("aksjonskoder fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(4), s -> s)
                );
    }

    /**
     * Verifiserer at oversettinga hentar stillingsprosent fr� kolonne nr 9 / index 8.
     */
    @Test
    public void skalHenteUtStillingsprosentFraKolonne9() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::stillingsprosent)
        ).as("stillingsprosent fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(8), s -> new Stillingsprosent(new Prosent(s)))
                );
    }

    /**
     * Verifiserer at oversettinga hentar l�nnstrinn fr� kolonne nr 10 / index 9.
     */
    @Test
    public void skalHenteUtLoennstrinnFraKolonne10() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::loennstrinn)
        ).as("l�nnstrinn fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(9), this::tilLoennstrinn)
                );
    }

    /**
     * Verifiserer at oversettinga hentar innrapportert l�nn fr� kolonne nr 11 / index 10.
     */
    @Test
    public void skalHenteUtLoennFraKolonne11() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::loenn)
        ).as("l�nn fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(10), this::tilLoenn)
                );
    }

    /**
     * Verifiserer at oversettinga hentar innrapporterte faste tillegg i l�nn fr� kolonne nr 12 / index 11.
     */
    @Test
    public void skalHenteUtFasteTilleggFraaKolonne12() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::fastetillegg)
        ).as("faste tillegg fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(11), this::tilFastetillegg)
                );
    }

    /**
     * Verifiserer at oversettinga hentar innrapporterte faste tillegg i l�nn fr� kolonne nr 13 / index 12.
     */
    @Test
    public void skalHenteUtFasteTilleggFraaKolonne13() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::variabletillegg)
        ).as("variable tillegg fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(12), this::tilVariabletillegg)
                );
    }

    /**
     * Verifiserer at oversettinga hentar innrapporterte funksjonstillegg i l�nn fr� kolonne nr 14 / index 13.
     */
    @Test
    public void skalHenteUtFunksjonstilleggFraaKolonne14() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::funksjonstillegg)
        ).as("funksjonstillegg fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(13), this::tilFunksjonstillegg)
                );
    }

    /**
     * Verifiserer at oversettinga hentar aksjonsdato fr� kolonne nr 15 / index 14.
     */
    @Test
    public void skalHenteUtAksjonsdatoFraKolonne15() {
        assertThat(
                transform(oversetter::oversett, Stillingsendring::aksjonsdato)
        ).as("aksjonskoder fr� stillingsendringane")
                .containsExactlyElementsOf(
                        transform(rad -> rad.get(14), Datoar::dato)
                );
    }

    private Optional<Loennstrinn> tilLoennstrinn(final String text) {
        return valgfri(text).map(Loennstrinn::new);
    }

    private Optional<DeltidsjustertLoenn> tilLoenn(final String text) {
        return valgfri(text).map(Long::valueOf).map(Kroner::new).map(DeltidsjustertLoenn::new);
    }

    private Optional<Fastetillegg> tilFastetillegg(final String text) {
        return valgfri(text).map(Long::valueOf).filter(tall -> tall > 0).map(Kroner::new).map(Fastetillegg::new);
    }

    private Optional<Funksjonstillegg> tilFunksjonstillegg(final String text) {
        return valgfri(text).map(Long::valueOf).filter(tall -> tall > 0).map(Kroner::new).map(Funksjonstillegg::new);
    }

    private Optional<Variabletillegg> tilVariabletillegg(final String text) {
        return valgfri(text).map(Long::valueOf).filter(tall -> tall > 0).map(Kroner::new).map(Variabletillegg::new);
    }

    private Optional<String> valgfri(String text) {
        return ofNullable(text)
                .filter(t -> !t.trim().isEmpty());
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