package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;

import java.time.LocalDate;
import java.time.Month;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.requireNonNull;

/**
 * {@link Observasjonsdato} representerer dagen ein observasjon blir simulert utf�rt.
 * <p>
 * Observasjonsdatoen regulerer kva perioder i �rsunderlaget som er synlige n�r ein utf�rer observasjonen.
 * Underlagsperioder og endringar med fr� og med- eller aksjonsdato etter denne dagen blir ikkje tatt hensyn
 * til n�r observasjonen blir generert.
 * <p>
 * Observasjonsdatoen regulerer og kva underlagsperiode og endringar som blir lagt til grunn som gjeldande ut det
 * aktuelle �ret observasjondatoen ligg innanfor, viss stillingsforholdets framleis er aktivt p� observasjonsdatoen.
 *
 * @author Tarjei Skorgenes
 */
public class Observasjonsdato {
    private final LocalDate dato;

    /**
     * Konstruerer ein ny observasjondato.
     *
     * @param dato observasjondatoen
     * @throws NullPointerException dersom <code>dato</code> er <code>null</code>
     */
    public Observasjonsdato(final LocalDate dato) {
        this.dato = requireNonNull(dato, () -> "dato manglar verdi, men er p�krevd");
    }

    @Override
    public int hashCode() {
        return dato.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Observasjonsdato other = (Observasjonsdato) obj;
        return dato.equals(other.dato);
    }

    @Override
    public String toString() {
        return "observasjonsdato " + dato.toString();
    }

    /**
     * Opprettar ein ny observasjonsdato siste dag i det aktuelle �rets m�nad.
     *
     * @param aarstall �rstallet observasjonsdatoen ligg innanfor
     * @param month    m�naden som observasjonsdatoen skal vere siste dag i m�naden for
     * @return ein ny observasjonsdato som er siste dag i det angitte �rets m�ned
     */
    public static Observasjonsdato forSisteDag(final Aarstall aarstall, final Month month) {
        return new Observasjonsdato(
                aarstall
                        .atStartOfYear()
                        .with(month)
                        .with(lastDayOfMonth())
        );
    }

    /**
     * Ligg observasjonsdatoen innanfor det angitte �ret?
     *
     * @param aarstall �rstallet til �ret datoen skal sjekkast mot
     * @return <code>true</code> dersom datoen ligg innanfor det angitt �ret, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Aarstall aarstall) {
        return dato.getYear() == aarstall.toYear().getValue();
    }

    /**
     * Ligg observasjonsdatoen innanfor den angitte m�naden?
     *
     * @param month m�naden som datoen skal sjekkast mot
     * @return <code>true</code> dersom datoen ligg innanfor den angitte m�naden,  <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Month month) {
        return dato.getMonth().equals(month);
    }
}
