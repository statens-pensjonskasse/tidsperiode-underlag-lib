package no.spk.pensjon.faktura.tidsserie.storage.csv;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Snorre E. Brekke - Computas
 */
public class AvtaleproduktOversetterTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();
    
    private final AvtaleproduktOversetter oversetter = new AvtaleproduktOversetter();

    @Test
    public void testSolskinnslinje() throws Exception {
        assertThat(
                oversetter.oversett(
                        asList(
                                "AVTALEPRODUKT;100001;PEN;2007.01.01;2010.08.31;11;0.00;0.00;10.00;0;0.0;0.00".split(";")
                        )
                )
        ).isEqualToComparingFieldByField(new Avtaleprodukt(
                LocalDate.of(2007, 1, 1),
                Optional.of(LocalDate.of(2010, 8, 31)),
                AvtaleId.valueOf("100001"),
                Produkt.PEN,
                11,
                Optional.of(new Satser<>(
                        ZERO,
                        ZERO,
                        prosent("10%"))),
                empty()));
    }

    @Test
    public void testUkjentProduktstrengSkalGiUkjentProduktEnum() throws Exception {
        assertThat(
                oversetter.oversett(
                        asList(
                                "AVTALEPRODUKT;100001;XXX;2007.01.01;2010.08.31;11;0.00;0.00;10.00;0;0;0".split(";")
                        )
                ).produkt()
        ).isEqualTo(Produkt.UKJ);
    }

    @Test
    public void testProduktlinjeMedPremisatserOgKronestatserKasterFeilmelding() throws Exception {
        e.expect(IllegalStateException.class);
        e.expectMessage("Både prosentsatser og kronesatser kan ikke være i bruk for et avtaleprodukt.");
        oversetter.oversett(
                asList(
                        "AVTALEPRODUKT;100001;XXX;2007.01.01;2010.08.31;11;0.00;0.00;10.00;0;0;2".split(";")
                )
        );
    }
}