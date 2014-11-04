package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata}.
 *
 * @author Tarjei Skorgenes
 */
public class MedlemsdataTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at endringar av type 1 er dei einaste som blir forsøkt konvertert til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode}.
     */
    @Test
    public void skalKonvertereType1TilAvtalekobling() {
        List<List<String>> endringar = asList(
                asList(
                        "1"
                ),
                asList(
                        "0"
                ),
                asList(
                        "0"
                ),
                asList(
                        "0"
                )
        );
        final Medlemsdata data = new Medlemsdata(endringar);
        assertThat(data.alleAvtalekoblingsperioder()).as("alle avtalekoblingsperioder i " + data).hasSize(1);
    }

    /**
     * Verifiserer at endringar av type 0 er dei einaste som blir forsøkt konvertert til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring}.
     */
    @Test
    public void skalKonvertereType0TilStillingsendring() {
        List<List<String>> endringar = asList(
                asList(
                        "1"
                ),
                asList(
                        "0"
                ),
                asList(
                        "0"
                ),
                asList(
                        "0"
                ),
                asList(
                        "2"
                )
        );
        final Medlemsdata data = new Medlemsdata(endringar);
        assertThat(data.alleStillingsendringar()).as("alle stillingendringar i " + data).hasSize(3);
    }

    /**
     * Verifiserer at det ikkje er tillatt å opprette medlemsdata for eit medlem som vi ikkje har noko informasjon
     * på.
     */
    @Test
    public void skalIkkjeTillateMedlemsdataUtanInnhold() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("medlemsdata må inneholde minst ei stillingsendring, medregning eller avtalekobling, men var tom");

        new Medlemsdata(new ArrayList<>());
    }

    /**
     * Verifiserer at det ikkje er tillatt å konstruere nye medlemsdata der data-parameteret er null ettersom det vil
     * føre til NullPointerException ved seinare prosessering.
     */
    @Test
    public void skalIkkjeTillateDataLikNullVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("medlemsdata er påkrevd, men var null");
        new Medlemsdata(null);
    }
}
