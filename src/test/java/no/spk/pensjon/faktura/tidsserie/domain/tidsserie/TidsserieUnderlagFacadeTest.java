package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import java.util.stream.Stream;

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