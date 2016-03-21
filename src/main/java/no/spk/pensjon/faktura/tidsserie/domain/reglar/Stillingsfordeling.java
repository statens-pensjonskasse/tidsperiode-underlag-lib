package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVKORTET;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.BegrunnetFaktureringsandel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.FordelingsStrategi;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak;

/**
 * <p>Strategi for å begrense total stillingsprosent til maksimalt 100% for parallelle stillingsforhold.</p>
 *
 * <p>Stillinger som legges til</p>
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsfordeling {
    private static final Prosent LIMIT = new Prosent("100%");

    private Map<StillingsforholdId, BegrunnetFaktureringsandel> andelar = new HashMap<>();
    private final FordelingsStrategi strategi;

    public Stillingsfordeling() {
        strategi = stilling -> ORDINAER;
    }

    public Stillingsfordeling(FordelingsStrategi strategi) {
        this.strategi = requireNonNull(strategi, "strategi kan ikke være null");
    }

    /**
     * Gir andelen i prosent et stillingforhold utgjør i stillingsfordelingen.
     * @param stilling som stillingsfordelingen skal finne andelen for
     * @return Valgfi prosent-verdi som representerer stillingsfoholdets andel i stillingsfordelingen.
     */
    public Optional<Prosent> andelFor(final StillingsforholdId stilling) {
        return ofNullable(andelar.get(stilling)).map(FaktureringsandelStatus::andel);
    }

    /**
     * Gir en {@link BegrunnetFaktureringsandel} for et stillingsforhold.
     * @param stilling som stillingsfordelingen skal finne andelen for
     * @return Valgfi begrunnet faktureringandel-verdi som representerer stillingsfoholdets andel i stillingsfordelingen,
     * og årsaken til at andelen er blitt som den er.
     */
    public Optional<BegrunnetFaktureringsandel> begrunnetAndelFor(final StillingsforholdId stilling) {
        return ofNullable(andelar.get(stilling));
    }

    /**
     * Legger til en aktiv stilling til stillingsfordelingen. Dersom stillingen fører til at total stillingsprosent for alle
     * stillinger som er lagt til stillingsfordelingen overstiger 100%, blir stillingsprosenten til stillingen avkortet slik
     * at den totale stillingsprosenten ikke overstiger 100%. Dvs. at dersom total stillingsprosent for
     * stillingsfordelingen allerede er 100%, vil alle stillinger senere legges til utgjøre 0% av stillingsfordelingen.
     *
     * <p>Rekkefølgen stillinger bli lagt til stillingsfordelingen er avgjørende for hvilken andel en stilling vil få
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

    private BegrunnetFaktureringsandel beregnNyAndel(final AktivStilling stilling) {
        Fordelingsaarsak aarsak = strategi.klassifiser(stilling);
        boolean fakturerbar = aarsak.fakturerbar();

        Prosent nyAndel = stilling
                .stillingsprosent()
                .filter(s -> fakturerbar)
                .orElse(Prosent.ZERO);
        final Prosent nyTotal = total().plus(nyAndel);

        if (nyTotal.isGreaterThan(LIMIT)) {
            aarsak = AVKORTET;
            nyAndel = LIMIT.minus(total());
        }

        return new BegrunnetFaktureringsandel(
                stilling.stillingsforhold(),
                nyAndel,
                aarsak
        );
    }

    /**
     * Denne dummy metoden eksisterer for å kunne tilfredstille signaturen for reduce fra en strøm med aktive stillinger.
     * Blir kun kalt ved parallellprossersering av størm, og det støttes ikke/brukes ikke.
     * @param other Stillingsfordelinger kan ikke brukes i en parallell strøm
     * @return Kaster alltid UnsupportedOperationException
     * @throws UnsupportedOperationException Stillingsfordelinger kan ikke brukes i en parallell strøm
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
                .map(FaktureringsandelStatus::andel)
                .reduce(
                        Prosent.ZERO,
                        Prosent::plus
                );
    }
}
