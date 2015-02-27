package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.StatligLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StatligLoennstrinnperiodeOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjontestar for innlesing av og mapping av statlige l�nnstrinn.
 *
 * @author Tarjei Skorgenes
 */
public class StatligeLoennstrinnIT {
    @ClassRule
    public static final EksempelDataForStatligeLoennstrinn data = new EksempelDataForStatligeLoennstrinn();

    @Rule
    public final ExpectedException e = ExpectedException.none();

    private StatligLoennstrinnperiodeOversetter oversetter;

    @Before
    public void _before() {
        oversetter = new StatligLoennstrinnperiodeOversetter();
    }

    /**
     * Verifiserer at eksempeldatane ikkje inneheld nokon overlappande perioder innanfor samme l�nnstrinn.
     * <p>
     * Hovedhensikta her er � raskt oppdaga viss det snik seg inn inkonsistente data i eksempeldatane slik at ein
     * lett oppdagar det her framfor via andre integrasjonstestar der det ikkje er like lett � finne �rsak til rare
     * feil i beregningane eller periodiseringa.
     */
    @Test
    public void skalIkkjeInneholdeOverlappandePerioderInnanforSammeLoennstrinn() {
        final List<StatligLoennstrinnperiode> perioder = statligeLoennstrinn()
                .collect(toList());
        assertThat(perioder).hasSize(3325);
        final Function<Loennstrinn, Underlag> periodiser = (Loennstrinn trinn) -> {
            final UnderlagFactory factory = new UnderlagFactory(
                    new Observasjonsperiode(dato("1948.01.01"), dato("2099.12.31"))
            );
            factory.addPerioder(perioder.stream().filter((StatligLoennstrinnperiode p) -> p.harLoennFor(trinn, Optional.empty())));
            return factory.periodiser();
        };
        assertThat(
                perioder
                        .stream()
                        .map(StatligLoennstrinnperiode::trinn)
                        .distinct()
                        .map(periodiser)
                        .flatMap(Underlag::stream)
                        .filter((Underlagsperiode p) -> p.koblingarAvType(StatligLoennstrinnperiode.class).count() > 1)
                        .collect(toList())
        )
                .as("underlagsperioder kobla til to eller fleire samtidige l�nnstrinnperioder med likt l�nnstrinn")
                .isEmpty();
    }

    /**
     * Verifiserer at alle l�nnstrinnperiodene st�ttar l�nnstrinnet dei inneheld gjeldande l�nn for.
     */
    @Test
    public void skalStoetteKunSittEigetLoennstrinn() {
        assertThat(
                statligeLoennstrinn()
                        .filter(p -> !p.harLoennFor(p.trinn(), Optional.empty()))
                        .collect(toList())
        ).as("l�nnstrinnperioder som p�st�r dei ikkje har l�nn for sitt eige l�nnstrinn").isEmpty();

        statligeLoennstrinn().forEach((StatligLoennstrinnperiode p) -> {
            final IntPredicate erTrinn = p.trinn()::erTrinn;
            assertThat(
                    rangeClosed(1, 101)
                            .filter(erTrinn.negate())
                            .mapToObj(Loennstrinn::new)
                            .filter((loennstrinn) -> p.harLoennFor(loennstrinn, Optional.empty()))
                            .collect(toList())
            )
                    .as("u�nska l�nnstrinn som er st�tta av l�nnstrinnperioda " + p)
                    .isEmpty();
        });
    }

    /**
     * Verifiserer at oversetteren kun st�ttar statlige l�nnstrinn.
     */
    @Test
    public void skalKunStoetteStatligeLoennstrinn() {
        assertSupports(asList("POA_LTR")).isFalse();
        assertSupports(asList("SPK_LTR")).isTrue();
    }

    /**
     * Verifiserer at oversettinga feilar dersom nokon av radene inneheld feil antall kolonner.
     */
    @Test
    public void skalFeileHorribeltVissRaderInneheldFeilAntallKolonner() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Rada inneheldt ikkje forventa antall kolonner");
        e.expectMessage("m� inneholde f�lgjande kolonner");
        e.expectMessage("typeindikator, l�nnstrinn, fr� og med-dato, til og med-dato, bel�p");
        oversetter.oversett(asList("SPK_LTR", "1"));
    }

    /**
     * Verifiserer at oversettaren klarer � konvertere alle felt i rader som inneheld statlige l�nnstrinn
     * og mapper dei inn p� korrekt stad i {@link StatligLoennstrinnperiode}.
     */
    @Test
    public void skalOversetterRaderMedStatligeLoennstrinn() {
        assertThat(data
                        .stream()
                        .map(oversetter::oversett)
                        .collect(toList())
        ).hasSize(3325);
    }

    private Stream<StatligLoennstrinnperiode> statligeLoennstrinn() {
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett);
    }

    private AbstractBooleanAssert<?> assertSupports(List<String> rad) {
        return assertThat(oversetter.supports(rad)).as("st�tter oversetteren " + rad + "?");
    }
}
