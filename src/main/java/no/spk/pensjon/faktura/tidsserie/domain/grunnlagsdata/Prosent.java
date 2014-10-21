package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent} representerer ein prosentsats st�rre enn eller lik
 * 0%.
 *
 * @author Tarjei Skorgenes
 */
public class Prosent {
    /**
     * Konstruerer ein ny prosentsats ut fr� den tekstlige representasjonen av satsen.
     * <p>
     * <code>tekst</code> blir strippa for mellomrom og %-tegn og konvertert direkte til ein prosentsats
     * <br>
     * Prosentsatsen m� bli angitt som eit positivt heiltall, desimalar er ikkje st�tta av denne konstrukt�ren.
     *
     * @param tekst ein <code>String</code> som inneheld ein prosentsats, formatert p� forma <code>123%</code>
     * @throws java.lang.NullPointerException  viss <code>tekst</code> er <code>null</code>
     * @throws java.lang.NumberFormatException viss <code>tekst</code> ikkje kan konverterast til ein prosentsats
     *                                         fordi den inneheld andre tegn enn tall, %-tegn eller mellomrom.
     */
    public Prosent(final String tekst) {
    }
}
