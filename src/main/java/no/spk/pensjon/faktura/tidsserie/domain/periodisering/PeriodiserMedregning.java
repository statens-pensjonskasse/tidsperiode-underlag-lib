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

public class PeriodiserMedregning {
    private final List<Medregningsperiode> perioder = new ArrayList<>();

    public Optional<List<StillingsforholdPeriode>> periodiser() {
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

    public void addMedregning(final Stream<Medregningsperiode> perioder) {
        perioder.forEach(this.perioder::add);
    }
}
