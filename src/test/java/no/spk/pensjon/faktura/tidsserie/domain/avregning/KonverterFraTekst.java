package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Konvertering fra tekst til datatyper som en underlagsperiode kan annoteres med.
 */
class KonverterFraTekst {
    static Kroner beloep(final String verdi) {
        return kroner(parseInt(verdi.replaceAll("kr", "").replaceAll(" ", "")));
    }

    static AarsfaktorRegel aarsfaktorRegel(final String verdi) {
        return new AarsfaktorRegel() {
            @Override
            public Aarsfaktor beregn(final Beregningsperiode<?> periode) {
                return new Aarsfaktor(parseDouble(verdi));
            }
        };
    }

    static AarsverkRegel aarsverkRegel(final String verdi) {
        return new AarsverkRegel() {
            @Override
            public Aarsverk beregn(final Beregningsperiode<?> periode) {
                return Aarsverk.aarsverk(prosent(verdi));
            }
        };
    }

    static MaskineltGrunnlagRegel pensjonsgivendeLoenn(final String verdi) {
        return new MaskineltGrunnlagRegel() {
            @Override
            public Kroner beregn(final Beregningsperiode<?> periode) {
                return beloep(verdi);
            }
        };
    }

    public static Stillingsprosent stillingsprosent(final String verdi) {
        return trim(verdi).map(Prosent::new).map(Stillingsprosent::new).orElseThrow(() -> feilmeldingUkjentStillingsprosent(verdi));
    }

    public static Ordning ordning(final String verdi) {
        final Map<String, Ordning> ordninger = new HashMap<>();
        ordninger.put("SPK", Ordning.SPK);
        ordninger.put("APOTEK", Ordning.POA);
        ordninger.put("POA", Ordning.POA);
        ordninger.put("OPERA", Ordning.OPERA);
        return ofNullable(verdi)
                .map(String::toUpperCase)
                .map(ordninger::get)
                .orElseThrow(() -> feilmeldingUkjentOrdning(verdi, ordninger.keySet().stream()));
    }

    private static IllegalArgumentException feilmeldingUkjentStillingsprosent(final String prosent) {
        return new IllegalArgumentException(
                prosent + " er ikke formatert som en prosentverdi på formatet NN.NNN% og kunne derfor ikke konverteres til en stillingsprosent"
        );
    }

    private static IllegalArgumentException feilmeldingUkjentOrdning(final String verdi, final Stream<String> ordninger) {
        return new IllegalArgumentException(
                "Ukjent ordning: "
                        + verdi
                        + ", det er kun følgende ordninger som støttes av fastsats: "
                        + ordninger.collect(joining(","))
        );
    }

    private static Optional<String> trim(final String ordning) {
        return of(ordning).map(String::trim);
    }
}
