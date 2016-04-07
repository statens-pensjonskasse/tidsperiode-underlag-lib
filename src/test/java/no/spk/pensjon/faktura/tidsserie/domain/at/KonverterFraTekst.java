package no.spk.pensjon.faktura.tidsserie.domain.at;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Konvertering fra tekst til datatyper som en underlagsperiode kan annoteres med.
 */
public class KonverterFraTekst {
    public static Kroner beloep(final String verdi) {
        return kroner(parseInt(verdi.replaceAll("kr", "").replaceAll(" ", "")));
    }

    static YrkesskadefaktureringRegel yrkesskadeandel(final String verdi) {
        return new YrkesskadefaktureringRegel() {
            @Override
            public FaktureringsandelStatus beregn(final Beregningsperiode<?> periode) {
                return new FaktureringsandelStatus(StillingsforholdId.valueOf(1), prosent(verdi));
            }
        };
    }

    static GruppelivsfaktureringRegel gruppelivsandel(final String verdi) {
        return new GruppelivsfaktureringRegel() {
            @Override
            public FaktureringsandelStatus beregn(final Beregningsperiode<?> periode) {
                return new FaktureringsandelStatus(StillingsforholdId.valueOf(1), prosent(verdi));
            }
        };
    }

    private static Prosent desimalTilProsent(String verdi) {
        return new Prosent(Double.parseDouble(verdi));
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

    public static DeltidsjustertLoenn deltidsjustertLoenn(final String verdi) {
        return new DeltidsjustertLoenn(beloep(verdi));
    }

    public static LoennstrinnBeloep loennstrinnBeloep(final String verdi) {
        return new LoennstrinnBeloep(beloep(verdi));
    }

    public static Medregning medregning(final String verdi) {
        return new Medregning(beloep(verdi));
    }

    public static Grunnbeloep grunnbeloep(final String verdi) {
        return new Grunnbeloep(beloep(verdi));
    }

    public static Fastetillegg fasteTillegg(final String verdi) {
        return new Fastetillegg(beloep(verdi));
    }

    public static Variabletillegg variableTillegg(final String verdi) {
        return new Variabletillegg(beloep(verdi));
    }

    public static Funksjonstillegg funksjonsTillegg(final String verdi) {
        return new Funksjonstillegg(beloep(verdi));
    }

    public static Produktinfo produktinfo(final String verdi) {
        return new Produktinfo(Integer.parseInt(verdi));
    }

    public static MaskineltGrunnlagRegel pensjonsgivendeLoenn(final String verdi) {
        return new MaskineltGrunnlagRegel() {
            @Override
            public Kroner beregn(final Beregningsperiode<?> periode) {
                return beloep(verdi);
            }
        };
    }

    public static <T extends BeregningsRegel<Kroner>> Function<String, T> beloepRegel(Class<T> regel) {
        return verdi -> {
            final T mock = mock(regel);
            when(mock.beregn(any(Beregningsperiode.class))).thenReturn(beloep(verdi));
            return mock;
        };
    }

    public static <T extends BeregningsRegel<Boolean>> Function<String, T> booleanRegel(Class<T> regel) {
        return verdi -> {
            final T mock = mock(regel);
            when(mock.beregn(any(Beregningsperiode.class))).thenReturn(sannhetsverdi(verdi));
            return mock;
        };
    }

    public static Boolean sannhetsverdi(final String verdi) {
        final Map<String, Boolean> ordninger = new HashMap<>();
        ordninger.put("JA", true);
        ordninger.put("NEI", false);
        return ofNullable(verdi)
                .map(String::toUpperCase)
                .map(ordninger::get)
                .orElseThrow(() -> feilmeldingUkjentSannhetsverdi(verdi, ordninger.keySet().stream()));
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


    private static IllegalArgumentException feilmeldingUkjentSannhetsverdi(final String verdi, final Stream<String> sannhetsverdier) {
        return new IllegalArgumentException(
                "Ukjent sannhetsverdier: "
                        + verdi
                        + ", støtter kun følgende sannhetsverdier (ignore case): "
                        + sannhetsverdier.collect(joining(","))
        );
    }

    private static Optional<String> trim(final String ordning) {
        return of(ordning).map(String::trim);
    }
}
