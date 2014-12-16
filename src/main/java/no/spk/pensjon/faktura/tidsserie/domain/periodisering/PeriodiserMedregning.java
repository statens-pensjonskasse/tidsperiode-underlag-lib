package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Medregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * {@link PeriodiserMedregning} representerer algoritma som konverterer medregningsperioder til stillingsforholdperioder.
 *
 * @author Tarjei Skorgenes
 */
class PeriodiserMedregning {
    private final List<Medregningsperiode> perioder = new ArrayList<>();

    /**
     * Bygger opp ein ny, periodisert representasjon av eit stillingsforhold som er basert p� medregning.
     * <p>
     * Medregningsperiodene som er tilknytta stillingsforholdet dannar grunnlaget for periodiseringa som blir
     * generert, dei blir brukt as is, sortert kronologisk og blir forventa � ikkje inneholde nokon tidsgap mellom
     * medregningsperiodene dersom det eksisterer meir enn ei medregningsperiode tilknytta stillingsforholdet.
     * <p>
     * Det blir ikkje foretatt noko form for validering av om desse antagelsane stemmer. Shit in -> shit out.
     *
     * @return den periodiserte representasjonen av stillingsforholdet viss det er basert p� medregning, eller
     * ingenting dersom stillingsforholdet ikkje har nokon medregningar tilknytta seg
     */
    Optional<List<StillingsforholdPeriode>> periodiser() {
        final List<StillingsforholdPeriode> resultat = perioder
                .stream()
                .sorted((a, b) -> a.fraOgMed().compareTo(b.fraOgMed()))
                .map(StillingsforholdPeriode::new)
                .collect(toList());
        if (resultat.isEmpty()) {
            return empty();
        }
        return of(resultat);
    }

    /**
     * Legger til alle medregningsperiodene som er tilknytta stillingsforholdet.
     *
     * @param perioder ein straum med alle medregningsperiodene som er tilknytta stillingsforholdet
     */
    void addMedregning(final Stream<Medregningsperiode> perioder) {
        perioder.forEach(this.perioder::add);
    }
}
