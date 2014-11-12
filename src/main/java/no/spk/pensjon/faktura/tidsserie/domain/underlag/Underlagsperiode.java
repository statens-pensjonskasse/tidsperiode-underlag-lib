package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.GenerellTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Feilmeldingar.feilmeldingForMeirEnnEiKobling;

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
public class Underlagsperiode extends GenerellTidsperiode {
    private final Map<Class<? extends Tidsperiode>, Set<Tidsperiode>> koblingar = new HashMap<>();

    private final Map<Object, Object> annotasjonar = new HashMap<>();

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

    /**
     * Koblar saman underlagsperioda med ei tidsperiode som overlappar underlagsperioda heilt eller delvis.
     * <p>
     * Det er b�de mulig og tillatt � koble opp ei underlagsperiode mot fleire tidsperioder av samme type. Brukarane av
     * av underlaget er den som skal styre korvidt fleire tilkobla perioder av samme type er funksjonelt sett tillatt
     * eller ikkje fr� bruksm�nster til bruksm�nster.
     * <p>
     * Av ytelsesmessige �rsaker verifiserer ikkje underlagsperioda at <code>kobling</code> faktisk overlappar
     * underlagsperioda, det er opp til klienten � handheve denne kontrakta.
     *
     * @param kobling ei tidsperiode som underlagsperioda skal koblast opp mot
     */
    public void kobleTil(final Tidsperiode kobling) {
        koblingar
                .computeIfAbsent(
                        kobling.getClass(),
                        c -> new HashSet<>()
                )
                .add(kobling);
    }

    /**
     * Hentar ut koblinga underlagsperioda muligens har til ei tidsperiode av den angitte typen.
     * <p>
     * Denne metoda er prim�rt ei hjelpemetode for � forenkle klientar som har ei forventning til at underlagsperioder
     * kun skal kunne vere tilkobla 0 eller 1 tidsperioder av den bestemte typen. I det generelle tilfellet der
     * underlagsperioder funksjonelt sett kan vere kobla til fleire perioder av samme type, m�
     * {@link #koblingarAvType(Class)} brukast framfor denne metoda.
     * <p>
     * Dersom denne metoda blir brukt, antas det derfor at klienten forventar at viss underlagsperioda er kobla opp mot
     * meir enn ei periode av den angitte typen s� indikerer dette d�rlig datakvalitet. Alternativt at klienten er
     * feilaktig implementert. Det blir derfor kasta ein exception for � sikre at klienten blir gjort oppmerksom p�
     * problemet og kan handtere dette p� eit eller anna vis.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @return den eine tidsperioda av den angitte typen som underlagsperioda er tilkobla, eller eit
     * {@link java.util.Optional#empty() tomt} svar viss perioda ikkje er kobla til ei tidsperioda av den angitte typen
     * @throws IllegalStateException dersom perioda er tilkobla meir enn ei tidsperiode av den angitte typen
     */
    public Optional<Tidsperiode> koblingAvType(Class<? extends Tidsperiode> type) {
        final Set<Tidsperiode> koblingar = this.koblingar.get(type);
        if (koblingar == null) {
            return empty();
        }
        if (koblingar.size() > 1) {
            throw new IllegalStateException(
                    feilmeldingForMeirEnnEiKobling(type, koblingar)
            );
        }
        return koblingar.stream().findFirst();
    }

    /**
     * Hentar ut alle koblingar underlagsperioda har til tidsperioder av den angitte typen.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @return ein straum som inneheld alle dei tilkobla periodene av den angitte typen
     */
    public Stream<Tidsperiode> koblingarAvType(final Class<? extends Tidsperiode> type) {
        final Set<Tidsperiode> koblingar = this.koblingar.get(type);
        if (koblingar == null) {
            return Stream.empty();
        }
        return koblingar.stream();
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
        final Optional<T> verdi = valgfriAnnotasjonFor(type);
        if (!verdi.isPresent()) {
            throw new PaakrevdAnnotasjonManglarException(this, type);
        }
        return verdi.get();
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

    @Override
    public String toString() {
        return "UP[" + fraOgMed + "->" + tilOgMed.map(d -> d.toString()).orElse("") + "]";
    }
}
