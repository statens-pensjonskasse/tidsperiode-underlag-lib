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
 * {@link PeriodiserMedlem} representerer algoritma som bygger opp medlemsperioder basert p�
 * alle stillingsforholdperioder tilknytta stillingsforholda til medlemmet.
 * <p>
 * Algoritma bygger vidare p� periodiseringa som {@link PeriodiserStillingshistorikk} og {@link PeriodiserMedregning} har
 * utf�rt i forkant for � bygge opp {@link StillingsforholdPeriode}r fr� medlemmet sine {@link Stillingsendring}ar og
 * {@link Medregningsperiode}r.
 * <p>
 * Ei medlemsperiode er definert som ei tidsperiode der det ikkje skjer nokon tilstandsendringar p� nokon av
 * medlemmet sine stillingsforhold. Algoritma bygger opp eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} basert p� alle stillingsforholdperioder tilknytta
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
                // SPK eksisterte ikkje f�r 1. januar 1917, ergo kan heller ikkje nokon vere medlem tidligare enn det
                LocalDate.of(1917, JANUARY, 1),
                // Periodiseringa legger p� 1 dag seinare, ergo -1 her for � unng�
                // � wrappe rundt til latterlig, latterlig langt inn i fortida
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