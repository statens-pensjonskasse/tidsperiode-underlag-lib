package no.spk.tidsserie.tidsperiode.underlag.reglar;

import java.util.stream.Stream;

import no.spk.tidsserie.tidsperiode.underlag.BeregningsRegel;

/**
 * {@link Regelsett} representerer eit sett med
 * {@link BeregningsRegel reglar} og tidsperioda dei gjeld for.
 *
 * @author Tarjei Skorgenes
 */
public interface Regelsett {
    /**
     * Returnerer eit sett med regelperioder og reglane som gjeld i desse periodene.
     *
     * @return ein straum av regelperioder
     */
    Stream<Regelperiode<?>> reglar();
}
