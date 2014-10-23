package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent} representerer ein andel av ei
 * fulltidsstilling målt i prosent.
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsprosent {
    public final Prosent prosent;

    /**
     * Konstruerer ein ny stillingsprosent.
     *
     * @param verdi ein positiv prosentsats
     * @throws NullPointerException viss <code>verdi</code> er <code>null</code>
     */
    public Stillingsprosent(final Prosent verdi) throws NullPointerException {
        prosent = verdi;
    }

    @Override
    public String toString() {
        return prosent + " stilling";
    }
}
