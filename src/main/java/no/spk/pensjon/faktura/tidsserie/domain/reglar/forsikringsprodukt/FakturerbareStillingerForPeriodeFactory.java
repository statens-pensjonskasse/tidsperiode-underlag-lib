package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * @author Snorre E. Brekke - Computas
 */
class FakturerbareStillingerForPeriodeFactory {

    private final List<FakturerbarStilling> stillinger;

    private final Faktureringsbegrunner begrunner;

    FakturerbareStillingerForPeriodeFactory(final Produkt produkt, final Beregningsperiode<?> periode) {
        requireNonNull(produkt, "produkt kan ikke være null");
        requireNonNull(periode, "periode kan ikke være null");

        begrunner = new Faktureringsbegrunner(produkt);

        stillinger = stillinger(periode).collect(toList());
    }

    Stream<FakturerbarStilling> stillinger() {
        return stillinger.stream();
    }

    Optional<FakturerbarStilling> stilling(StillingsforholdId stillingsforholdId) {
        return stillinger()
                .filter(s -> s.aktivStilling().stillingsforhold().equals(stillingsforholdId))
                .findFirst();
    }

    private Stream<FakturerbarStilling> stillinger(final Beregningsperiode<?> periode) {
        return periode
                .annotasjonFor(AktiveStillingar.class)
                .stillingar()
                .map(s -> new FakturerbarStilling(s, begrunner.fordelingsaarsakFor(s, periode)));
    }
}
