package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import java.time.LocalDate;
import java.time.Month;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * {@link Maaned} representerer ein bestemt måned
 * som ligg innanfor eit {@link Aar}.
 *
 * @author Tarjei Skorgenes
 */
public class Maaned extends AbstractTidsperiode<Maaned> {
    private final Aarstall aar;
    private final Month maaned;

    /**
     * Konstruerer ei ny tidsperiode som strekker seg frå første til siste dag i den aktuelle måneden
     * for det aktuelle året.
     *
     * @param aar    aaret tidsperioda skal ligge innanfor
     * @param maaned årets måned som tidsperioda skal strekke seg åver
     * @throws NullPointerException if <code>dato</code> er <code>null</code>
     */
    public Maaned(final Aarstall aar, final Month maaned) {
        super(
                dato(
                        requireNonNull(aar, () -> "årstall er påkrevd, men var null"),
                        requireNonNull(maaned, () -> "måned er påkrevd, men var null")
                ).with(firstDayOfMonth()),
                of(dato(aar, maaned).with(lastDayOfMonth()))
        );
        this.aar = aar;
        this.maaned = maaned;
    }

    /**
     * Returnerer kva for ein måned i året perioda tilhøyrer.
     *
     * @return periodas tilhøyrande måned i året
     */
    public Month toMonth() {
        return maaned;
    }

    /**
     * Er vi tilknytta den angitte måneden i året?
     *
     * @param month måned i året som vi skal sjekke om vi er tilknytta
     * @return <code>true</code> dersom perioda er tilknytta den angitte måneden i året,
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
