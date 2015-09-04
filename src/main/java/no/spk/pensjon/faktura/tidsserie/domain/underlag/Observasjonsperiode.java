package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import static java.time.LocalDate.MAX;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * {@link Observasjonsperiode} representerer
 * nedre grense for fr� og med-datoen og �vre grense for til og med-datoen til eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}.
 * <p>
 * Hovedintensjonen ved dette konseptet er � kunne avgrense st�rrelsen fram i tid p� underlaget i situasjonar der
 * ei eller fleire av input-periodene til underlaget er l�pande og dermed er varig evig langt inn i framtida.
 *
 * @author Tarjei Skorgenes
 */
public final class Observasjonsperiode extends AbstractTidsperiode<Observasjonsperiode> {
    /**
     * Konstruerer ei ny grenser.
     *
     * @param fraOgMed nedre grense for fr� og med-dato til f�rste underlagsperiode i eit underlag
     * @param tilOgMed �vre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException dersom nokon av datoane er <code>null</code>
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        super(fraOgMed, ofNullable(requireNonNull(tilOgMed, () -> "til og med-dato er p�krevd, men var null")));
    }

    /**
     * Returnerer alle �r som observasjonsperioda overlappar, enten heilt eller delvis.
     * <p>
     * Ettersom vi her returnerer �ra, ikkje m�nedane perioda overlappar vil det samlinga kunne inneholde fleire
     * m�nedar enn observasjonsperioda faktisk dekker. Vi har ikkje heilt bestemt oss om kva som er �nska oppf�rsel
     * i situasjonar der ein �nskjer � observere berre delar av ei �r. Skal det i det heile tatt vere tillatt?
     *
     * @return ei samling med alle �ra som observasjonsperioda overlappar
     */
    public Collection<Aar> overlappendeAar() {
        return IntStream
                .rangeClosed(
                        fraOgMed().getYear(),
                        tilOgMed().get().getYear()
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
     * @since 1.1.2
     */
    public Optional<Observasjonsperiode> intersect(final Tidsperiode<?> periode) {
        if (!overlapper(periode)) {
            return empty();
        }
        final LocalDate fraOgMed = periode.fraOgMed();
        final LocalDate tilOgMed = periode.tilOgMed().orElse(MAX);
        return of(
                new Observasjonsperiode(
                        avgrensFraOgMedDato(fraOgMed),
                        avgrensTilOgMedDato(tilOgMed)
                )
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraOgMed(), tilOgMed().get());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Observasjonsperiode)) {
            return false;
        }
        final Observasjonsperiode other = (Observasjonsperiode) obj;
        return Objects.equals(fraOgMed(), other.fraOgMed()) && Objects.equals(tilOgMed(), other.tilOgMed());
    }

    @Override
    public String toString() {
        return "observasjonsperiode [" + fraOgMed() + "->" + tilOgMed().get() + "]";
    }

    private LocalDate avgrensFraOgMedDato(final LocalDate other) {
        return Stream.of(other, fraOgMed())
                .filter(this::overlapper)
                .max(LocalDate::compareTo)
                .get();
    }

    private LocalDate avgrensTilOgMedDato(final LocalDate other) {
        return Stream.of(other, tilOgMed().get())
                .filter(this::overlapper)
                .min(LocalDate::compareTo)
                .get();
    }
}
