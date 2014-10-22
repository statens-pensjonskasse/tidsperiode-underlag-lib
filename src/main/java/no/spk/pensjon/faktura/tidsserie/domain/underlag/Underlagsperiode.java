package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.GenerellTidsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Ei tidsperiode som inngår som ein del av eit underlag.
 * <p>
 * Underlagsperioder har som funksjon å representere den minste tidsperioda der ingen av underlagsperiodas
 * tilknytta tidsperioder endrar innhold. Underlagsperiodas hensikt er altså å inngå som ein del av eit
 * underlag som kan benyttast for å finne ut og beregne verdiar som baserer seg på grunnlagsdata som er periodiserte
 * og som kan endre verdi eller betydning over tid.
 * <br>
 * Underlagsperioder kan ikkje vere løpande ettersom eit {@link Underlag} kun skal bestå av lukka tidsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class Underlagsperiode extends GenerellTidsperiode {
    private final Map<Object, Object> annotasjonar = new HashMap<>();

    /**
     * Konstruerer ei ny underlagsperiode som har ein frå og med- og ein til og med-dato ulik <code>null</code>.
     *
     * @param fraOgMed frå og med-dato for underlagsperioda
     * @param tilOgMed til og med-dato for underlagsperioda
     * @throws NullPointerException     viss <code>fraOgMed</code> eller <code>tilOgMed</code> er
     *                                  <code>null</code>
     * @throws IllegalArgumentException dersom fra og med-dato er etter til og med-dato
     */
    public Underlagsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        super(fraOgMed, of(requireNonNull(tilOgMed, () -> "til og med-dato er påkrevd, men var null")));
    }

    /**
     * Slår opp ein beregningsregel av ei bestemt type og brukar den for å gjere ei bestemt type beregning
     * ut frå underlagsperiodas annoterte fakta.
     *
     * @param regelType kva type beregningsregel som skal brukast
     * @return resultatet frå beregningsregelen basert på underlagsperiodas tilstand
     * @throws PaakrevdAnnotasjonManglarException dersom perioda ikkje er annotert med ein regel av den angitte typen
     */
    public <T> T beregn(final Class<? extends BeregningsRegel<T>> regelType) throws PaakrevdAnnotasjonManglarException {
        return annotasjonFor(regelType).beregn(this);
    }

    /**
     * Slår opp verdien av den påkrevde annotasjonen med den angitte typen frå perioda.
     * <p>
     * Dersom perioda ikkje har ein annotasjon av den angitte typen blir det kasta ein feil sidan annotasjonen blir
     * behandla som påkrevd. og dermed skulle ha vore tilgjengelig på perioda.
     *
     * @param <T>  annotasjonens type
     * @param type annotasjonens type
     * @return verdien av den angitte annotasjonstypen
     * @throws PaakrevdAnnotasjonManglarException viss perioda ikkje har ein verdi for den angitte annotasjonstypen
     */
    public <T> T annotasjonFor(final Class<T> type) throws PaakrevdAnnotasjonManglarException {
        final Optional<T> verdi = valgfriAnnotasjonFor(type);
        if (!verdi.isPresent()) {
            throw new PaakrevdAnnotasjonManglarException(this, type);
        }
        return verdi.get();
    }

    /**
     * Slår opp verdien av den valgfrie annotasjonen med den angitte typen frå perioda.
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
        return ofNullable((T) annotasjonar.get(type));
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
     * Frå og med-datoen til underlagsperioda.
     *
     * @return første dag i underlagsperioda
     */
    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    /**
     * Til og med-datoen til underlagsperioda.
     * <p>
     * Merk at sjølv om underlagsperioda alltid er garanterert å ha ein til og med-dato blir den returnert
     * som ein {@link java.util.Optional} for å oppfølge den generelle kontrakta til tidsperioder.
     *
     * @return siste dag i underlagsperioda, garantert å vere {@link java.util.Optional#isPresent() tilgjengelig}
     */
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }

    @Override
    public String toString() {
        return "UP[" + fraOgMed + "->" + tilOgMed.map(d -> d.toString()).orElse("") + "]";
    }
}
