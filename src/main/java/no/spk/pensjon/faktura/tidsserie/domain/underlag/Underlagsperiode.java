package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * Ei tidsperiode som inng�r som ein del av eit underlag.
 * <p>
 * Underlagsperioder har som funksjon � representere den minste tidsperioda der ingen av underlagsperiodas
 * tilknytta tidsperioder endrar innhold. Underlagsperiodas hensikt er alts� � inng� som ein del av eit
 * underlag som kan benyttast for � finne ut og beregne verdiar som baserer seg p� grunnlagsdata som er periodiserte
 * og som kan endre verdi eller betydning over tid.
 * <br>
 * Underlagsperioder kan ikkje vere l�pande ettersom eit {@link Underlag} kun skal best� av lukka tidsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class Underlagsperiode extends AbstractTidsperiode<Underlagsperiode> implements Annoterbar<Underlagsperiode>,HarKoblingar {
    private final Koblingar koblingar = new Koblingar();

    private final Annotasjonar annotasjonar = new Annotasjonar();

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
        super(fraOgMed, of(requireNonNull(tilOgMed, () -> "til og med-dato er p�krevd, men var null")));
    }

    /**
     * Sl�r opp ein beregningsregel av ei bestemt type og brukar den for � gjere ei bestemt type beregning
     * ut fr� underlagsperiodas annoterte fakta.
     *
     * @param regelType kva type beregningsregel som skal brukast
     * @return resultatet fr� beregningsregelen basert p� underlagsperiodas tilstand
     * @throws PaakrevdAnnotasjonManglarException dersom perioda ikkje er annotert med ein regel av den angitte typen
     */
    public <T> T beregn(final Class<? extends BeregningsRegel<T>> regelType) throws PaakrevdAnnotasjonManglarException {
        return annotasjonFor(regelType).beregn(this);
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
    public <T extends Tidsperiode<?>> Stream<T> koblingarAvType(final Class<T> type) {
        return koblingar.koblingarAvType(type);
    }

    @Override
    public <T> T annotasjonFor(final Class<T> type) throws PaakrevdAnnotasjonManglarException {
        return annotasjonar
                .lookup(type)
                .orElseThrow(() -> new PaakrevdAnnotasjonManglarException(this, type));
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
     * som ein {@link Optional} for � oppf�lge den generelle kontrakta til tidsperioder.
     *
     * @return siste dag i underlagsperioda, garantert � vere {@link Optional#isPresent() tilgjengelig}
     */
    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }

    @Override
    public String toString() {
        return "UP[" + fraOgMed + "->" + tilOgMed.map(d -> d.toString()).orElse("") + "]";
    }

    /**
     * Genererer en modifisert kopi av underlagsperioden, inkludert annotasjonane.
     * <p>
     * Den nye perioda arvar ikkje fr� og med- og til og med-dato til perioda, dei angitte datoane blir brukt
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
