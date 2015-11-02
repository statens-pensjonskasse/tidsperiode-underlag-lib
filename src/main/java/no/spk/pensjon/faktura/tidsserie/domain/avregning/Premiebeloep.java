package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Premiebel�p representerer ein �rspremieandel for eit produkt
 * som har premiesatsar med opp til to desimalar i satsen.
 * <br>
 * Ved alle beregningar som sl�r saman to premiebel�p eller multipliserer premiebel�pet opp
 * med ein prosentandel, blir delresultatet avrunda til 2 desimalar etter beregninga er utf�rt.
 * Ved avrunding blir {@link RoundingMode#HALF_EVEN} benytta for � redusere den kumulative
 * feilen ved beregning av �rspremieandelar for fleire hundretusen til millionar perioder i ein
 * tidsserie for avregningsform�l.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public final class Premiebeloep {
    private static final MathContext CONTEXT = new MathContext(14, RoundingMode.HALF_UP);
    private static final Premiebeloep ZERO = new Premiebeloep(tilToDesimaler(BigDecimal.ZERO));

    private final BigDecimal value;

    private Premiebeloep(final BigDecimal value) {
        this.value = value;
    }

    /**
     * Opprettar eit nytt premiebel�p lik kr 0,00.
     *
     * @return eit nytt premiebel�p med verdi lik kr 0,00.
     */
    public static Premiebeloep premiebeloep() {
        return ZERO;
    }

    /**
     * Konverterer heltallsverdien av kronebel�pet til et premiebel�p.
     * <br>
     * Premiebel�pet blir utvidet til � inneholde 2 desimaltegn med begge to lik 0.
     *
     * @param verdi heltallsverdien for det nye premiebel�pet
     * @return et nytt premiebel�p med heltallsverdien + 2 desimaltegn lik 0
     */
    public static Premiebeloep premiebeloep(final Kroner verdi) {
        return new Premiebeloep(
                tilToDesimaler(
                        new BigDecimal(
                                verdi.verdi()
                        )
                )
        );
    }

    /**
     * Konverterer <code>text</code> til et premiebel�p med 2 desimaltegn.
     * <br>
     * Desimalverdien fra <code>text</code> blir med over i premiebel�pet og premiebel�pet
     * avrundes til 2 desimaler dersom inputen inneholder mer enn 2 desimaler.
     * <br>
     * Konverteringen ignorerer alle mellomrom, kr-prefix eller -postfix. Teksten kan inneholde opp til
     * 1 norsk eller 1 engelsk desimalseparator, men ingen andre separatorer for heltallsdelen av teksten
     * (utenom mellomrom) er st�ttet.
     *
     * @param text teksten som skal konverteres til et premiebel�p angitt i norske kroner med 2 desimaler
     * @return det nye premiebel�pet avrundet til 2 desimaler
     * @throws IllegalArgumentException dersom <code>text</code> ikke kan konverteres til et premiebel�p angitt i norske kroner
     */
    public static Premiebeloep premiebeloep(final String text) {
        if (!text.matches("^(kr )?-?[0-9 ]+([,\\.][0-9]{1,})?( kr)?$")) {
            throw new IllegalArgumentException(
                    text
                            + " er ikkje eit gyldig heil- eller desimaltall med maksimalt 2 desimalar"
            );
        }

        return new Premiebeloep(
                tilToDesimaler(
                        new BigDecimal(
                                text
                                        .replaceAll(",-", "")
                                        .replaceAll("kr", "")
                                        .replaceAll(",", ".")
                                        .replaceAll(" ", "")
                        ))
        );
    }

    /**
     * Legger sammen gjeldende premiebel�p med <code>other</code> og runder av resultatet til
     * 2 desimaler.
     *
     * @param other det andre premiebel�pet som vi skal legges sammen med
     * @return et nytt premiebel�p med summen av de to premiebel�pene, avrundet til 2 desimaler
     */
    public Premiebeloep plus(final Premiebeloep other) {
        return new Premiebeloep(
                tilToDesimaler(
                        this.value.add(
                                other.value,
                                CONTEXT
                        )
                )
        );
    }

    /**
     * Multipliserer gjeldende premiebel�p med den angitte prosentsatsen og avrunder til
     * 2 desimaler.
     * <br>
     * Prosentsatsen avrundes til 2 desimaler f�r den multipliseres med det gjeldende premiebel�pet. Dette fordi
     * prosentsatsen forventes � representere en premiesats og premiesatsene skal maksimalt inneholde 2 desimaler.
     * <br>
     * Som en f�lge av dette vil multiplisering med premiesatser mindre enn eller lik 0.005%, gi et premiebel�p lik
     * kr 0.
     *
     * @param other prosentsatsen som vi skal legges sammen med, maksimalt 4 desimaler st�ttes
     * @return et nytt premiebel�p med resultatet av multiplikasjonen
     */
    public Premiebeloep multiply(final Prosent other) {
        return new Premiebeloep(
                tilToDesimaler(
                        value.multiply(
                                tilDesimaler(
                                        new BigDecimal(
                                                other.toDouble(),
                                                CONTEXT
                                        ),
                                        4 // 100.00% blir representert som 1.0000, ergo 4-desimaler
                                )
                        )
                )
        );
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return toString().equals(obj.toString());
    }


    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(1);
        return toString(format);
    }

    public String toString(final NumberFormat format) {
        return format.format(value.round(CONTEXT)).replaceAll(",", ".");
    }

    private static BigDecimal tilToDesimaler(final BigDecimal value) {
        return tilDesimaler(value, 2);
    }

    private static BigDecimal tilDesimaler(final BigDecimal value, final int desimaler) {
        return value.setScale(desimaler, CONTEXT.getRoundingMode());
    }

    /**
     * For test-usage only.
     *
     * @return antall desimaler i premiebel�pet
     */
    int desimaler() {
        return value.scale();
    }
}
