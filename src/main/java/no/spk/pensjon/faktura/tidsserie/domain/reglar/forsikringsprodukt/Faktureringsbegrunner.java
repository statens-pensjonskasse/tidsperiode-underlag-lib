package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Hjelpeklasse som angir {@link Fordelingsaarsak} for en {@link AktiveStillingar.AktivStilling} basert på egenskaper ved perioden.
 * Når {@link #fordelingsaarsakFor(AktiveStillingar.AktivStilling, Beregningsperiode)} er kjørt,
 * vil resultatet angi om stillingen i det hele tatt er fakturerbar. Dersom stillingen er fakturerbar (@link {@link Fordelingsaarsak#fakturerbar()})
 * må {@link Fordelingsaarsak} som skyldes sammenhenger mellom paralelle stillingsforhold avgjøres separat.
 *
 * @author Snorre E. Brekke - Computas
 */
class Faktureringsbegrunner {
    static final Set<Produkt> LOVLIGE_PRODUKT = new HashSet<>(asList(Produkt.GRU, Produkt.YSK));

    private final Produkt produkt;

    private final List<Aarsaksfunksjon> aarsaksfunksjoner = asList(
            funksjon(Fordelingsaarsak.AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT, harProdukt().negate()),
            funksjon(Fordelingsaarsak.ER_MEDREGNING, (p, s) -> s.erMedregning()),
            funksjon(Fordelingsaarsak.ER_PERMISJON_UTEN_LOENN, erPermisjonUtenLoenn())
    );

    Faktureringsbegrunner(Produkt produkt) {
        this.produkt = requireNonNull(produkt, "proukt kan ikke være null");
        validerProdukt(produkt);
    }


    Fordelingsaarsak fordelingsaarsakFor(AktiveStillingar.AktivStilling stilling, Beregningsperiode<?> periode) {
        return aarsaksfunksjoner.stream()
                .filter(f -> f.test(periode, stilling))
                .findAny()
                .map(Aarsaksfunksjon::status)
                .orElse(ORDINAER);
    }

    private static BiPredicate<Beregningsperiode<?>, AktiveStillingar.AktivStilling> erPermisjonUtenLoenn() {
        return (p, s) -> s
                .aksjonskode()
                .filter(PERMISJON_UTAN_LOENN::equals)
                .isPresent();
    }


    private BiPredicate<Beregningsperiode<?>, AktiveStillingar.AktivStilling> harProdukt() {
        return (p, s) -> {
            final Medlemsavtalar avtalar = p.annotasjonFor(Medlemsavtalar.class);
            return avtalar.betalarTilSPKFor(s.stillingsforhold(), produkt);
        };
    }

    private void validerProdukt(Produkt produkt) {
        if (!LOVLIGE_PRODUKT.contains(produkt)) {
            throw new IllegalArgumentException(
                    "Kan ikke lage faktureringsaarsak for produkt: " +
                            produkt +
                            " Lovlige verdier er:\n" +
                            LOVLIGE_PRODUKT.
                                    stream()
                                    .map(Produkt::kode)
                                    .collect(
                                            joining("\n")
                                    )
            );
        }
    }

    private static Aarsaksfunksjon funksjon(Fordelingsaarsak status, BiPredicate<Beregningsperiode<?>, AktiveStillingar.AktivStilling> predikat) {
        return new Aarsaksfunksjon(status, predikat);
    }

    private static class Aarsaksfunksjon {
        Fordelingsaarsak status;
        BiPredicate<Beregningsperiode<?>, AktiveStillingar.AktivStilling> predikat;

        Aarsaksfunksjon(Fordelingsaarsak status, BiPredicate<Beregningsperiode<?>, AktiveStillingar.AktivStilling> predikat) {
            this.status = status;
            this.predikat = predikat;
        }

        Fordelingsaarsak status() {
            return status;
        }

        boolean test(Beregningsperiode<?> periode, AktiveStillingar.AktivStilling stilling) {
            return predikat.test(periode, stilling);
        }
    }
}
