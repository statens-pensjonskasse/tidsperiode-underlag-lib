package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.assertj.core.data.Offset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent}.
 *
 * @author Tarjei Skorgenes
 */
public class ProsentTest {
    private static final Offset<Double> PRESISJON = offset(0.00001);

    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalTillateMellomromVedKonverteringFraTekstTilProsent() {
        assertThat(prosent("45 %").toDouble()).isEqualTo(0.45d, PRESISJON);
    }

    @Test
    public void skalTillateKommaVedKonverteringFraTekstTilProsent() {
        assertThat(prosent("45,4 %").toDouble()).isEqualTo(0.454d, PRESISJON);
    }

    @Test
    public void skalTillatePunktumVedKonverteringFraTekstTilProsent() {
        assertThat(prosent("31.7 %").toDouble()).isEqualTo(0.317d, PRESISJON);
    }

    @Test
    public void skalFeilVissAntallKommaErMeirEnn1() {
        e.expect(NumberFormatException.class);
        prosent("100.0,1%");
    }

    @Test
    public void skalFeileVissTekstErNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("er påkrevd");
        e.expectMessage("men var null");
        prosent(null);
    }

    @Test
    public void skalFeilVissAntallPunktumErMeirEnn1() {
        e.expect(NumberFormatException.class);
        prosent("100..1%");
    }

    @Test
    public void skalFeileDersomTekstenInneheldeUstoettaTegn() {
        e.expect(NumberFormatException.class);
        prosent("ABCD %");
    }

    @Test
    public void skalRepresentere100ProsentSom1Komma0() {
        assertThat(prosent("100%").toDouble()).isEqualTo(1.0, PRESISJON);
    }

    @Test
    public void skalRepresentere0ProsentSom0Komma0() {
        assertThat(prosent("0%").toDouble()).isEqualTo(0.0, PRESISJON);
    }

    @Test
    public void skalDeleDesimalverdienPaaHundreVedKonstruksjon() {
        assertThat(new Prosent(100d).toDouble()).isEqualTo(100d, PRESISJON);
    }

    @Test
    public void skalViseProsentsatsFormatertViaToStringForEnklareLoggingOgDebugging() {
        assertThat(prosent("100%").toString()).isEqualTo("100%");
        assertThat(new Prosent(100d).toString()).isEqualTo("10000%");
        assertThat(new Prosent(-2.000149d).toString()).isEqualTo("-200,015%");
    }

    @Test
    public void skalLeggeSamanVerdiane() {
        assertThat(prosent("12.459%").plus(prosent("26.315%")).toDouble())
                .isEqualTo(prosent("38.774%").toDouble(), PRESISJON);
    }

    @Test
    public void skalMultiplisereSamanVerdiane() {
        assertThat(prosent("10%").multiply(prosent("10%")).toDouble())
                .isEqualTo(prosent("1%").toDouble(), PRESISJON);
    }
}