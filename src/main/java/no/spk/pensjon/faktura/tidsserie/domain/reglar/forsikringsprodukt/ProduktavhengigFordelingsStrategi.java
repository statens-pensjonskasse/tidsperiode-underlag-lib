package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVKORTET;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ER_MEDREGNING;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ER_PERMISJON_UTEN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * {@link FordelingsStrategi} som angir {@link Fordelingsaarsak} for en {@link AktivStilling} basert på egenskaper ved perioden.
 * <br>
 * Når {@link #begrunnetAndelFor(AktivStilling)} er kjørt,
 * vil resultatet angi om stillingen i det hele tatt er fakturerbar. Dersom stillingen er fakturerbar (@link {@link Fordelingsaarsak#fakturerbar()})
 * må {@link Fordelingsaarsak} som skyldes sammenhenger mellom paralelle stillingsforhold avgjøres separat.
 *
 * @author Snorre E. Brekke - Computas
 */
class ProduktavhengigFordelingsStrategi implements FordelingsStrategi {
    static final Set<Produkt> LOVLIGE_PRODUKT = new HashSet<>(asList(Produkt.GRU, Produkt.YSK));

    private final Produkt produkt;
    private final Medlemsavtalar medlemsavtalar;

    private final List<Aarsaksfunksjon> ikkeFakturerbareAarsaker = asList(
            aarsak(AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT, harProdukt().negate()),
            aarsak(ER_MEDREGNING, AktivStilling::erMedregning),
            aarsak(ER_PERMISJON_UTEN_LOENN, erPermisjonUtenLoenn())
    );

    ProduktavhengigFordelingsStrategi(Produkt produkt, Medlemsavtalar medlemsavtalar) {
        this.produkt = requireNonNull(produkt, "proukt kan ikke være null");
        validerProdukt(produkt);
        this.medlemsavtalar = requireNonNull(medlemsavtalar, "medlemsavtalar kan ikke være null");
    }

    @Override
    public BegrunnetFaktureringsandel begrunnetAndelFor(AktivStilling stilling, Prosent maksimalAndel) {
        Fordelingsaarsak fordelingsaarsak = fordelingsaarsak(stilling);
        boolean fakturerbar = fordelingsaarsak.fakturerbar();

        Prosent nyAndel = stilling
                .stillingsprosent()
                .filter(s -> fakturerbar)
                .orElse(Prosent.ZERO);

        if (nyAndel.isGreaterThan(maksimalAndel)) {
            fordelingsaarsak = AVKORTET;
            nyAndel = maksimalAndel;
        }

        return new BegrunnetFaktureringsandel(
                stilling.stillingsforhold(),
                nyAndel,
                fordelingsaarsak
        );
    }

    private Fordelingsaarsak fordelingsaarsak(AktivStilling stilling) {
        return ikkeFakturerbareAarsaker.stream()
                .filter(f -> f.matcher(stilling))
                .findAny()
                .map(Aarsaksfunksjon::status)
                .orElse(ORDINAER);
    }

    private static Predicate<AktivStilling> erPermisjonUtenLoenn() {
        return s -> s
                .aksjonskode()
                .filter(PERMISJON_UTAN_LOENN::equals)
                .isPresent();
    }

    private Predicate<AktivStilling> harProdukt() {
        return s -> medlemsavtalar.betalarTilSPKFor(s.stillingsforhold(), produkt);
    }

    private void validerProdukt(Produkt produkt) {
        if (!LOVLIGE_PRODUKT.contains(produkt)) {
            throw new IllegalArgumentException(
                    "Kan ikke lage faktureringsaarsak for produkt: " +
                            produkt +
                            ".\nLovlige verdier er:\n" +
                            LOVLIGE_PRODUKT.
                                    stream()
                                    .map(Produkt::kode)
                                    .collect(
                                            joining("\n")
                                    )
            );
        }
    }

    private static Aarsaksfunksjon aarsak(Fordelingsaarsak status, Predicate<AktivStilling> predikat) {
        return new Aarsaksfunksjon(status, predikat);
    }

    private static class Aarsaksfunksjon {
        Fordelingsaarsak status;
        Predicate<AktivStilling> predikat;

        Aarsaksfunksjon(Fordelingsaarsak status, Predicate<AktivStilling> predikat) {
            this.status = status;
            this.predikat = predikat;
        }

        Fordelingsaarsak status() {
            return status;
        }

        boolean matcher(AktivStilling stilling) {
            return predikat.test(stilling);
        }
    }
}
