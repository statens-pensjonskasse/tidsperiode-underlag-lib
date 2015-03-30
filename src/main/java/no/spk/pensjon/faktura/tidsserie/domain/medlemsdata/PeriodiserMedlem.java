package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.LocalDate.MAX;
import static java.time.Month.JANUARY;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * {@link PeriodiserMedlem} representerer algoritma som bygger opp medlemsperioder basert på
 * alle stillingsforholdperioder tilknytta stillingsforholda til medlemmet.
 * <p>
 * Algoritma bygger vidare på periodiseringa som {@link PeriodiserStillingshistorikk} og {@link PeriodiserMedregning} har
 * utført i forkant for å bygge opp {@link StillingsforholdPeriode}r frå medlemmet sine {@link Stillingsendring}ar og
 * {@link Medregningsperiode}r.
 * <p>
 * Ei medlemsperiode er definert som ei tidsperiode der det ikkje skjer nokon tilstandsendringar på nokon av
 * medlemmet sine stillingsforhold. Algoritma bygger opp eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} basert på alle stillingsforholdperioder tilknytta
 * medlemmet. Kvar {@link Underlagsperiode} i underlaget blir konvertert til ei medlemsperiode. Kvar medlemsperiode
 * blir deretter kobla saman med alle stillingsforholdperioder som den overlappar, enten heilt eller delvis. Sidan
 * det for medlemsperioder og er mulig at medlemmet i perioder ikkje har nokon aktive stillingsforhold
 * kan det derfor bli danna medlemsperioder utan nokon stillingsforholdperioder tilknytta.
 *
 * @author Tarjei Skorgenes
 */
class PeriodiserMedlem {
    private final List<StillingsforholdPeriode> perioder = new ArrayList<>();

    PeriodiserMedlem addStillingsforholdperioder(final Stream<StillingsforholdPeriode> perioder) {
        perioder.forEach(this.perioder::add);
        return this;
    }

    Optional<Stream<Medlemsperiode>> periodiser() {
        if (perioder.isEmpty()) {
            return empty();
        }
        final Observasjonsperiode observasjonsperiode = new Observasjonsperiode(
                // SPK eksisterte ikkje før 1. januar 1917, ergo kan heller ikkje nokon vere medlem tidligare enn det
                LocalDate.of(1917, JANUARY, 1),
                // Periodiseringa legger på 1 dag seinare, ergo -1 her for å unngå
                // å wrappe rundt til latterlig, latterlig langt inn i fortida
                MAX.minusDays(1)
        );
        final Function<Underlagsperiode, Medlemsperiode> tilMedlemsperiode = p -> nyPeriode(observasjonsperiode, p);
        return of(
                new UnderlagFactory(observasjonsperiode)
                        .addPerioder(perioder)
                        .periodiser()
                        .stream()
                        .map(tilMedlemsperiode)
        );
    }

    private Medlemsperiode nyPeriode(final Observasjonsperiode observasjonsperiode, final Underlagsperiode p) {
        final Medlemsperiode medlemsperiode = new Medlemsperiode(
                p.fraOgMed(),
                p.tilOgMed().equals(
                        observasjonsperiode.tilOgMed()
                )
                        ? Optional.empty() : p.tilOgMed()
        );
        medlemsperiode.kobleTil(p.koblingarAvType(StillingsforholdPeriode.class));
        return medlemsperiode;
    }
}