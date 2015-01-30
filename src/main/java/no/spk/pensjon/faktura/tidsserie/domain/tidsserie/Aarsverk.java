package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import static java.util.Objects.requireNonNull;

/**
 * {@link Aarsverk} representerer 1 års arbeidsinnsats for eit stillingsforhold som har 100% stillingsprosent
 * gjennom heile året.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsverk {
    /**
     * Konstant som representerer eit 0% eller tomt årsverk.
     */
    public static final Aarsverk ZERO = new Aarsverk(new Prosent(0d));

    private final Prosent verdi;

    /**
     * Opprettar eit nytt årsverk, basert på ei måling angitt i prosent av 1 årsverk.
     *
     * @param verdi antall årsverk angitt i prosent
     * @throws NullPointerException if <code>verdi</code> er <code>null</code>
     */
    public Aarsverk(final Prosent verdi) {
        requireNonNull(verdi, "verdi er påkrevd, men var null");
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
     * Hentar ut antall årsverk som ein prosentverdi sett i forhold til 1 årsverk.
     *
     * @return antall årsverk i prosent
     */
    public Prosent tilProsent() {
        return verdi;
    }

    /**
     * Legger saman verdien av dei to årsverka.
     *
     * @param other årsverket som vi skal legge saman verdien med
     * @return eit nytt årsverk som inneheld summen av dei to årsverk som er lagt saman
     */
    public Aarsverk plus(final Aarsverk other) {
        return new Aarsverk(
                verdi.plus(other.verdi)
        );
    }
}
