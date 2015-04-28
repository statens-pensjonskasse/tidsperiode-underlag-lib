package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

public class Stillingsfordeling {
    private static final Prosent LIMIT = new Prosent("100%");

    private Map<StillingsforholdId, Prosent> andelar = new HashMap<>();

    public Optional<Prosent> andelFor(final StillingsforholdId stilling) {
        return Optional.ofNullable(andelar.get(stilling));
    }

    public Stillingsfordeling leggTil(final AktivStilling stilling) {
        andelar.put(stilling.stillingsforhold(), beregnNyAndel(stilling));
        return this;
    }

    public Prosent beregnNyAndel(final AktivStilling stilling) {
        Prosent nyAndel = stilling
                .stillingsprosent()
                .orElse(Prosent.ZERO);
        final Prosent nyTotal = total().plus(nyAndel);
        if (nyTotal.isGreaterThan(LIMIT)) {
            nyAndel = LIMIT.minus(total());
        }
        return nyAndel;
    }

    public Stillingsfordeling kombiner(final Stillingsfordeling other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "sum = " + total()
                + "\nStillingar:\n"
                +
                andelar
                        .entrySet()
                        .stream()
                        .map(e -> "- " + e.getKey() + " => " + e.getValue())
                        .collect(joining("\n"));
    }

    void clear() {
        andelar.clear();
    }

    private Prosent total() {
        return andelar
                .values()
                .stream()
                .reduce(
                        Prosent.ZERO,
                        Prosent::plus
                );
    }
}
