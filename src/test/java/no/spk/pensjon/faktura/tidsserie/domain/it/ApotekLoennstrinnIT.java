package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.ApotekLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.ApotekLoennstrinnperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static org.assertj.core.api.Assertions.assertThat;

public class ApotekLoennstrinnIT {
    @ClassRule
    public static final EksempelDataForApotekLoennstrinn data = new EksempelDataForApotekLoennstrinn();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private ApotekLoennstrinnperiodeOversetter oversetter;

    @Before
    public void _before() {
        oversetter = new ApotekLoennstrinnperiodeOversetter();
    }

    /**
     * Verifiserer at eksempeldatane ikkje inneheld nokon overlappande perioder innanfor samme lønnstrinn.
     * <p>
     * Hovedhensikta her er å raskt oppdaga viss det snik seg inn inkonsistente data i eksempeldatane slik at ein
     * lett oppdagar det her framfor via andre integrasjonstestar der det ikkje er like lett å finne årsak til rare
     * feil i beregningane eller periodiseringa.
     */
    @Test
    public void skalIkkjeInneholdeOverlappandePerioderInnanforSammeLoennstrinn() {
        final List<ApotekLoennstrinnperiode> perioder = alleLoennstrinn()
                .collect(toList());
        final Function<ApotekLoennstrinnId, Underlag> periodiser = (ApotekLoennstrinnId trinn) -> {
            final UnderlagFactory factory = new UnderlagFactory(
                    new Observasjonsperiode(dato("1983.05.01"), dato("2099.12.31"))
            );
            factory.addPerioder(perioder.stream().filter((ApotekLoennstrinnperiode p) ->
                            p.harLoennFor(trinn.trinn(), of(trinn.stillingskode())))
            );
            return factory.periodiser();
        };

        assertThat(
                perioder
                        .stream()
                        .map(p -> new ApotekLoennstrinnId(p.trinn(), p.stillingskode()))
                        .distinct()
                        .map(periodiser)
                        .flatMap(Underlag::stream)
                        .filter((Underlagsperiode p) -> p.koblingarAvType(ApotekLoennstrinnperiode.class).count() > 1)
                        .collect(toList())
        )
                .as("underlagsperioder kobla til to eller fleire samtidige lønnstrinnperioder med likt lønnstrinn")
                .isEmpty();
    }

    /**
     * Verifiserer at alle lønnstrinnperiodene støttar lønnstrinnet dei inneheld gjeldande lønn for.
     */
    @Test
    public void skalStoetteKunSittEigetLoennstrinn() {
        assertThat(
                alleLoennstrinn()
                        .filter(p -> !p.harLoennFor(p.trinn(), of(p.stillingskode())))
                        .collect(toList())
        ).as("lønnstrinnperioder som påstår dei ikkje har lønn for sitt eige lønnstrinn og stillingskode").isEmpty();

        alleLoennstrinn().forEach((ApotekLoennstrinnperiode p) -> {
            final IntPredicate erTrinn = p.trinn()::erTrinn;
            assertThat(
                    rangeClosed(1, 101)
                            .filter(erTrinn.negate())
                            .mapToObj(Loennstrinn::new)
                            .filter((loennstrinn) -> p.harLoennFor(loennstrinn, of(p.stillingskode())))
                            .collect(toList())
            )
                    .as("uønska lønnstrinn som er støtta av lønnstrinnperioda " + p)
                    .isEmpty();
        });
    }

    /**
     * Verifiserer at oversetteren kun støttar statlige lønnstrinn.
     */
    @Test
    public void skalKunStoetteApotekLoennstrinn() {
        assertSupports(asList("POA_LTR")).isTrue();
        assertSupports(asList("SPK_LTR")).isFalse();
    }

    /**
     * Verifiserer at oversettinga feilar dersom nokon av radene inneheld feil antall kolonner.
     */
    @Test
    public void skalFeileHorribeltVissRaderInneheldFeilAntallKolonner() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Rada inneheldt ikkje forventa antall kolonner");
        e.expectMessage("må inneholde følgjande kolonner");
        e.expectMessage("typeindikator, frå og med-dato, til og med-dato, lønnstrinn, stillingskode, beløp");
        oversetter.oversett(asList("POA_LTR", "1980.01.01"));
    }

    /**
     * Verifiserer at oversettaren klarer å konvertere alle felt i rader som inneheld lønnstrinn for apotek
     * og mapper dei inn på korrekt stad i {@link ApotekLoennstrinnperiode}.
     */
    @Test
    public void skalOversetterRaderMedStatligeLoennstrinn() {
        assertThat(data
                        .stream()
                        .map(oversetter::oversett)
                        .collect(toList())
        ).hasSize(1462);
    }

    /**
     * Verifiserer at lønnstrinnoppslaget for lønnstrinn klarer å finne lønn i 100% stilling for alle stillingskoder
     * som Apotekordninga innrapporterer lønnstrinn på.
     * <p>
     * Intensjonen er å sikre at lønnstrinnoppslaget fungerer også for andre stillingskoder med lønnstrinn enn
     * 2, 4 og 60 som er dei einaste som det eksisterer lønnstrinnperioder tilknytta.
     * <p>
     * 5. april 2011 er valgt basert på at dette ser ut til å vere siste året der både 2, 4 og 60 fekk nye lønnstrinn,
     * nyare år ser ut til å kun ha lønnstrinn for 2 og 4 og ville dermed gjere oss litt blinde for om oppslag for
     * kodene tilknytta 60  også fungerer.
     * <p>
     * Lønnstrinn 10 er valgt fordi alle dei tre gruppene skal kunne bruke dette lønnstrinnet.
     */
    @Test
    public void skalHaLoennForAlleStillingskoderSomBrukarLoennstrinnTilknyttaApotekordninga() {
        final Loennstrinn loennstrinn = new Loennstrinn(10);
        final LocalDate dato = dato("2011.04.01");
        assertGjeldendeLoennstrinnBeloep(2, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(382_980)));
        assertGjeldendeLoennstrinnBeloep(3, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(311_388)));
        assertGjeldendeLoennstrinnBeloep(4, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(311_388)));
        assertGjeldendeLoennstrinnBeloep(5, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
        assertGjeldendeLoennstrinnBeloep(60, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
        assertGjeldendeLoennstrinnBeloep(61, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
        assertGjeldendeLoennstrinnBeloep(7, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
        assertGjeldendeLoennstrinnBeloep(8, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
        assertGjeldendeLoennstrinnBeloep(9, loennstrinn, dato).isEqualTo(new LoennstrinnBeloep(kroner(255_072)));
    }

    private Stream<ApotekLoennstrinnperiode> alleLoennstrinn() {
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett);
    }

    private AbstractBooleanAssert<?> assertSupports(List<String> rad) {
        return assertThat(oversetter.supports(rad)).as("støtter oversetteren " + rad + "?");
    }

    private AbstractObjectAssert<?, LoennstrinnBeloep> assertGjeldendeLoennstrinnBeloep(
            final int kode, final Loennstrinn loennstrinn, final LocalDate dato) {
        final Stillingskode stillingskode = Stillingskode.parse(kode);
        final List<LoennstrinnBeloep> kandidatar = alleLoennstrinn()
                .filter(p -> p.fraOgMed().isEqual(dato))
                .filter(p -> p.harLoennFor(loennstrinn, ofNullable(stillingskode)))
                .map(ApotekLoennstrinnperiode::beloep)
                .collect(toList());
        assertThat(kandidatar)
                .as("lønnstrinnperiode frå og med " + dato + "for " + loennstrinn + " med " + stillingskode)
                .hasSize(1);
        return assertThat(kandidatar.get(0))
                .as("lønn i 100% stilling frå og med " + dato + " for " + loennstrinn + " med " + stillingskode);
    }

    static class ApotekLoennstrinnId {
        private final Stillingskode stillingskode;

        private final Loennstrinn trinn;

        public ApotekLoennstrinnId(final Loennstrinn trinn, final Stillingskode stillingskode) {
            this.stillingskode = stillingskode;
            this.trinn = trinn;
        }

        public Stillingskode stillingskode() {
            return stillingskode;
        }

        public Loennstrinn trinn() {
            return trinn;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trinn, stillingskode);
        }

        @Override
        public boolean equals(Object obj) {
            final ApotekLoennstrinnId other = (ApotekLoennstrinnId) obj;
            return other.trinn.equals(trinn) && other.stillingskode.equals(stillingskode);
        }
    }
}
