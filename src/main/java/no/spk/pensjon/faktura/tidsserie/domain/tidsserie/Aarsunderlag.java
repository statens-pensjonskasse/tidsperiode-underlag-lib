package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Aarsunderlag} representerer
 * algoritma for � splitte opp eit stillingsforholdunderlag i eit eller fleire underlag, med eit underlag pr �r.
 * <p>
 * �rsunderlag er p�krevd for � kunne gjere beregningar av maskinelt grunnlag ettersom ein kun skal benytte perioder
 * innanfor eit og samme �r ved � beregne dette.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsunderlag {
    /**
     * Genererer eit nytt �rsunderlag for kvart �rstall som underlagsperioder i <code>underlag</code> er annotert med
     * <p>
     * Ei forutsetning for at genereringa skal fungere er at alle underlagsperioder har ein annotasjon av type
     * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall}. Ei eller fleire perioder utan denne annotasjonen
     * vil f�re til at genereringa feilar.
     *
     * @param underlag eit underlag med perioder tilknytta eit bestemt stillingsforhold
     * @return ein straum av nye underlag der kvart underlag kun inneheld underlagsperioder for eit bestemt �rstall
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
     * Opprettar eit nytt predikat som kan brukast for � sjekke om underlagsperioder
     * er annotert med eit bestemt �rstall.
     *
     * @param aar �rstallet som periodene m� vere annotert med
     * @return eit predikat som matchar alle perioder som er annotert med den angitte verdien for �rstall
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
