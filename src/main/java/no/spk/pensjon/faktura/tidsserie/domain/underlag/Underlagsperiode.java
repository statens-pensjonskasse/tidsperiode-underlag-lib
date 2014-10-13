package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Ei tidsperiode som inng�r som ein del av eit underlag.
 * <p>
 * Underlagsperioder har som funksjon � representere den minste tidsperioda der ingen av underlagsperiodas
 * tilknytta tidsperioder endrar innhold. Underlagsperiodas hensikt er alts� � inng� som ein del av eit
 * underlag som kan benyttast for � finne ut og beregne verdiar som baserer seg p� grunnlagsdata som er periodiserte
 * og som kan endre verdi eller betydning over tid.
 *
 * @author Tarjei Skorgenes
 */
public class Underlagsperiode {
    private final Map<Object, Object> annotasjonar = new HashMap<>();

    private final LocalDate fraOgMed;

    private final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny underlagsperiode som har ein fr� og med- og ein til og med-dato ulik <code>null</code>.
     *
     * @param fraOgMed fr� og med-dato for underlagsperioda
     * @param tilOgMed til og med-dato for underlagsperioda
     * @throws NullPointerException     viss <code>fraOgMed</code> eller <code>tilOgMed</code> er
     *                                  <code>null</code>
     * @throws IllegalArgumentException dersom fra og med-dato er etter til og med-dato
     */
    public Underlagsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        requireNonNull(fraOgMed, () -> "fra og med-dato er p�krevd, men var null");
        requireNonNull(tilOgMed, () -> "til og med-dato er p�krevd, men var null");
        if (fraOgMed.isAfter(tilOgMed)) {
            throw new IllegalArgumentException("fra og med-dato kan ikkje vere etter til og med-dato, men "
                    + fraOgMed + " er etter " + tilOgMed
            );
        }
        this.fraOgMed = fraOgMed;
        this.tilOgMed = of(tilOgMed);
    }

    /**
     * Sl�r opp verdien av den p�krevde annotasjonen med den angitte typen fr� perioda.
     * <p>
     * Dersom perioda ikkje har ein annotasjon av den angitte typen blir det kasta ein feil sidan annotasjonen blir
     * behandla som p�krevd. og dermed skulle ha vore tilgjengelig p� perioda.
     *
     * @param <T>  annotasjonens type
     * @param type annotasjonens type
     * @return verdien av den angitte annotasjonstypen
     * @throws PaakrevdAnnotasjonManglarException viss perioda ikkje har ein verdi for den angitte annotasjonstypen
     */
    public <T> T annotasjonFor(final Class<T> type) throws PaakrevdAnnotasjonManglarException {
        Object verdi = annotasjonar.get(type);
        if (verdi == null) {
            throw new PaakrevdAnnotasjonManglarException(this, type);
        }
        return (T) verdi;
    }

    /**
     * Sl�r opp verdien av den valgfrie annotasjonen med den angitte typen fr� perioda.
     * <p>
     * Dersom perioda ikkje har ein annotasjon av den angitte typen blir det returnert ein {@link Optional#empty() tom}
     * verdi, det blir ikkje kasta nokon feil.
     *
     * @param <T>  annotasjonens type
     * @param type annotasjonens type
     * @return verdien av den angitte annotasjonstypen, eller {@link Optional#empty()} viss perioda ikkje har den
     * angitte annotasjonen
     */
    public <T> Optional<T> valgfriAnnotasjonFor(final Class<T> type) {
        return empty();
    }

    /**
     * Annoterer perioda med den angitte typen og verdien.
     *
     * @param <T>   annotasjonstypen
     * @param type  annotasjonstypen
     * @param verdi verdien som skal vere tilknytta annotasjonstypen
     */
    public <T> void annoter(final Class<? extends T> type, final T verdi) {
        annotasjonar.put(type, verdi);
    }

    /**
     * Fr� og med-datoen til underlagsperioda.
     *
     * @return f�rste dag i underlagsperioda
     */
    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    /**
     * Til og med-datoen til underlagsperioda.
     * <p>
     * Merk at sj�lv om underlagsperioda alltid er garanterert � ha ein til og med-dato blir den returnert
     * som ein {@link java.util.Optional} for � oppf�lge den generelle kontrakta til tidsperioder.
     *
     * @return siste dag i underlagsperioda, garantert � vere {@link java.util.Optional#isPresent() tilgjengelig}
     */
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }
}
