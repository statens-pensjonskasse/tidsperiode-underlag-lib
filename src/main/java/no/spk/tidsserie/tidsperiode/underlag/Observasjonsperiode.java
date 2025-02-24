package no.spk.tidsserie.tidsperiode.underlag;

import static java.time.LocalDate.MAX;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import no.spk.tidsserie.tidsperiode.Aar;
import no.spk.tidsserie.tidsperiode.Aarstall;
import no.spk.tidsserie.tidsperiode.AbstractTidsperiode;
import no.spk.tidsserie.tidsperiode.Tidsperiode;

/**
 * {@link Observasjonsperiode} representerer nedre grense for frå og med-datoen og øvre grense for til og med-datoen
 * til underlagsperiodene som inngår i eit {@link Underlag}.
 * <p>
 * Hovedintensjonen ved dette konseptet er å kunne avgrense størrelsen fram i tid på underlaget i situasjonar der
 * ei eller fleire av input-periodene til underlaget er løpande og dermed er varig evig langt inn i framtida.
 *
 * @author Tarjei Skorgenes
 */
public final class Observasjonsperiode extends AbstractTidsperiode<Observasjonsperiode> {
    /**
     * Konstruerer en ny observerasjonsperiode.
     *
     * @param fraOgMed nedre grense for frå og med-dato til første underlagsperiode i eit underlag
     * @param tilOgMed øvre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException dersom nokon av datoane er <code>null</code>
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        this(fraOgMed, of(requireNonNull(tilOgMed, "til og med-dato er påkrevd, men var null")));
    }

    /**
     * Konstruerer en ny observerasjonsperiode.
     *
     * @param fraOgMed nedre grense for frå og med-dato til første underlagsperiode i eit underlag
     * @param tilOgMed øvre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException dersom nokon av datoane er <code>null</code>
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
    }

    /**
     * Returnerer alle år som observasjonsperioda overlappar, enten heilt eller delvis.
     * <p>
     * Ettersom vi her returnerer åra, ikkje månedane perioda overlappar vil det samlinga kunne inneholde fleire
     * månedar enn observasjonsperioda faktisk dekker. Vi har ikkje heilt bestemt oss om kva som er ønska oppførsel
     * i situasjonar der ein ønskjer å observere berre delar av ei år. Skal det i det heile tatt vere tillatt?
     *
     * @return ei samling med alle åra som observasjonsperioda overlappar
     */
    public Collection<Aar> overlappendeAar() {
        return IntStream
                .rangeClosed(
                        fraOgMed().getYear(),
                        tilOgMed().orElseThrow(() -> new IllegalStateException("Å lage Aar for en periode uten ende støttes ikke."))
                                .getYear()
                )
                .mapToObj(Aarstall::new)
                .map(Aar::new)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Genererer ei ny observasjonsperiode som kun dekker den tidsperioda der gjeldande
     * observasjonsperiode overlappar <code>periode</code>.
     *
     * @param periode perioda som alle dagane i den den nye observasjonsperioda skal overlappe
     * @return ei ny observasjonsperiode der alle dagane blir overlapparav <code>periode</code>,
     * {@link Optional#empty()} dersom periodene ikkje overlappar kvarandre
     */
    public Optional<Observasjonsperiode> intersect(final Tidsperiode<?> periode) {
        if (!overlapper(periode)) {
            return empty();
        }
        final LocalDate fraOgMed = periode.fraOgMed();
        final Optional<LocalDate> tilOgMed = periode.tilOgMed();
        return of(
                new Observasjonsperiode(
                        avgrensFraOgMedDato(fraOgMed),
                        avgrensTilOgMedDato(tilOgMed)
                )
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraOgMed(), tilOgMed().orElse(MAX));
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final Observasjonsperiode other)) {
            return false;
        }
        return Objects.equals(fraOgMed(), other.fraOgMed()) && Objects.equals(tilOgMed(), other.tilOgMed());
    }

    @Override
    public String toString() {
        return "observasjonsperiode [" + fraOgMed() + "->" + tilOgMed().map(Objects::toString).orElse("<løpende>") + "]";
    }

    private LocalDate avgrensFraOgMedDato(final LocalDate other) {
        return Stream.of(other, fraOgMed())
                .filter(this::overlapper)
                .max(LocalDate::compareTo)
                .get();
    }

    private Optional<LocalDate> avgrensTilOgMedDato(final Optional<LocalDate> other) {
        return Stream.of(other, tilOgMed())
                .flatMap(Optional::stream)
                .filter(this::overlapper)
                .min(LocalDate::compareTo);
    }
}
