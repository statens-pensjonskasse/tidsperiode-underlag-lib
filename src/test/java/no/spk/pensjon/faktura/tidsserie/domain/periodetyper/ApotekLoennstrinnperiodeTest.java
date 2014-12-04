package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

public class ApotekLoennstrinnperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalTilhoeyreApotekordninga() {
        assertTilhoeyrer(eiPeriode(), Ordning.POA).isTrue();
        assertTilhoeyrer(eiPeriode(), Ordning.valueOf("3060")).isTrue();
        assertTilhoeyrer(eiPeriode(), Ordning.valueOf(3060)).isTrue();
    }

    @Test
    public void skalIkkjeTilhoeyreAndreOrdninga() {
        assertTilhoeyrer(eiPeriode(), Ordning.SPK).isFalse();
        assertTilhoeyrer(eiPeriode(), Ordning.OPERA).isFalse();
    }

    @Test
    public void skalKreveStillingskodeVedOppslagAvLoennstrinn() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("stillingskode er påkrevd ved oppslag av lønn");
        e.expectMessage("Apotekordninga");
        e.expectMessage("lønnstrinn 10");
        eiPeriode().harLoennFor(Loennstrinn.loennstrinn(10), empty());
    }

    private static ApotekLoennstrinnperiode eiPeriode() {
        return new ApotekLoennstrinnperiode(
                dato("1989.04.01"),
                empty(),
                new Loennstrinn(10),
                Stillingskode.K_STIL_APO_PROVISOR,
                new LoennstrinnBeloep(new Kroner(40_500))
        );
    }

    private static AbstractBooleanAssert<?> assertTilhoeyrer(final ApotekLoennstrinnperiode periode, final Ordning ordning) {
        return assertThat(periode.tilhoeyrer(ordning)).as("tilhøyrer " + periode + " " + ordning + "?");
    }
}