package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link Medlemsperiode} representerer ei tidsperiode der det ikkje skjer nokon lønns- eller stillingsrelaterte
 * endringar på medlemmet.
 * <p>
 * Periodene blir bygd opp basert på alle stillingsforhold- og medregningsperioder tilknytta medlemmet.
 * <p>
 * Merk at i motsetning til {@link StillingsforholdPeriode} kan ei medlemsperiode indikere at medlemmet ikkje er aktivt,
 * det vil bli danna medlemsperioder også for periodene der medlemmet ikkje har ei aktiv stilling hos arbeidsgivarar
 * tilknytta nokon av ordninga som blir administrert av Statens Pensjonskasse.
 * <p>
 * Ved hjelp av medlemsperioda sine overlappande {@link #stillingsforhold} kan ein gjere utrekningar som utledar ting
 * som antall parallelle stillingsforhold og antall dagar med slike pr medlem, ein kan rekne ut total stillingsprosent
 * eller total lønn. Det blir forventa at dette kan bli brukt for å implementere meir presis handtering av øvre og
 * nedre lønnsgrenser (minstegrense/10G/12G) og liknande.
 *
 * @author Tarjei Skorgenes
 */
public class Medlemsperiode extends AbstractTidsperiode<Medlemsperiode> {
    private final List<StillingsforholdPeriode> stillingsforhold = new ArrayList<>(1);

    /**
     * Konstruerer ei ny tidsperiode som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed første dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     */
    public Medlemsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
    }

    /**
     * Returnerer alle stillingsforhola som er aktive for medlemmet innanfor den aktuelle perioda.
     *
     * @return alle aktive stillingsforhold for medlemmet innanfor den aktuelle tidsperioda
     */
    public Stream<StillingsforholdPeriode> stillingsforhold() {
        return stillingsforhold.stream();
    }

    /**
     * Koblar saman medlemsperioda med alle stillingsforholdperioder som den blir enten heilt eller delvis overlappa av.
     *
     * @param perioder stillingsforholdsperioder som overlappar medlemsperioda
     * @return <code>this</code>
     */
    public Medlemsperiode kobleTil(final Stream<StillingsforholdPeriode> perioder) {
        perioder.forEach(stillingsforhold::add);
        return this;
    }

    @Override
    public String toString() {
        return "MP[" + fraOgMed + "," + tilOgMed().map(Object::toString).orElse("->") + "]";
    }
}
