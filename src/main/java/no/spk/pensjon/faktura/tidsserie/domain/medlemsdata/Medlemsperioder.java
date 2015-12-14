package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * {@link Medlemsperioder} representerer alle perioder og tilstandsovergangar på medlemsnivå
 * basert på alle stillingsforholda til medlemmet.
 * <p>
 * I motsetning til {@link StillingsforholdPerioder} blir medlemsperiodene splitta kvar gang det skjer ein
 * tilstandovergang på eit av medlemmet sine stillingsforhold, uavhengig av kva for eit av stillingsforholda som
 * har endra tilstand.
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsperioder {
    private final List<Medlemsperiode> perioder = new ArrayList<>();

    Medlemsperioder(final Stream<Medlemsperiode> perioder) {
        perioder.forEach(this.perioder::add);
    }

    /**
     * Returnerer medlemsperiodene som periodiseringa har generert.
     * <p>
     * Innanfor kvar periode vil medlemmet si tilstand vere den samme frå dag til dag, ingen endringar i
     * medregning eller historikk vil forekomme innanfor kvar periode. Alle endringar i tilstand for medlemmet
     * vil medføre at ei ny periode blir danna for kvar ny tilstand.
     *
     * @return alle periodene medlemmet strekker seg over
     */
    public Stream<Medlemsperiode> stream() {
        return perioder.stream();
    }

    /**
     * Spørring som hentar ut alle stillingsforholdperioder som tilhøyrer stillingsforholdnummeret <code>id</code>.
     *
     * @param id stillingsforholdnummeret som returnerte perioder må tilhøyre
     * @return alle stillingsforholdperioder som tilhøyrer <code>id</code>, eller
     * {@link Optional#empty()} dersom det ikkje eksisterer nokon stillingsforholdperioder tilknytta <code>id</code>
     */
    public Optional<StillingsforholdPerioder> stillingsforhold(final StillingsforholdId id) {
        final List<StillingsforholdPeriode> stillingsforhold = perioder
                .stream()
                .flatMap(Medlemsperiode::stillingsforhold)
                .filter(p -> p.tilhoeyrer(id))
                .collect(toList());
        if (stillingsforhold.isEmpty()) {
            return empty();
        }
        return of(new StillingsforholdPerioder(id, stillingsforhold));
    }
}
