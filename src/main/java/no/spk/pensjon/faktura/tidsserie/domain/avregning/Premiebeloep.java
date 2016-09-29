package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * Premiebeløp representerer ein årspremieandel for eit produkt
 * som har premiesatsar med opp til to desimalar i satsen.
 * <br>
 * Ved alle beregningar som slår saman to premiebeløp eller multipliserer premiebeløpet opp
 * med ein prosentandel, blir delresultatet avrunda til 2 desimalar etter beregninga er utført.
 * Ved avrunding blir {@link RoundingMode#HALF_EVEN} benytta for å redusere den kumulative
 * feilen ved beregning av årspremieandelar for fleire hundretusen til millionar perioder i ein
 * tidsserie for avregningsformål.
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
     * Opprettar eit nytt premiebeløp lik kr 0,00.
     *
     * @return eit nytt premiebeløp med verdi lik kr 0,00.
     */
    public static Premiebeloep premiebeloep() {
        return ZERO;
    }

    /**
     * Konverterer heltallsverdien av kronebeløpet til et premiebeløp.
     * <br>
     * Premiebeløpet blir utvidet til å inneholde 2 desimaltegn med begge to lik 0.
     *
     * @param verdi heltallsverdien for det nye premiebeløpet
     * @return et nytt premiebeløp med heltallsverdien + 2 desimaltegn lik 0
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
     * Multipliserer grunnlag for pensjonsproduk med den angitte premiesatsen og avrunder til
     * 2 desimaler.
     * <br>
     * Prosentsatsen avrundes til 2 desimaler før den multipliseres med grunnlaget. Dette fordi
     * prosentsatsen forventes å representere en premiesats og premiesatsene skal maksimalt inneholde 2 desimaler.
     * <br>
     * Som en følge av dette vil multiplisering med premiesatser mindre enn eller lik 0.005%, gi et premiebeløp lik
     * kr 0.
     * <br>
     * Grunnlaget behandles som kroner med 0 desimaler.
     *
     * @param grunnlag grunnlaget for premiebeløpet
     * @param premiesats premiesats som grunnlaget skal multipliseres med
     * @return et nytt premiebeløp
     */
    public static Premiebeloep premiebeloep(final GrunnlagForPensjonsprodukt grunnlag, final Prosent premiesats) {
        return new Premiebeloep(
                tilToDesimaler(
                        new BigDecimal(
                                grunnlag.kroneverdi().verdi(),
                                CONTEXT
                        )
                                .multiply(
                                        tilDesimaler(
                                                new BigDecimal(
                                                        premiesats.toDouble(),
                                                        CONTEXT
                                                ),
                                                4 // 100.00% blir representert som 1.0000, ergo 4-desimaler
                                        )
                                )
                )
        );
    }

    /**
     * Premiebeløp for YSK på underlagsperioder skal alltid være 0,-.
     * <br>
     * Premie for YSK beregnes kun på avtalenivå utifra grunnlag for GRU.
     *
     * @param grunnlag grunnlaget for premiebeløpet
     * @param premiesats premiesats som grunnlaget skal multipliseres med
     * @return et nytt premiebeløp på 0,- kr.
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static Premiebeloep premiebeloep(final GrunnlagForYSK grunnlag, final Kroner premiesats) {
        return new Premiebeloep(new BigDecimal(0));
    }

    /**
     * Premiebeløp for GRU på underlagsperioder skal alltid være 0,-.
     * <br>
     * Premie for GRU beregnes kun på avtalenivå utifra grunnlag for GRU.
     *
     * @param grunnlag grunnlaget for premiebeløpet
     * @param premiesats premiesats som grunnlaget skal multipliseres med
     * @return et nytt premiebeløp på 0,- kr.
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static Premiebeloep premiebeloep(final GrunnlagForGRU grunnlag, final Kroner premiesats) {
        return new Premiebeloep(new BigDecimal(0));
    }

    /**
     * Konverterer <code>text</code> til et premiebeløp med 2 desimaltegn.
     * <br>
     * Desimalverdien fra <code>text</code> blir med over i premiebeløpet og premiebeløpet
     * avrundes til 2 desimaler dersom inputen inneholder mer enn 2 desimaler.
     * <br>
     * Konverteringen ignorerer alle mellomrom, kr-prefix eller -postfix. Teksten kan inneholde opp til
     * 1 norsk eller 1 engelsk desimalseparator, men ingen andre separatorer for heltallsdelen av teksten
     * (utenom mellomrom) er støttet.
     *
     * @param text teksten som skal konverteres til et premiebeløp angitt i norske kroner med 2 desimaler
     * @return det nye premiebeløpet avrundet til 2 desimaler
     * @throws IllegalArgumentException dersom <code>text</code> ikke kan konverteres til et premiebeløp angitt i norske kroner
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
     * Legger sammen gjeldende premiebeløp med <code>other</code> og runder av resultatet til
     * 2 desimaler.
     *
     * @param other det andre premiebeløpet som vi skal legges sammen med
     * @return et nytt premiebeløp med summen av de to premiebeløpene, avrundet til 2 desimaler
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
     * Multipliserer gjeldende premiebeløp med den angitte prosentsatsen og avrunder til
     * 2 desimaler.
     * <br>
     * Prosentsatsen avrundes til 2 desimaler før den multipliseres med det gjeldende premiebeløpet. Dette fordi
     * prosentsatsen forventes å representere en premiesats og premiesatsene skal maksimalt inneholde 2 desimaler.
     * <br>
     * Som en følge av dette vil multiplisering med premiesatser mindre enn eller lik 0.005%, gi et premiebeløp lik
     * kr 0.
     *
     * @param other prosentsatsen som vi skal legges sammen med, maksimalt 4 desimaler støttes
     * @return et nytt premiebeløp med resultatet av multiplikasjonen
     * @see Premiebeloep#premiebeloep(GrunnlagForPensjonsprodukt, Prosent)
     * @see Premiebeloep#premiebeloep(GrunnlagForGRU, Kroner)
     * @see Premiebeloep#premiebeloep(GrunnlagForYSK, Kroner)
     * @deprecated since 2.1.1 - Premiebeloep skal representere et ferdig beregnet premiebeloep, og skal ikke multiploseres med noe
     * etter konstruksjon
     */
    @Deprecated
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
     * @return antall desimaler i premiebeløpet
     */
    int desimaler() {
        return value.scale();
    }
}
