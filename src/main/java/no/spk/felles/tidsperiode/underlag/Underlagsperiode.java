package no.spk.felles.tidsperiode.underlag;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.felles.tidsperiode.AbstractTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

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
public class Underlagsperiode extends AbstractTidsperiode<Underlagsperiode>
        implements HarKoblingar, Annoterbar<Underlagsperiode>, Beregningsperiode<Underlagsperiode> {
    private final Koblingar koblingar = new Koblingar();

    private final Map<Class<? extends BeregningsRegel<?>>, Object> cache = new HashMap<>();

    private final Annotasjonar annotasjonar;

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
        this(fraOgMed, tilOgMed, new Annotasjonar());
    }

    private Underlagsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed, final Annotasjonar annotasjonar) {
        super(fraOgMed, of(requireNonNull(tilOgMed, "til og med-dato er påkrevd, men var null")));
        this.annotasjonar = annotasjonar;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T beregn(final Class<? extends BeregningsRegel<T>> regelType) throws PaakrevdAnnotasjonManglarException {
        if (!cache.containsKey(regelType)) {
            cache.put(regelType, annotasjonFor(regelType).beregn(this));
        }
        return (T) cache.get(regelType);
    }

    @Override
    public void kobleTil(final Tidsperiode<?> kobling) {
        koblingar.add(kobling);
    }

    @Override
    public <T extends Tidsperiode<T>> Optional<T> koblingAvType(final Class<T> type) {
        return koblingar.koblingAvType(type);
    }

    @Override
    public <T extends Tidsperiode<T>> Optional<T> koblingAvType(final Class<T> type, final Predicate<T> predikat) {
        return koblingar.koblingAvType(type, predikat);
    }

    @Override
    public <T extends Tidsperiode<?>> Stream<T> koblingarAvType(final Class<T> type) {
        return koblingar.koblingarAvType(type);
    }

    @Override
    public <T> T annotasjonFor(final Class<T> type) throws PaakrevdAnnotasjonManglarException {
        final Optional<T> resultat = annotasjonar.lookup(type);
        if (!resultat.isPresent()) {
            throw new PaakrevdAnnotasjonManglarException(this, type);
        }
        return resultat.get();
    }

    @Override
    public <T> Optional<T> valgfriAnnotasjonFor(final Class<T> type) {
        return annotasjonar.lookup(type);
    }

    @Override
    public <T> Underlagsperiode annoter(final Class<? extends T> type, final T verdi) {
        annotasjonar.registrer(type, verdi);
        return this;
    }

    @Override
    public Underlagsperiode annoterFra(final Underlagsperiode kilde) {
        annotasjonar.addAll(kilde.annotasjonar);
        return this;
    }

    /**
     * Til og med-datoen til underlagsperioda.
     * <p>
     * Merk at sjølv om underlagsperioda alltid er garanterert å ha ein til og med-dato blir den returnert
     * som ein {@link Optional} for å oppfølge den generelle kontrakta til tidsperioder.
     *
     * @return siste dag i underlagsperioda, garantert å vere {@link Optional#isPresent() tilgjengelig}
     */
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }

    @Override
    public String toString() {
        return "UP[" + fraOgMed + "->" + tilOgMed.map(LocalDate::toString).orElse("") + "]";
    }

    /**
     * Genererer en modifisert kopi av underlagsperioden, inkludert annotasjonane.
     * <p>
     * Den nye perioda arvar ikkje frå og med- og til og med-dato til perioda, dei angitte datoane blir brukt
     * som nye datoar for kopien.
     * <p>
     * Kopien vil bli generert uten noen kopi av originalens periodekoblinger, kun en kopi av originalens annotasjoner.
     *
     * @param fraOgMed kopiens fra og med-dato
     * @param tilOgMed kopiens til og med-dato
     * @return en modifisert kopi av underlagsperioden og dens annotasjoner
     */
    public Underlagsperiode kopierUtenKoblinger(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        return new Underlagsperiode(
                fraOgMed,
                tilOgMed
        )
                .annoterFra(this);
    }
}
