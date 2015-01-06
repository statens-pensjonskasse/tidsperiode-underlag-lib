package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.internal.MinstegrenseRegel}.
 *
 * @author Tarjei Skorgenes
 */
public class MinstegrenseRegelTest {
    @Test
    public void skalGenerereForventaMinstegrenseForAllePremiestatusarTilknyttaSPKOrdninga() {
        final UnderlagsperiodeBuilder periode = eiPeriode().med(Ordning.SPK);
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-01"))).isEqualTo(new Minstegrense(new Prosent("35%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-02"))).isEqualTo(new Minstegrense(new Prosent("35%")));

        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-03"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-04"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-05"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-06"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-07"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-08"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-09"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-10"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-11"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-12"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("AAO-13"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));

        assertMinstegrense(periode.med(Premiestatus.valueOf("MED-01"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
        assertMinstegrense(periode.med(Premiestatus.valueOf("MED-02"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));

        assertMinstegrense(periode.med(Premiestatus.valueOf("FIK"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));

        assertMinstegrense(periode.med(Premiestatus.valueOf("IPB"))).isEqualTo(new Minstegrense(new Prosent("37.33%")));
    }

    @Test
    public void skalBenytteMinstegrense50ProsentForOperaordninga() {
        final UnderlagsperiodeBuilder periode = eiPeriode().med(Ordning.OPERA);
        assertMinstegrense(periode.med(Premiestatus.AAO_01)).isEqualTo(new Minstegrense(new Prosent("50%")));
    }

    @Test
    public void skalGenerereForventaMinstegrenseForAlleStillingskoderTilknyttaApotekordning() {
        final UnderlagsperiodeBuilder periode = eiPeriode().med(Ordning.POA);

        assertMinstegrense(periode.med(Stillingskode.parse(1))).isEqualTo(new Minstegrense(new Prosent("38.46%")));
        assertMinstegrense(periode.med(Stillingskode.parse(2))).isEqualTo(new Minstegrense(new Prosent("38.46%")));
        assertMinstegrense(periode.med(Stillingskode.parse(3))).isEqualTo(new Minstegrense(new Prosent("38.46%")));
        assertMinstegrense(periode.med(Stillingskode.parse(4))).isEqualTo(new Minstegrense(new Prosent("38.46%")));
        assertMinstegrense(periode.med(Stillingskode.parse(10))).isEqualTo(new Minstegrense(new Prosent("38.46%")));
        assertMinstegrense(periode.med(Stillingskode.parse(11))).isEqualTo(new Minstegrense(new Prosent("38.46%")));

        assertMinstegrense(periode.med(Stillingskode.parse(5))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(60))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(61))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(7))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(8))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(9))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(12))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(13))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
        assertMinstegrense(periode.med(Stillingskode.parse(14))).isEqualTo(new Minstegrense(new Prosent("37.5%")));
    }

    private static UnderlagsperiodeBuilder eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2013.01.01"))
                .tilOgMed(dato("2013.12.31"))
                .med(new MinstegrenseRegel())
                ;
    }

    private static AbstractObjectAssert<?, Minstegrense> assertMinstegrense(final UnderlagsperiodeBuilder periode) {
        return assertThat(
                periode
                        .bygg()
                        .beregn(MinstegrenseRegel.class)
        ).as("minstegrense");
    }
}