package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SatserTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalFeileVedKonstruksjonDersomSatserErAvForskjelligeTyper() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Alle satser på et enkelt avtaleprodukt må være av samme type");
        e.expectMessage("2 forskjellige typer satser vart forsøkt brukt");
        e.expectMessage("Kroner");
        e.expectMessage("Prosent");

        new Satser<>(kroner(10), prosent("0.35%"), kroner(10));
    }

    @Test
    public void skalVerifisereAtArbeidsgiversatsIkkeErNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("arbeidsgiver");
        new Satser<>(null, kroner(0), kroner(0));
    }

    @Test
    public void skalVerifisereAtMedlemssatsIkkeErNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("medlem");
        new Satser<>(kroner(20), null, kroner(0));
    }

    @Test
    public void skalVerifisereAtAdministrasjonsgebyrIkkeErNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("administrasjonsgebyr");
        new Satser<>(kroner(10), kroner(0), null);
    }

    @Test
    public void skalHenteUtProsentsatserVissAlleSatsverdieneErProsent() {
        final Satser<Prosent> expected = prosentsatser();
        assertThat(expected.somProsent().get()).isSameAs(expected);
    }

    @Test
    public void skalHenteUtKronesatserVissAlleSatsverdieneErKroner() {
        final Satser<Kroner> expected = kronebeloep();
        assertThat(expected.somKroner().get()).isSameAs(expected);
    }

    @Test
    public void skalHenteUtIngentingVissIkkeAlleSatsverdieneErKroner() {
        assertThat(prosentsatser().somKroner()).isEqualTo(empty());
    }

    @Test
    public void skalHenteUtIngentingVissIkkeAlleSatsverdieneErProsent() {
        assertThat(kronebeloep().somProsent()).isEqualTo(empty());
    }

    private static Satser<Kroner> kronebeloep() {
        return new Satser<>(Kroner.ZERO, Kroner.ZERO, Kroner.ZERO);
    }

    private static Satser<Prosent> prosentsatser() {
        return new Satser<>(Prosent.ZERO, Prosent.ZERO, Prosent.ZERO);
    }
}