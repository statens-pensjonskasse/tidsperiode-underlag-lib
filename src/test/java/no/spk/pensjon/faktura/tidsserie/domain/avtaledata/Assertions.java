package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Sats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;

import org.assertj.core.api.AbstractObjectAssert;

@SuppressWarnings("rawtypes")
public class Assertions {
    public static AbstractObjectAssert<?, Optional<String>> assertArbeidsgiverbeloep(final Avtaleprodukt produkt) {
        final Optional<? extends Satser<?>> premiesatser = of(produkt.premiesatser());
        final Function<Satser, Sats> mapper = satser -> satser.arbeidsgiverpremie();
        return assertThat(premiesatser.map(mapper).map(Object::toString))
                .as("premiebeløp for arbeidsgiver fra " + produkt);
    }

    public static AbstractObjectAssert<?, Optional<String>> assertMedlemsbeloep(final Avtaleprodukt produkt) {
        final Function<Satser, Sats> mapper = satser -> satser.medlemspremie();
        return assertThat(of(produkt.premiesatser()).map(mapper).map(Object::toString))
                .as("premiebeløp for medlem fra " + produkt);
    }

    public static AbstractObjectAssert<?, Optional<String>> assertAdministrasjonsgebyrbeloep(final Avtaleprodukt produkt) {
        final Function<Satser, Sats> mapper = satser -> satser.administrasjonsgebyr();
        return assertThat(of(produkt.premiesatser()).map(mapper).map(Object::toString))
                .as("premiebeløp for administrasjonsgebyr fra " + produkt);
    }
}
