package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode.kronologiskSorteringAvTidsperioder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * {@link StillingsforholdPerioder} representerer ei periodisering
 * av alle stillingsendringar eller medregningar tilknytta eit bestemt stillingsforhold.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPerioder implements Tidsperiode<StillingsforholdPerioder> {
    private final ArrayList<StillingsforholdPeriode> perioder = new ArrayList<>();
    private final StillingsforholdId stillingsforhold;
    private final Optional<LocalDate> tilOgMed;
    private final LocalDate fraOgMed;

    /**
     * @see #StillingsforholdPerioder(StillingsforholdId, Stream)
     */
    public StillingsforholdPerioder(final StillingsforholdId stillingsforhold, final List<StillingsforholdPeriode> perioder) {
        this(stillingsforhold, requireNonNull(perioder, () -> "perioder er påkrevd, men var null").stream());
    }

    /**
     * Konstruerer ei ny periodisering av eit bestemt stillingsforhold.
     *
     * @param stillingsforhold stillingsforholdet som har blitt periodisert
     * @param perioder periodene som periodiseringa har generert for stillingsforholdet
     * @throws java.lang.NullPointerException viss nokon av parametera er <code>null</code>
     */
    public StillingsforholdPerioder(final StillingsforholdId stillingsforhold, final Stream<StillingsforholdPeriode> perioder) {
        requireNonNull(stillingsforhold, () -> "stillingsforhold er påkrevd, men var null");
        requireNonNull(perioder, () -> "perioder er påkrevd, men var null");

        this.stillingsforhold = stillingsforhold;
        perioder.collect(() -> this.perioder, ArrayList::add, ArrayList::addAll);
        assert harMinst1Periode() : /*        */ feilmeldingNårPerioderManglar();
        assert erPeriodeneKronologiskSortert() : feilmeldingNårPeriodeneIkkjeErSortert();
        this.fraOgMed = this.perioder.get(0).fraOgMed();
        this.tilOgMed = this.perioder.get(this.perioder.size() - 1).tilOgMed();
    }

    /**
     * {@inheritDoc}
     */
    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    /**
     * {@inheritDoc}
     */
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
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
     * <p>
     * Innanfor kvar periode vil stillingsforholdets tilstand vere den samme frå dag til dag, ingen endringar i
     * medregning eller historikk vil forekomme innanfor kvar periode. Alle endringar i tilstand for stillingsforholdet
     * vil medføre at ei ny periode blir danna for kvar ny tilstand.
     *
     * @return alle periodene stillingsforholdet strekker seg over
     */
    public Stream<StillingsforholdPeriode> stream() {
        return perioder.stream();
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

    private boolean harMinst1Periode() {
        return !this.perioder.isEmpty();
    }

    private boolean erPeriodeneKronologiskSortert() {
        return this.perioder.equals(
                this.perioder
                        .stream()
                        .sorted(kronologiskSorteringAvTidsperioder())
                        .collect(toList())
        );
    }

    private String feilmeldingNårPerioderManglar() {
        return "Forventa minst 1 stillingsforholdperiode, men var 0";
    }

    private String feilmeldingNårPeriodeneIkkjeErSortert() {
        return "Stillingsforholdperiodene må vere sortert i kronologisk rekkefølge, men var ikkje det.\nPerioder:\n"
                + this.perioder
                .stream()
                .map(Object::toString)
                .map(t -> "- " + t)
                .collect(joining("\n"));
    }
}
