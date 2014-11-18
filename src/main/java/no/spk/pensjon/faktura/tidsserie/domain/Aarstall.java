package no.spk.pensjon.faktura.tidsserie.domain;

import java.time.LocalDate;
import java.time.Year;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.Aarstall} representerer eit årstall.
 *
 * @author Tarjei Skorgenes
 */
public class Aarstall {
    private final int aarstall;

    /**
     * Konstruerer eit nytt verdiobjekt.
     *
     * @param aarstall årstallet det skal konstruerast eit verdiobjekt for
     */
    public Aarstall(final int aarstall) {
        this.aarstall = aarstall;
    }

    /**
     * Returnerer første dag i året årstallet tilhøyrer.
     *
     * @return 1. januar i det aktuelle året
     */
    public LocalDate atStartOfYear() {
        return Year.of(aarstall).atMonth(JANUARY).atDay(1);
    }

    /**
     * Returnerer siste dag i året årstallet tilhøyrer.
     *
     * @return 31. desember i det aktuelle året
     */
    public LocalDate atEndOfYear() {
        return Year.of(aarstall).atMonth(DECEMBER).atEndOfMonth();
    }

    @Override
    public int hashCode() {
        return aarstall;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Aarstall other = (Aarstall) obj;
        return aarstall == other.aarstall;
    }

    @Override
    public String toString() {
        return Integer.toString(aarstall);
    }

    /**
     * Opprettar ein ny instans av {@link java.time.Year} som representerer det aktuelle årstallet.
     *
     * @return ein ny instans av Year for samme årstall
     */
    public Year toYear() {
        return Year.of(aarstall);
    }

    /**
     * Returnerer eit årstall for det kronologisk foregåande året.
     *
     * @return forrige årstall
     */
    public Aarstall forrige() {
        return new Aarstall(aarstall - 1);
    }

    /**
     * Returnerer eit årstall for det kronologisk etterfølgande året.
     *
     * @return neste årstall
     */
    public Aarstall neste() {
        return new Aarstall(aarstall + 1);
    }
}
