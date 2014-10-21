package no.spk.pensjon.faktura.tidsserie.domain;

import java.time.LocalDate;
import java.time.Year;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.Aarstall} representerer eit �rstall.
 *
 * @author Tarjei Skorgenes
 */
public class Aarstall {
    private final int aarstall;

    /**
     * Konstruerer eit nytt verdiobjekt.
     *
     * @param aarstall �rstallet det skal konstruerast eit verdiobjekt for
     */
    public Aarstall(final int aarstall) {
        this.aarstall = aarstall;
    }

    /**
     * Returnerer f�rste dag i �ret �rstallet tilh�yrer.
     *
     * @return 1. januar i det aktuelle �ret
     */
    public LocalDate atStartOfYear() {
        return Year.of(aarstall).atMonth(JANUARY).atDay(1);
    }

    /**
     * Returnerer siste dag i �ret �rstallet tilh�yrer.
     *
     * @return 31. desember i det aktuelle �ret
     */
    public LocalDate atEndOfYear() {
        return Year.of(aarstall).atMonth(DECEMBER).atEndOfMonth();
    }

    @Override
    public String toString() {
        return Integer.toString(aarstall);
    }
}
