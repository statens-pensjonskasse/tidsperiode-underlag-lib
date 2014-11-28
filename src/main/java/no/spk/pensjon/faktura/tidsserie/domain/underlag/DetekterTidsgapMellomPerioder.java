package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * {@link DetekterTidsgapMellomPerioder} representerer
 * algoritma som kvalitetssikrar at eit {@link Underlag} ikkje
 * inneheld tidsgap mellom ei eller fleire av underlagsperiodene sine.
 *
 * @author Tarjei Skorgenes
 */
class DetekterTidsgapMellomPerioder implements BinaryOperator<Underlagsperiode> {
    // Vi allokerer ei liste med ein initiell kapasitet p� 0 element for � redusere minnebruken
    // for dei 99.9% av underlaga som det ikkje eksisterer nokon tidsgap p�
    private final List<Underlagsperiode[]> tidsgap = new ArrayList<>(0);

    /**
     * Verifiserer at det ikkje eksisterer eit tidsgap p� meir enn 1 dag mellom <code>previous</code>
     * sin til og med-dato og <code>current</code> sin fra og med-dato.
     * <p>
     * Dersom det er meir enn 1 dag mellom desse to datoane indikerer det at underlaget er inkonsistent og manglar
     * ei eller fleire underlagsperioder for � vere konsistent.
     *
     * @param previous forrige underlagsperiode
     * @param current  gjeldande underlagsperiode
     * @return gjeldande underlagsperioda slik at den blir ny <code>previous</code> for neste kall
     */
    @Override
    public Underlagsperiode apply(final Underlagsperiode previous, Underlagsperiode current) {
        if (previous.tilOgMed().orElse(LocalDate.MAX).isBefore(current.fraOgMed().minusDays(1))) {
            tidsgap.add(new Underlagsperiode[]{previous, current});
        }
        return current;
    }

    /**
     * Har det blitt oppdaga nokon tidsgap mellom underlagsperiodene som algoritma har behandla?
     *
     * @return <code>true</code> dersom det er oppdaga eit eller fleire tidsgap,
     * <code>false</code> viss det ikkje er oppdaga nokon tidsgap hittil
     */
    boolean harTidsgap() {
        return !tidsgap.isEmpty();
    }

    Stream<Underlagsperiode[]> stream() {
        return tidsgap.stream();
    }
}
