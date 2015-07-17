package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;

import org.assertj.core.api.AbstractObjectAssert;

@SuppressWarnings("rawtypes")
public class Assertions {
    public static AbstractObjectAssert<?, Optional<String>> assertArbeidsgiverbeloep(final Avtaleprodukt produkt) {
        return assertBeloep(produkt, Satser::arbeidsgiverpremie, "arbeidsgiver");
    }

    public static AbstractObjectAssert<?, Optional<String>> assertMedlemsbeloep(final Avtaleprodukt produkt) {
        return assertBeloep(produkt, Satser::medlemspremie, "medlem");
    }

    public static AbstractObjectAssert<?, Optional<String>> assertAdministrasjonsgebyrbeloep(final Avtaleprodukt produkt) {
        return assertBeloep(produkt, Satser::administrasjonsgebyr, "administrasjonsgebyr");
    }

    public static AbstractObjectAssert<?, Optional<String>> assertArbeidsgiverprosent(final Avtaleprodukt produkt) {
        return assertProsent(produkt, Satser::arbeidsgiverpremie, "arbeidsgiver");
    }

    public static AbstractObjectAssert<?, Optional<String>> assertMedlemsprosent(final Avtaleprodukt produkt) {
        return assertProsent(produkt, Satser::medlemspremie, "medlem");
    }

    public static AbstractObjectAssert<?, Optional<String>> assertAdministrasjonsgebyrprosent(final Avtaleprodukt produkt) {
        return assertProsent(produkt, Satser::administrasjonsgebyr, "administrasjonsgebyr");
    }

    private static AbstractObjectAssert<?, Optional<String>> assertProsent(
            final Avtaleprodukt produkt, final Function<Satser<Prosent>, Prosent> mapper, final String felt) {
        final Avtale.AvtaleBuilder builder = avtale(produkt.avtale());
        final Optional<Premiesats> premiesats = produkt.populer(builder).bygg().premiesatsFor(produkt.produkt());
        return assertThat(premiesats.flatMap(Premiesats::prosentsatser).map(mapper).map(Object::toString))
                .as("premiesats for " + felt + " fra " + premiesats);
    }

    private static AbstractObjectAssert<?, Optional<String>> assertBeloep(
            final Avtaleprodukt produkt, final Function<Satser<Kroner>, Kroner> mapper, final String felt) {
        final Avtale.AvtaleBuilder builder = avtale(produkt.avtale());
        final Optional<Premiesats> premiesats = produkt.populer(builder).bygg().premiesatsFor(produkt.produkt());
        return assertThat(premiesats.flatMap(Premiesats::beloepsatsar).map(mapper).map(Object::toString))
                .as("premiebeløp for " + felt + " fra " + premiesats);
    }
}
