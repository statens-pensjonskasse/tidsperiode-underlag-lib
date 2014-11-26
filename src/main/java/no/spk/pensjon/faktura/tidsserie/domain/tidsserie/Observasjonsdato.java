package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;

import java.time.LocalDate;
import java.time.Month;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.requireNonNull;

/**
 * {@link Observasjonsdato} representerer dagen ein observasjon blir simulert utført.
 * <p>
 * Observasjonsdatoen regulerer kva perioder i årsunderlaget som er synlige når ein utfører observasjonen.
 * Underlagsperioder og endringar med frå og med- eller aksjonsdato etter denne dagen blir ikkje tatt hensyn
 * til når observasjonen blir generert.
 * <p>
 * Observasjonsdatoen regulerer og kva underlagsperiode og endringar som blir lagt til grunn som gjeldande ut det
 * aktuelle året observasjondatoen ligg innanfor, viss stillingsforholdets framleis er aktivt på observasjonsdatoen.
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
        this.dato = requireNonNull(dato, () -> "dato manglar verdi, men er påkrevd");
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
     * Opprettar ein ny observasjonsdato siste dag i det aktuelle årets månad.
     *
     * @param aarstall årstallet observasjonsdatoen ligg innanfor
     * @param month    månaden som observasjonsdatoen skal vere siste dag i månaden for
     * @return ein ny observasjonsdato som er siste dag i det angitte årets måned
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
     * Ligg observasjonsdatoen innanfor det angitte året?
     *
     * @param aarstall årstallet til året datoen skal sjekkast mot
     * @return <code>true</code> dersom datoen ligg innanfor det angitt året, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Aarstall aarstall) {
        return dato.getYear() == aarstall.toYear().getValue();
    }

    /**
     * Ligg observasjonsdatoen innanfor den angitte månaden?
     *
     * @param month månaden som datoen skal sjekkast mot
     * @return <code>true</code> dersom datoen ligg innanfor den angitte månaden,  <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Month month) {
        return dato.getMonth().equals(month);
    }
}
