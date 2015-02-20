package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Aarsunderlag} representerer
 * algoritma for å splitte opp eit stillingsforholdunderlag i eit eller fleire underlag, med eit underlag pr år.
 * <p>
 * Årsunderlag er påkrevd for å kunne gjere beregningar av maskinelt grunnlag ettersom ein kun skal benytte perioder
 * innanfor eit og samme år ved å beregne dette.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsunderlag {
    /**
     * Genererer eit nytt årsunderlag for kvart årstall som underlagsperioder i <code>underlag</code> er annotert med
     * <p>
     * Ei forutsetning for at genereringa skal fungere er at alle underlagsperioder har ein annotasjon av type
     * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall}. Ei eller fleire perioder utan denne annotasjonen
     * vil føre til at genereringa feilar.
     *
     * @param underlag eit underlag med perioder tilknytta eit bestemt stillingsforhold
     * @return ein straum av nye underlag der kvart underlag kun inneheld underlagsperioder for eit bestemt årstall
     */
    public Stream<Underlag> genererUnderlagPrAar(final Underlag underlag) {
        final Function<Aarstall, Underlag> nyttUnderlagForAarstall = aar -> underlag
                .restrict(annotertMedAarstall(aar))
                .annoter(Aarstall.class, aar);
        return underlag
                .stream()
                .map(Aarsunderlag::aarstallForPeriode)
                .distinct()
                .map(nyttUnderlagForAarstall);

    }

    /**
     * Opprettar eit nytt predikat som kan brukast for å sjekke om underlagsperioder
     * er annotert med eit bestemt årstall.
     *
     * @param aar årstallet som periodene må vere annotert med
     * @return eit predikat som matchar alle perioder som er annotert med den angitte verdien for årstall
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#annotasjonFor(Class)
     */
    private static Predicate<Underlagsperiode> annotertMedAarstall(final Aarstall aar) {
        return (Underlagsperiode p) -> aarstallForPeriode(p).equals(aar);
    }

    /**
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#annotasjonFor(Class)
     * @see no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall
     */
    private static Aarstall aarstallForPeriode(final Underlagsperiode periode) {
        return periode.annotasjonFor(Aarstall.class);
    }
}
