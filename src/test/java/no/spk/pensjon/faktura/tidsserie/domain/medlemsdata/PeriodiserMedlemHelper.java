package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.GenerellTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import org.assertj.core.api.AbstractListAssert;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

class PeriodiserMedlemHelper {
    static List<Medlemsperiode> periodiser(final StillingsforholdPeriode... perioder) {
        return new PeriodiserMedlem()
                .addStillingsforholdperioder(asList(perioder).stream())
                .periodiser()
                .get()
                .collect(toList());
    }

    static Tidsperiode<?> periode(final String fraOgMed, final String tilOgMed) {
        return new GenerellTidsperiode(dato(fraOgMed), of(dato(tilOgMed)));
    }

    static Tidsperiode<?> periode(final String fraOgMed, final Optional<Void> løpende) {
        return new GenerellTidsperiode(dato(fraOgMed), empty());
    }

    static AbstractListAssert<?, ?, StillingsforholdPeriode> assertStillingsforholdperioder(
            final List<Medlemsperiode> perioder, final Tidsperiode<?> periode) {
        final Medlemsperiode medlemsperiode = perioder
                .stream()
                .filter(m -> new GenerellTidsperiode(m.fraOgMed(), m.tilOgMed()).equals(periode))
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError(
                                "Det eksisterer ingen medlemsperioder lik "
                                        + periode
                                        + " blant medlemsperiodene.\nMedlemsperioder:\n"
                                        + perioder.stream().map(p -> "- " + p.toString()).collect(joining("\n"))
                        )
                );
        return assertThat(medlemsperiode.stillingsforhold().collect(toList()))
                .as("medlemsperiode " + periode + " sine stillingsforholdperioder");
    }
}
