package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Sats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;

import org.assertj.core.api.AbstractObjectAssert;

public class Assertions {
    public static AbstractObjectAssert<?, Optional<String>> assertArbeidsgiverbeloep(final Avtaleprodukt produkt) {
        final Optional<? extends Satser<?>> premiesatser = of(produkt.premiesatser());
        return assertThat(premiesatser.map(satser -> satser.arbeidsgiverpremie()).map(Object::toString))
                .as("premiebeløp for arbeidsgiver fra " + produkt);
    }

    public static AbstractObjectAssert<?, Optional<String>> assertMedlemsbeloep(final Avtaleprodukt produkt) {
        return assertThat(of(produkt.premiesatser()).map(satser -> satser.medlemspremie()).map(Object::toString))
                .as("premiebeløp for medlem fra " + produkt);
    }

    public static AbstractObjectAssert<?, Optional<String>> assertAdministrasjonsgebyrbeloep(final Avtaleprodukt produkt) {
        return assertThat(of(produkt.premiesatser()).map(satser -> satser.administrasjonsgebyr()).map(Object::toString))
                .as("premiebeløp for administrasjonsgebyr fra " + produkt);
    }
}
