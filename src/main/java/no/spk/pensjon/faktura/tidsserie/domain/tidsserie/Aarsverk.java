package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import static java.util.Objects.requireNonNull;

/**
 * {@link Aarsverk} representerer 1 �rs arbeidsinnsats for eit stillingsforhold som har 100% stillingsprosent
 * gjennom heile �ret.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsverk {
    /**
     * Konstant som representerer eit 0% eller tomt �rsverk.
     */
    public static final Aarsverk ZERO = new Aarsverk(new Prosent(0d));

    private final Prosent verdi;

    /**
     * Opprettar eit nytt �rsverk, basert p� ei m�ling angitt i prosent av 1 �rsverk.
     *
     * @param verdi antall �rsverk angitt i prosent
     * @throws NullPointerException if <code>verdi</code> er <code>null</code>
     */
    public Aarsverk(final Prosent verdi) {
        requireNonNull(verdi, "verdi er p�krevd, men var null");
        this.verdi = verdi;
    }

    /**
     * @see no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent#Prosent(String)
     * @see #Aarsverk(no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent)
     */
    public static Aarsverk aarsverk(final Prosent verdi) {
        return new Aarsverk(verdi);
    }

    /**
     * Hentar ut antall �rsverk som ein prosentverdi sett i forhold til 1 �rsverk.
     *
     * @return antall �rsverk i prosent
     */
    public Prosent tilProsent() {
        return verdi;
    }

    /**
     * Legger saman verdien av dei to �rsverka.
     *
     * @param other �rsverket som vi skal legge saman verdien med
     * @return eit nytt �rsverk som inneheld summen av dei to �rsverk som er lagt saman
     */
    public Aarsverk plus(final Aarsverk other) {
        return new Aarsverk(
                verdi.plus(other.verdi)
        );
    }
}
