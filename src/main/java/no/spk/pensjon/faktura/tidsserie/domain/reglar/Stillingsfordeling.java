package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

/**
 * <p>Strategi for � begrense total stillingsprosent til maksimalt 100% for parallelle stillingsforhold.</p>
 *
 * <p>Stillinger som legges til</p>
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsfordeling {
    private static final Prosent LIMIT = new Prosent("100%");

    private Map<StillingsforholdId, Prosent> andelar = new HashMap<>();

    /**
     * Gir andelen i prosent et stillingforhold utgj�r i stillingsfordelingen.
     * @param stilling som stillingsfordelingen skal finne andelen for
     * @return Valgfi prosent-verdi som representerer stillingsfoholdets andel i stillingsfordelingen.
     */
    public Optional<Prosent> andelFor(final StillingsforholdId stilling) {
        return Optional.ofNullable(andelar.get(stilling));
    }

    /**
     * Legger til en aktiv stilling til stillingsfordelingen. Dersom stillingen f�rer til at total stillingsprosent for alle
     * stillinger som er lagt til stillingsfordelingen overstiger 100%, blir stillingsprosenten til stillingen avkortet slik
     * at den totale stillingsprosenten ikke overstiger 100%. Dvs. at dersom total stillingsprosent for
     * stillingsfordelingen allerede er 100%, vil alle stillinger senere legges til utgj�re 0% av stillingsfordelingen.
     *
     * <p>Rekkef�lgen stillinger bli lagt til stillingsfordelingen er avgj�rende for hvilken andel en stilling vil f�
     * i den endelige stillingsfordelingen.</p>
     *
     * @param stilling Stillingen som skal legges til stillingsfordelingen.
     * Stillingsprosenten avkortes dersom total stillingsprosent for alle stillingsforhold som er lagt til stillingsfordelingen
     * overstiger 100%.
     * @return Stillingsfordelingen for chaining.
     */
    public Stillingsfordeling leggTil(final AktivStilling stilling) {
        andelar.put(stilling.stillingsforhold(), beregnNyAndel(stilling));
        return this;
    }

    private Prosent beregnNyAndel(final AktivStilling stilling) {
        Prosent nyAndel = stilling
                .stillingsprosent()
                .orElse(Prosent.ZERO);
        final Prosent nyTotal = total().plus(nyAndel);
        if (nyTotal.isGreaterThan(LIMIT)) {
            nyAndel = LIMIT.minus(total());
        }
        return nyAndel;
    }

    /**
     * Denne dummy metoden eksisterer for � kunne tilfredstille signaturen for reduce fra en str�m med aktive stillinger.
     * Blir kun kalt ved parallellprossersering av st�rm, og det st�ttes ikke/brukes ikke.
     * @throws UnsupportedOperationException
     */
    public Stillingsfordeling kombinerIkkeStoettet(final Stillingsfordeling other) {
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
