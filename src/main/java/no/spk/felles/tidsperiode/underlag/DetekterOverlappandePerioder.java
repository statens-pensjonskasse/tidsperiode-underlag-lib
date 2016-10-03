package no.spk.felles.tidsperiode.underlag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * {@link DetekterOverlappandePerioder} representerer
 * algoritma som kvalitetssikrar at eit {@link Underlag} ikkje
 * inneheld overlappande underlagsperioder.
 *
 * @author Tarjei Skorgenes
 */
class DetekterOverlappandePerioder implements BinaryOperator<Underlagsperiode> {
    // Vi allokerer ei liste med ein initiell kapasitet på 0 element for å redusere minnebruken
    // for dei 99.9% av underlaga som det ikkje eksisterer nokon overlappar på
    private final List<Underlagsperiode[]> overlappar = new ArrayList<>(0);

    /**
     * Verifiserer at <code>current</code> ikkje overlappar <code>previous</code>.
     * <p>
     * Dersom periodene overlappar indikerer det at underlaget er inkonsistent og er konstruert ut frå eit totally
     * bollocks sett med underlagsperioder.
     *
     * @param previous forrige underlagsperiode
     * @param current  gjeldande underlagsperiode
     * @return gjeldande underlagsperioda slik at den blir ny <code>previous</code> for neste kall
     */
    @Override
    public Underlagsperiode apply(final Underlagsperiode previous, Underlagsperiode current) {
        if (previous.overlapper(current)) {
            overlappar.add(new Underlagsperiode[]{previous, current});
        }
        return current;
    }

    /**
     * Har algoritma oppdaga nokon underlagsperioder som overlappar kvarandre?
     *
     * @return <code>true</code> dersom det er oppdaga ei eller fleire overlappande underlagsperioder
     * <code>false</code> viss det ikkje er oppdaga nokon perioder som overlappar kvarandre
     */
    boolean harOverlappande() {
        return !overlappar.isEmpty();
    }

    Stream<Underlagsperiode[]> stream() {
        return overlappar.stream();
    }
}
