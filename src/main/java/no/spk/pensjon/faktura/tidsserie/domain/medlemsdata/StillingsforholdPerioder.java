package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * {@link StillingsforholdPerioder} representerer ei periodisering
 * av alle stillingsendringar eller medregningar tilknytta eit bestemt stillingsforhold.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPerioder {
    private final ArrayList<StillingsforholdPeriode> perioder = new ArrayList<>();
    private final StillingsforholdId stillingsforhold;

    /**
     * Konstruerer ei ny periodisering av eit bestemt stillingsforhold.
     *
     * @param stillingsforhold stillingsforholdet som har blitt periodisert
     * @param perioder         periodene som periodiseringa har generert for stillingsforholdet
     * @throws java.lang.NullPointerException viss nokon av parametera er <code>null</code>
     */
    public StillingsforholdPerioder(final StillingsforholdId stillingsforhold, final List<StillingsforholdPeriode> perioder) {
        requireNonNull(stillingsforhold, () -> "stillingsforhold er påkrevd, men var null");
        requireNonNull(perioder, () -> "perioder er påkrevd, men var null");

        this.stillingsforhold = stillingsforhold;
        perioder.stream().collect(() -> this.perioder, List::add, List::addAll);
    }

    /**
     * Tilhøyrer avtalekobling dette stillingsforholdet?
     *
     * @param avtalekobling avtalekoblinga som skal sjekkast
     * @return <code>true</code> dersom avtalekoblinga tilhøyrer samme stillingsforhold som oss,
     * <code>false</code> viss avtalekoblinga tilhøyrer eit anna stillingsforhold
     */
    public boolean tilhoeyrer(final Avtalekoblingsperiode avtalekobling) {
        return avtalekobling.tilhoeyrer(stillingsforhold);
    }

    /**
     * Stillingsforholdsnummeret som unikt identifiserer og skiller stillingsforholdet frå alle andre stillingsforhold.
     *
     * @return stillingsforholdnummeret
     */
    public StillingsforholdId id() {
        return stillingsforhold;
    }

    /**
     * Returnerer stillingsforholdsperiodene som periodiseringa har generert.
     *
     * @return alle stillingsforholdperiodene for stillingsforholdet
     */
    public Iterable<StillingsforholdPeriode> perioder() {
        return unmodifiableList(perioder);
    }

    /**
     * Genererer ein utfyllande, tekstlig beskrivelse av stillingsforholdsperiodene.
     *
     * @return ein tekstlig representasjon av alle stillingsforholdperiodene som har blitt generert for stillingsforholdet
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Stillingsforholdperioder for stillingsforhold ").append(stillingsforhold).append('\n');
        perioder.forEach(p -> builder.append('\t').append("- ").append(p).append('\n'));
        return builder.toString();
    }
}
