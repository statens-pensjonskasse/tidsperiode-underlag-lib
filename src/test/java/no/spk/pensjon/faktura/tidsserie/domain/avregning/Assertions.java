package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractObjectAssert;

class Assertions {
    static AbstractObjectAssert<?, Premiebeloep> assertAdministrasjonsgebyr(final Premier premier) {
        return assertThat(premier.administrasjonsgebyr()).as("administrasjonsgebyr fra " + premier);
    }

    static AbstractObjectAssert<?, Premiebeloep> assertArbeidsgiverpremie(final Premier premier) {
        return assertThat(premier.arbeidsgiver()).as("arbeidsgiverpremie fra " + premier);
    }

    static AbstractObjectAssert<?, Premiebeloep> assertMedlemspremie(final Premier premier) {
        return assertThat(premier.medlem()).as("medlemspremie fra " + premier);
    }

    static AbstractObjectAssert<?, Premiebeloep> assertPremiebeloep(final Premiebeloep beloep, final int desimalarForventa) {
        assertDesimaler(beloep).isEqualTo(desimalarForventa);
        return assertThat(beloep).as("premiebeløp " + beloep);
    }

    static AbstractIntegerAssert<?> assertDesimaler(final Premiebeloep beloep) {
        return assertThat(beloep.desimaler()).as("forventa antall desimalar(scale) i " + beloep);
    }
}
