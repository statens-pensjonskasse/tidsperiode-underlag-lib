package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata}.
 *
 * @author Tarjei Skorgenes
 */
@SuppressWarnings("unchecked")
public class MedlemsdataTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private HashMap<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();

    private List<List<String>> medlemsdata = new ArrayList<>();

    @Before
    public void _before() {
        // Vi stubbar ut oversettinga her så vi slepp å måtte populere og sette opp eit komplett testdatasett kun for
        // å teste medlemsdata som ikkje har noko direkte med datainnholdet å gjere (ut over stillingsforholdnummer og
        // typeindikatoren)
        oversettere.put(Stillingsendring.class, new MedlemsdataOversetter<Stillingsendring>() {
            @Override
            public Stillingsendring oversett(final List<String> rad) {
                return new Stillingsendring();
            }

            @Override
            public boolean supports(List<String> rad) {
                return "0".equals(rad.get(0));
            }
        });
        oversettere.put(Avtalekoblingsperiode.class, new MedlemsdataOversetter<Avtalekoblingsperiode>() {
            @Override
            public Avtalekoblingsperiode oversett(List<String> rad) {
                return new Avtalekoblingsperiode(now(), of(now()), new StillingsforholdId(1L), new AvtaleId(1L));
            }

            @Override
            public boolean supports(List<String> rad) {
                return "1".equals(rad.get(0));
            }
        });
    }

    /**
     * Verifiserer at uthentinga av avtalekoblingar lar predikatet styre kva for nokon av avtalekoblingane som
     * skal returnerast.
     */
    @Test
    public void skalFiltrereAvtalekoblingarBasertPaaPredikat() {
        final Medlemsdata data = medMedlemsdata(
                avtalekoblingsdata(),
                stillingsendringdata(),
                avtalekoblingsdata(),
                stillingsendringdata()
        ).create();
        assertThat(data.avtalekoblingar(p -> true).collect(toList())).hasSize(2);
        assertThat(data.avtalekoblingar(p -> false).collect(toList())).hasSize(0);
    }

    /**
     * Verifiserer at endringar av type 1 er dei einaste som blir forsøkt konvertert til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode}.
     */
    @Test
    public void skalKonvertereType1TilAvtalekobling() {
        final Medlemsdata data = medMedlemsdata(
                avtalekoblingsdata(),
                stillingsendringdata(),
                stillingsendringdata(),
                avtalekoblingsdata(),
                stillingsendringdata()
        )
                .create();
        assertThat(data.alleAvtalekoblingsperioder()).as("alle avtalekoblingsperioder i " + data).hasSize(2);
    }

    /**
     * Verifiserer at endringar av type 0 er dei einaste som blir forsøkt konvertert til
     * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring}.
     */
    @Test
    public void skalKonvertereType0TilStillingsendring() {
        final Medlemsdata data = medMedlemsdata(
                avtalekoblingsdata(),
                stillingsendringdata(),
                stillingsendringdata(),
                stillingsendringdata(),
                medregningsdata()
        )
                .create();
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

        new Medlemsdata(new ArrayList<>(), oversettere);
    }

    /**
     * Verifiserer at det ikkje er tillatt å konstruere nye medlemsdata der data-parameteret er null ettersom det vil
     * føre til NullPointerException ved seinare prosessering.
     */
    @Test
    public void skalIkkjeTillateDataLikNullVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("medlemsdata er påkrevd, men var null");
        new Medlemsdata(null, oversettere);
    }

    /**
     * Verifiserer at det ikkje er tillatt å konstruere nye medlemsdata der oversettere-parameteret er null ettersom det vil
     * føre til NullPointerException ved seinare prosessering.
     */
    @Test
    public void skalIkkjeTillateOversettereLikNullVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("oversettere er påkrevd, men var null");
        new Medlemsdata(asList(stillingsendringdata(), avtalekoblingsdata()), null);
    }

    /**
     * Verifiserer at dersom medlemsdata blir satt opp utan ein oversettar for ei datatype
     * så blir det ikkje kasta nokon exception under konvertering, ein skal handterere denne
     * feilen som om det ikkje er lagt til nokon medlemsdata av den ønska datatypen.
     */
    @Test
    public void skalIgnorereManglandeOversettarVedKonvertering() {
        oversettere.remove(Stillingsendring.class);
        assertThat(
                medMedlemsdata(stillingsendringdata(), avtalekoblingsdata())
                        .create()
                        .alleStillingsendringar()
        ).isEmpty();
    }

    private MedlemsdataTest medMedlemsdata(final List<String>... medlemsdata) {
        this.medlemsdata.addAll(asList(medlemsdata));
        return this;
    }

    private Medlemsdata create() {
        return new Medlemsdata(medlemsdata, oversettere);
    }

    private List<String> avtalekoblingsdata() {
        return asList("1");
    }

    private List<String> stillingsendringdata() {
        return asList("0");
    }

    private List<String> medregningsdata() {
        return asList("2");
    }
}
