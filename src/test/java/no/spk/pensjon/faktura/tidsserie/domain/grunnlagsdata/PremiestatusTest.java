package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PremiestatusTest {
    /**
     * Verifiserer at det ved oppslag av premiestatus AAO01 og AAO-02 ikkje blir generert nokon ny instans, men at dei
     * predefinerte konstantane for desse gruppene blir returnert.
     */
    @Test
    public void skalSlaaOppKonstantForBaadeAAO01OgAAO02() {
        assertThat(Premiestatus.valueOf("AAO-01")).isSameAs(Premiestatus.AAO_01);
        assertThat(Premiestatus.valueOf("AAO-02")).isSameAs(Premiestatus.AAO_02);
    }

    @Test
    public void skalVereLikeVissKodeErLik() {
        assertThat(Premiestatus.valueOf("XYZ")).isEqualTo(Premiestatus.valueOf("XYZ"));
        assertThat(Premiestatus.valueOf("XYZ").hashCode()).isEqualTo(Premiestatus.valueOf("XYZ").hashCode());

        assertThat(Premiestatus.valueOf("XYZ")).isNotEqualTo(Premiestatus.valueOf("ZYX"));
    }
}