package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import java.time.LocalDate;
import java.time.Month;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * {@link Maaned} representerer ein bestemt m�ned
 * som ligg innanfor eit {@link Aar}.
 *
 * @author Tarjei Skorgenes
 */
public class Maaned extends AbstractTidsperiode<Maaned> {
    private final Aarstall aar;
    private final Month maaned;

    /**
     * Konstruerer ei ny tidsperiode som strekker seg fr� f�rste til siste dag i den aktuelle m�neden
     * for det aktuelle �ret.
     *
     * @param aar    aaret tidsperioda skal ligge innanfor
     * @param maaned �rets m�ned som tidsperioda skal strekke seg �ver
     * @throws NullPointerException if <code>dato</code> er <code>null</code>
     */
    public Maaned(final Aarstall aar, final Month maaned) {
        super(
                dato(
                        requireNonNull(aar, () -> "�rstall er p�krevd, men var null"),
                        requireNonNull(maaned, () -> "m�ned er p�krevd, men var null")
                ).with(firstDayOfMonth()),
                of(dato(aar, maaned).with(lastDayOfMonth()))
        );
        this.aar = aar;
        this.maaned = maaned;
    }

    /**
     * Returnerer kva for ein m�ned i �ret perioda tilh�yrer.
     *
     * @return periodas tilh�yrande m�ned i �ret
     */
    public Month toMonth() {
        return maaned;
    }

    /**
     * Er vi tilknytta den angitte m�neden i �ret?
     *
     * @param month m�ned i �ret som vi skal sjekke om vi er tilknytta
     * @return <code>true</code> dersom perioda er tilknytta den angitte m�neden i �ret,
     * <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Month month) {
        return fraOgMed().getMonth() == month;
    }

    @Override
    public String toString() {
        return maaned + " " + aar;
    }

    private static LocalDate dato(final Aarstall aar, final Month maaned) {
        return aar.atStartOfYear().withMonth(maaned.getValue());
    }
}
