package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent} representerer ein prosentsats større enn eller lik
 * 0%.
 *
 * @author Tarjei Skorgenes
 */
public class Prosent {
    /**
     * Konstruerer ein ny prosentsats ut frå den tekstlige representasjonen av satsen.
     * <p>
     * <code>tekst</code> blir strippa for mellomrom og %-tegn og konvertert direkte til ein prosentsats
     * <br>
     * Prosentsatsen må bli angitt som eit positivt heiltall, desimalar er ikkje støtta av denne konstruktøren.
     *
     * @param tekst ein <code>String</code> som inneheld ein prosentsats, formatert på forma <code>123%</code>
     * @throws java.lang.NullPointerException  viss <code>tekst</code> er <code>null</code>
     * @throws java.lang.NumberFormatException viss <code>tekst</code> ikkje kan konverterast til ein prosentsats
     *                                         fordi den inneheld andre tegn enn tall, %-tegn eller mellomrom.
     */
    public Prosent(final String tekst) {
    }
}
