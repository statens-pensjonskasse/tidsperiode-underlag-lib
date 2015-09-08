package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.time.LocalDate.MAX;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.Month;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

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
    private final boolean erSisteFor�ret;
    private final LocalDate sisteDagI�ret;

    /**
     * Konstruerer ein ny observasjondato.
     *
     * @param dato observasjondatoen
     * @throws NullPointerException dersom <code>dato</code> er <code>null</code>
     */
    public Observasjonsdato(final LocalDate dato) {
        this.dato = requireNonNull(dato, () -> "dato manglar verdi, men er p�krevd");
        this.erSisteFor�ret = Month.DECEMBER == dato.getMonth();
        this.sisteDagI�ret = dato.with(lastDayOfYear());
    }

    private Observasjonsdato(final Aarstall aarstall, final Month month) {
        this.dato = aarstall
                .toYear()
                .atMonth(month)
                .atEndOfMonth();
        this.sisteDagI�ret = aarstall.atEndOfYear();
        this.erSisteFor�ret = dato.isEqual(sisteDagI�ret);
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
        return new Observasjonsdato(aarstall, month);
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

    /**
     * Returnerer observasjonsdatoens datoverdi.
     *
     * @return observasjonsdatoens datoverdi
     */
    public LocalDate dato() {
        return dato;
    }

    /**
     * Er <code>perioda</code> synlig fr� gjeldande observasjonsdato?
     * <br>
     * Ei periode definerast som synlig dersom den har sin til og med-dato p� eller f�r observasjonsdatoen.
     * Dersom perioda er avslutta etter observasjonsdatoen er den ikkje synlig.
     *
     * @param perioda underlagsperioda som ein skal sjekke om er synlig p� observasjonsdatoen
     * @return <code>true</code> dersom perioda er avslutta f�r observasjonsdatoen, <code>false</code> ellers
     * @since 1.1.2
     */
    public boolean erPeriodenSynligFra(final Underlagsperiode perioda) {
        return !perioda.tilOgMed().orElse(MAX).isAfter(dato);
    }

    /**
     * Sjekkar om observasjonsdatoen er lik siste dag i �ret den er tilknytta, aka 31. desember?
     *
     * @return <code>true</code> dersom observasjonsdatoen er lik 31. desember, <code>false</code> ellers
     * @since 1.1.2
     */
    public boolean erAaretsSisteDag() {
        return erSisteFor�ret;
    }
}
