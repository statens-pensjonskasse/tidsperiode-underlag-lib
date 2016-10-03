package no.spk.felles.tidsperiode;

import java.time.LocalDate;
import java.time.Year;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static no.spk.felles.tidsperiode.AntallDagar.antallDagarMellom;

/**
 * {@link Aarstall} representerer eit årstall.
 *
 * @author Tarjei Skorgenes
 */
public class Aarstall {
    private final int aarstall;
    private final AntallDagar lengde;
    private LocalDate fraOgMed;
    private LocalDate tilOgMed;

    /**
     * Konstruerer eit nytt verdiobjekt.
     *
     * @param aarstall årstallet det skal konstruerast eit verdiobjekt for
     */
    public Aarstall(final int aarstall) {
        this.aarstall = aarstall;
        fraOgMed = Year.of(aarstall).atMonth(JANUARY).atDay(1);
        tilOgMed = Year.of(aarstall).atMonth(DECEMBER).atEndOfMonth();
        lengde = antallDagarMellom(
                fraOgMed,
                tilOgMed
        );
    }

    /**
     * Returnerer første dag i året årstallet tilhøyrer.
     *
     * @return 1. januar i det aktuelle året
     */
    public LocalDate atStartOfYear() {
        return fraOgMed;
    }

    /**
     * Returnerer siste dag i året årstallet tilhøyrer.
     *
     * @return 31. desember i det aktuelle året
     */
    public LocalDate atEndOfYear() {
        return tilOgMed;
    }

    /**
     * Returnerer antall dagar i det aktuelle året.
     *
     * @return antall dagar i det aktuelle året
     * @since 1.1.2
     */
    public AntallDagar lengde() {
        return lengde;
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
