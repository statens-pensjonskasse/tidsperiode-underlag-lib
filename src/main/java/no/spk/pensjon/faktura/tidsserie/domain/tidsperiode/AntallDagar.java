package no.spk.pensjon.faktura.tidsserie.domain.tidsperiode;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * {@link AntallDagar} representerer lengda p� ei tidsperiode.
 * <br>
 * Antall dagar skal aldri vere kortare enn 1 dag, 0 eller negative verdiar blir ikkje godtatt.
 *
 * @author Tarjei Skorgenes
 */
public class AntallDagar {
    private final int antall;

    /**
     * Konstruerer eit nytt verdiobjekt som representerer lengde p� ei tidsperiode i antall dagar.
     *
     * @param antall antall dagar
     * @throws java.lang.IllegalArgumentException dersom antall er mindre enn eller lik <code>0</code>
     */
    public AntallDagar(final int antall) {
        if (antall < 1) {
            throw new IllegalArgumentException("antall dagar kan ikkje vere kortare enn 1 dag, men var " + antall + " dagar");
        }
        this.antall = antall;
    }

    /**
     * Returnerer lengda i antall dagar.
     *
     * @return antall dagar
     */
    public int verdi() {
        return antall;
    }

    /**
     * Konstruerer ein ny {@link AntallDagar}.
     *
     * @param antall verdi for antall dagar
     * @return nytt verdiobjekt som inneheld verdi for antall dagar
     * @see AntallDagar#AntallDagar(int)
     */
    public static AntallDagar antallDagar(final int antall) {
        return new AntallDagar(antall);
    }

    /**
     * Beregnar antall dagar i perioda fr� og med <code>fraOgMed</code> og til og med <code>tilOgMed</code>.
     * <br>
     * Negativ lengde er ikkje st�tta, til og med-dato m� derfor vere st�rre enn eller lik fr� og med-datoen.
     *
     * @param fraOgMed fr� og med-dato for perioda
     * @param tilOgMed til og med-dato for perioda
     * @return lengda p� tidsperioda mellom dei to dagane, inkludert sj�lve fr� og med- og til og med-datoane
     * @throws IllegalArgumentException viss <code>fraOgMed</code> er etter <code>tilOgMed</code>
     */
    public static AntallDagar antallDagarMellom(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        requireNonNull(fraOgMed, "fr� og med-dato m� vere ulik null");
        requireNonNull(tilOgMed, "til og med-dato m� vere ulik null, l�pande perioder er ikkje st�tta");
        Validering.feilVissFraOgMedErEtterTilOgMedDato(fraOgMed, tilOgMed);
        return antallDagar((int) ChronoUnit.DAYS.between(fraOgMed, tilOgMed) + 1);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(antall);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final AntallDagar other = (AntallDagar) obj;
        return antall == other.antall;
    }

    @Override
    public String toString() {
        return Integer.toString(antall) + " dagar";
    }
}
