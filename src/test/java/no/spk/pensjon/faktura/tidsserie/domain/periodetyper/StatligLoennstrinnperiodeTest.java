package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

public class StatligLoennstrinnperiodeTest {
    private static final int DEFAULT_TRINN = 10;

    @Test
    public void skalTilhoeyreApotekordninga() {
        assertTilhoeyrer(eiPeriode(), Ordning.SPK).isTrue();
        assertTilhoeyrer(eiPeriode(), Ordning.valueOf("3010")).isTrue();
        assertTilhoeyrer(eiPeriode(), Ordning.valueOf(3010)).isTrue();
    }

    @Test
    public void skalIkkjeTilhoeyreAndreOrdninga() {
        assertTilhoeyrer(eiPeriode(), Ordning.POA).isFalse();
        assertTilhoeyrer(eiPeriode(), Ordning.OPERA).isFalse();
    }

    @Test
    public void skalIkkjeKreveStillingskodeForOppslagAvLoenn() {
        assertThat(
                eiPeriode()
                        .harLoennFor(
                                loennstrinn(),
                                empty()
                        )
        )
                .isTrue();
        assertThat(
                eiPeriode()
                        .harLoennFor(
                                new Loennstrinn(19),
                                empty()
                        )
        )
                .isFalse();
    }

    private static StatligLoennstrinnperiode eiPeriode() {
        return new StatligLoennstrinnperiode(
                dato("1989.04.01"),
                empty(),
                loennstrinn(),
                new Kroner(40_500)
        );
    }

    private static Loennstrinn loennstrinn() {
        return new Loennstrinn(DEFAULT_TRINN);
    }

    private static AbstractBooleanAssert<?> assertTilhoeyrer(final StatligLoennstrinnperiode periode, final Ordning ordning) {
        return assertThat(periode.tilhoeyrer(ordning)).as("tilhøyrer " + periode + " " + ordning + "?");
    }
}