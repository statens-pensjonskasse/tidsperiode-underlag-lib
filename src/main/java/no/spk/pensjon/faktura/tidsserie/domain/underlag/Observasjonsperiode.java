package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * {@link Observasjonsperiode} representerer
 * nedre grense for frå og med-datoen og øvre grense for til og med-datoen til eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}.
 * <p>
 * Hovedintensjonen ved dette konseptet er å kunne avgrense størrelsen fram i tid på underlaget i situasjonar der
 * ei eller fleire av input-periodene til underlaget er løpande og dermed er varig evig langt inn i framtida.
 *
 * @author Tarjei Skorgenes
 */
public class Observasjonsperiode extends AbstractTidsperiode<Observasjonsperiode> {
    /**
     * Konstruerer ei ny grenser.
     *
     * @param fraOgMed nedre grense for frå og med-dato til første underlagsperiode i eit underlag
     * @param tilOgMed øvre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException dersom nokon av datoane er <code>null</code>
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        super(fraOgMed, ofNullable(requireNonNull(tilOgMed, () -> "til og med-dato er påkrevd, men var null")));
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
                        tilOgMed().get().getYear()
                )
                .mapToObj(Aarstall::new)
                .map(Aar::new)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
