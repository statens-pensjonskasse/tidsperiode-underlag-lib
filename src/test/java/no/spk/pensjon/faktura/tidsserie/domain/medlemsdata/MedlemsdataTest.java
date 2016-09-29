package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.BISTILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning.SPK;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode.medregning;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata}.
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
                return new Stillingsendring().stillingsforhold(StillingsforholdId.valueOf(rad.get(3))).aksjonsdato(now());
            }

            @Override
            public boolean supports(List<String> rad) {
                return "0".equals(rad.get(0));
            }
        });
        oversettere.put(Avtalekoblingsperiode.class, new MedlemsdataOversetter<Avtalekoblingsperiode>() {
            @Override
            public Avtalekoblingsperiode oversett(List<String> rad) {
                return new Avtalekoblingsperiode(now(), of(now()), new StillingsforholdId(1L), new AvtaleId(1L), SPK);
            }

            @Override
            public boolean supports(List<String> rad) {
                return "1".equals(rad.get(0));
            }
        });
        oversettere.put(Medregningsperiode.class, new MedlemsdataOversetter<Medregningsperiode>() {
            @Override
            public Medregningsperiode oversett(final List<String> rad) {
                return medregning()
                        .fraOgMed(now())
                        .loepende()
                        .beloep(kroner(10))
                        .kode(BISTILLING)
                        .stillingsforhold(StillingsforholdId.valueOf(rad.get(3)))
                        .foedselsdato(foedselsdato(19700101))
                        .personnummer(new Personnummer(1))
                        .bygg();
            }

            @Override
            public boolean supports(List<String> rad) {
                return "2".equals(rad.get(0));
            }
        });
    }

    /**
     * Verifiserer at dersom rader frå forskjellige medlemmar blir sendt inn til samme Medlemsdata så blir det oppdaga
     * og medfører at instansieringa feilar.
     */
    @Test
    public void skalKunTillateDataTilknyttaEitMedlemPrMedlemsdata() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("medlemsdata kan kun inneholde data for eit medlem om gangen");
        e.expectMessage("1970010112345");
        e.expectMessage("1970010154321");

        medMedlemsdata(
                stillingsendringdata().foedselsdato("19700101").personnummer("12345"),
                stillingsendringdata().foedselsdato("19700101").personnummer("54321")
        )
                .create();
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
     * Verifiserer at endringar av type 2 er dei einaste som blir forsøkt konvertert til
     * {@link Medregningsperiode}.
     */
    @Test
    public void skalKonvertereType2TilMedregning() {
        final Medlemsdata data = medMedlemsdata(
                avtalekoblingsdata(),
                stillingsendringdata(),
                medregningsdata(),
                stillingsendringdata(),
                medregningsdata(),
                avtalekoblingsdata(),
                stillingsendringdata(),
                medregningsdata()
        )
                .create();
        assertThat(data.alleMedregningsperioder()).as("alle medregningsperioder i " + data).hasSize(3);
    }

    /**
     * Verifiserer at endringar av type 1 er dei einaste som blir forsøkt konvertert til
     * {@link Avtalekoblingsperiode}.
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
     * {@link Stillingsendring}.
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

        new Medlemsdata(
                asList(
                        stillingsendringdata(),
                        avtalekoblingsdata()
                )
                        .stream()
                        .map(RadBuilder::bygg)
                        .collect(toList())
                , null
        );
    }

    /**
     * Verifiserer at vi feilar dersom eit stillingsforhold har både medregning og historikk ettersom
     * dette ikkje er logisk mulig, slike tilfelle skal ha forskjellige stillingsforholdnummer.
     */
    @Test
    public void skalIkkjeTillateStillingsforholdSomHarHistorikkOgMedregning() {
        final long stillingsforhold = 4321L;
        e.expect(IllegalStateException.class);
        e.expectMessage("kan enten vere tilknytta stillingshistorikk eller medregning, ikkje begge deler");
        medMedlemsdata(
                stillingsendringdata(stillingsforhold),
                avtalekoblingsdata(stillingsforhold),
                medregningsdata(stillingsforhold)
        ).create()
                .alleStillingsforholdPerioder()
                .forEach(p -> {
                });
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

    private MedlemsdataTest medMedlemsdata(final RadBuilder... medlemsdata) {
        this.medlemsdata.addAll(asList(medlemsdata).stream().map(RadBuilder::bygg).collect(toList()));
        return this;
    }

    private Medlemsdata create() {
        return new Medlemsdata(medlemsdata, oversettere);
    }

    private RadBuilder avtalekoblingsdata() {
        return avtalekoblingsdata(1L);
    }

    private RadBuilder avtalekoblingsdata(final long stillingsforhold) {
        return rad().type("1")
                .foedselsdato("19700101")
                .personnummer("12345")
                .stillingsforhold(Long.toString(stillingsforhold))
                ;
    }

    private RadBuilder stillingsendringdata() {
        return stillingsendringdata(1L);
    }

    private RadBuilder stillingsendringdata(final long stillingsforhold) {
        return rad().type("0")
                .foedselsdato("19700101")
                .personnummer("12345")
                .stillingsforhold(Long.toString(stillingsforhold))
                ;
    }

    private RadBuilder medregningsdata() {
        return medregningsdata(1L);
    }

    private RadBuilder medregningsdata(final long stillingsforhold) {
        return rad().type("2")
                .foedselsdato("19700101")
                .personnummer("12345")
                .stillingsforhold(Long.toString(stillingsforhold))
                ;
    }

    private static RadBuilder rad() {
        return new RadBuilder();
    }

    private static class RadBuilder {
        private String type;
        private String foedselsdato;
        private String personnummer;
        private String stillingsforhold;

        public List<String> bygg() {
            return asList(type, foedselsdato, personnummer, stillingsforhold);
        }

        public RadBuilder type(final String value) {
            this.type = value;
            return this;
        }

        public RadBuilder foedselsdato(final String value) {
            this.foedselsdato = value;
            return this;
        }

        public RadBuilder personnummer(final String value) {
            this.personnummer = value;
            return this;
        }

        public RadBuilder stillingsforhold(final String value) {
            this.stillingsforhold = value;
            return this;
        }
    }
}
