package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.MedlemsdataOversetter;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperioder;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPerioder;
import no.spk.pensjon.faktura.tidsserie.storage.csv.AvtalekoblingOversetter;
import no.spk.pensjon.faktura.tidsserie.storage.csv.MedregningsOversetter;
import no.spk.pensjon.faktura.tidsserie.storage.csv.StillingsendringOversetter;
import org.assertj.core.api.AbstractIterableAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstest som verifiserer at {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata}
 * er i stand til å bygge opp
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode stillingsforholdperioder} ut
 * frå {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring} tilnytta medlemmet.
 *
 * @author Tarjei Skorgenes
 */
public class MedlemsdataPeriodiseringIT {
    private static final StillingsforholdId STILLINGSFORHOLD_A = EksempelDataForMedlem.STILLING_A;
    private static final StillingsforholdId STILLINGSFORHOLD_B = EksempelDataForMedlem.STILLING_B;
    private static final StillingsforholdId MEDREGNING_C = EksempelDataForMedlem.STILLING_C;

    private final HashMap<Class<?>, MedlemsdataOversetter<?>> oversettere = new HashMap<>();

    @ClassRule
    public static EksempelDataForMedlem data = new EksempelDataForMedlem();

    @Before
    public void _before() throws IOException {
        oversettere.put(Stillingsendring.class, new StillingsendringOversetter());
        oversettere.put(Avtalekoblingsperiode.class, new AvtalekoblingOversetter());
        oversettere.put(Medregningsperiode.class, new MedregningsOversetter());
    }

    /**
     * Verifiserer at {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata#allePeriodiserbareStillingsforhold()}
     * klarer å finne dei 3 unike stillingsforholda som medlem-1-stillingsforhold-3.csv inneheld.
     */
    @Test
    public void skalFinne3UnikeStillingsforhold() {
        final Medlemsdata medlem = create();
        assertThat(medlem.allePeriodiserbareStillingsforhold().collect(toList()))
                .hasSize(3)
                .containsOnly(STILLINGSFORHOLD_A, STILLINGSFORHOLD_B, MEDREGNING_C);
    }

    @Test
    public void skalPeriodisereMedlemBasertPAaAlleStillingsendringarOgMedregningar() {
        final Medlemsdata medlem = create();

        final Medlemsperioder perioder = medlem.periodiser().get();
        assertThat(perioder.stream().collect(toList())).hasSize(21);
    }

    /**
     * Verifiserer at vi får ut 3 stillingsforhold periodisert kun ut frå stillingsendringar tilknytta kvart enkelt
     * stillingsforhold, ikkje ei periodisering som er splitta på alle stillingsforholda sine stillingsendringar.
     */
    @Test
    public void skalPeriodiserePrStillingsforhold() {
        final Map<StillingsforholdId, List<StillingsforholdPerioder>> stillingsforholdene = create()
                .alleStillingsforholdPerioder()
                .collect(groupingBy(StillingsforholdPerioder::id));
        assertThat(stillingsforholdene).hasSize(3);

        assertPerioder(stillingsforholdene, STILLINGSFORHOLD_A).hasSize(14);
        assertPerioder(stillingsforholdene, STILLINGSFORHOLD_B).hasSize(5);
        assertPerioder(stillingsforholdene, MEDREGNING_C).hasSize(1);
    }

    private Medlemsdata create() {
        return new Medlemsdata(data.toList(), oversettere);
    }

    private static AbstractIterableAssert<?, ? extends Iterable<StillingsforholdPeriode>, StillingsforholdPeriode> assertPerioder(final Map<StillingsforholdId, List<StillingsforholdPerioder>> alleStillingsforhold, final StillingsforholdId id) {
        final List<StillingsforholdPerioder> stillingsforhold = alleStillingsforhold.get(id);
        assertThat(stillingsforhold).as("periodisering av stillingsforhold " + id).isNotNull().hasSize(1);
        return assertThat(stillingsforhold.get(0).stream().collect(toList()));
    }
}
