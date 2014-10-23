package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.PeriodiserStillingshistorikk} representerer
 * algoritmen for og stillingsendringene som skal periodiseres og danne nye {@link StillingsforholdPeriode}.
 *
 * @author Tarjei Skorgenes
 */
public class PeriodiserStillingshistorikk {
    private final ArrayList<Stillingsendring> endringer = new ArrayList<>();

    /**
     * Legger til stillingsendringer som skal benyttes av {@link #periodiser()} når den periodiserer
     * og konstruerer nye {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode perioder}.
     *
     * @param endringer stillingsendringer som skal periodiseres
     * @return <code>this</code>
     */
    public PeriodiserStillingshistorikk addEndring(final Iterable<Stillingsendring> endringer) {
        endringer.forEach(e -> this.endringer.add(e));
        return this;
    }

    /**
     * TODO: What is this shit.
     *
     * @return
     */
    public List<StillingsforholdPeriode> periodiser() {
        if (endringer.isEmpty()) {
            throw new IllegalStateException("Periodisering av stillingshistorikk kan ikkje bli utført " +
                    "med mindre stillingsforholdet har minst ei usletta endring i stillingshistorikken"
            );
        }

        final Map<LocalDate, List<Stillingsendring>> endringerPrAksjonsdato = endringer
                .stream()
                .collect(
                        groupingBy(e -> e.aksjonsdato())
                );
        final LocalDate min = endringerPrAksjonsdato
                .keySet()
                .stream()
                .min(LocalDate::compareTo)
                .get();

        final LocalDate max = endringerPrAksjonsdato
                .keySet()
                .stream()
                .max(LocalDate::compareTo)
                .get();

        final Optional<LocalDate> tilOgMed = endringerPrAksjonsdato
                .get(max)
                .stream()
                .filter(Stillingsendring::erSluttmelding)
                .findAny()
                .map(Stillingsendring::aksjonsdato);

        LocalDate fom = min;
        Predicate<LocalDate> fjernYtterGrenser = d -> !(d.isEqual(min) || (tilOgMed.isPresent() && d.isEqual(tilOgMed.get())));

        final ArrayList<StillingsforholdPeriode> perioder = new ArrayList<>();
        for (final LocalDate nesteEndring : endringerPrAksjonsdato
                .keySet()
                .stream()
                .filter(fjernYtterGrenser)
                .sorted(LocalDate::compareTo)
                .collect(toList())
                ) {
            perioder.add(new StillingsforholdPeriode(fom, of(nesteEndring.minusDays(1))));
            fom = nesteEndring;
        }

        perioder.add(new StillingsforholdPeriode(fom, tilOgMed));

        perioder.forEach(p -> {
            endringer.forEach(e -> {
                if (p.overlapper(e.aksjonsdato())) {
                    p.kobleTil(e);
                }
            });
        });
        return perioder;
    }
}
