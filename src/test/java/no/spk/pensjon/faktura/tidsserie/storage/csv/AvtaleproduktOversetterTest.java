package no.spk.pensjon.faktura.tidsserie.storage.csv;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import junit.framework.TestCase;

/**
 * @author Snorre E. Brekke - Computas
 */
public class AvtaleproduktOversetterTest extends TestCase {

    private final AvtaleproduktOversetter oversetter = new AvtaleproduktOversetter();

    public void testSolskinnslinje() throws Exception {
        assertThat(
                oversetter.oversett(
                        asList(
                                "AVTALEPRODUKT;100001;PEN;2007.01.01;2010.08.31;11;0.00;0.00;10.00;200;200.5;0.00".split(";")
                        )
                )
        ).isEqualTo(new Avtaleprodukt(
                LocalDate.of(2007, 1, 1),
                Optional.of(LocalDate.of(2010, 8, 31)),
                AvtaleId.valueOf("100001"),
                Produkt.PEN,
                11,
                Prosent.ZERO,
                Prosent.ZERO,
                Prosent.prosent("10%"),
                Kroner.kroner("200"),
                Kroner.kroner("200.5"),
                Kroner.kroner(0)));
    }

    public void testUkjentProduktstrengSkalGiUkjentProduktEnum() throws Exception {
        assertThat(
                oversetter.oversett(
                        asList(
                                "AVTALEPRODUKT;100001;XXX;2007.01.01;2010.08.31;11;0.00;0.00;10.00;0;0;0".split(";")
                        )
                ).produkt()
        ).isEqualTo(Produkt.UKJ);
    }
}