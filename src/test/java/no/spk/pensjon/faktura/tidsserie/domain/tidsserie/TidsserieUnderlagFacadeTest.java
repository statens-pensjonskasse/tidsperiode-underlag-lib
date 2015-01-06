package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;

/**
 * Enheitstestar av {@link TidsserieUnderlagFacade}.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieUnderlagFacadeTest {
    private final TidsserieUnderlagFacade fasade = new TidsserieUnderlagFacade();


    private Underlag annoterAllePerioder(final UnderlagsperiodeBuilder... perioder) {
        return fasade.annoter(
                new Underlag(
                        Stream
                                .of(perioder)
                                .map(UnderlagsperiodeBuilder::bygg)
                )
        );
    }

    private static UnderlagsperiodeBuilder eiTomPeriode() {
        return new UnderlagsperiodeBuilder();
    }
}