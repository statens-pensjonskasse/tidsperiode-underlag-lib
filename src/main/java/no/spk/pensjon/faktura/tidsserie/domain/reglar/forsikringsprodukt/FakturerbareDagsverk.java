package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * {@link FakturerbareDagsverk} er antall dagsverk som skal faktureres for en periode,
 * for YSK eller GRU produktene. Fakturerbare dagsverk er angitt i dager med 5 desimaler.
 * Dagsverkene krever 5 desimaler som følge av at stillingsprosenten kan inneholde opp til 3 desimaler,
 * og er angitt som 100.000.
 * @author Snorre E. Brekke - Computas
 */
public class FakturerbareDagsverk {
    private static final MathContext CONTEXT = new MathContext(14, RoundingMode.HALF_UP);
    private static final int ANTALL_DESIMALER = 5;

    private final BigDecimal verdi;

    /**
     * Oppretter {@link FakturerbareDagsverk} med angitt verdi, avrundet til 5 desimaler.
     * @param verdi antall dagsverk - blir avrundet til 5 desimaler.
     */
    public FakturerbareDagsverk(final double verdi) {
        this.verdi = tilFemDesimaler(new BigDecimal(verdi));
    }

    /**
     * Oppretter {@link FakturerbareDagsverk} med angitt verdi
     * @param verdi antall dagsverk
     */
    public FakturerbareDagsverk(final int verdi) {
        this.verdi = tilFemDesimaler(new BigDecimal(verdi));
    }

    /**
     * Oppretter {@link FakturerbareDagsverk} med angitt verdi, avrundet til 5 desimaler.
     * @param verdi antall dagsverk - blir avrundet til 5 desimaler.
     */
    public FakturerbareDagsverk(final BigDecimal verdi) {
        this.verdi = tilFemDesimaler(verdi);
    }

    /**
     *
     * @return Tallverdien av fakturerbare dagsverk (antall dager), med 5 desimalers presisjon.
     */
    public BigDecimal verdi() {
        return verdi;
    }

    /**
     * Legger saman verdien av dei to dagsverk-verdiene.
     *
     * @param other dagsverk som vi skal legge saman verdien med
     * @return eit nytt dagsverk som inneheld summen av dei to dagsverk som er lagt saman
     */
    public FakturerbareDagsverk plus(final FakturerbareDagsverk other) {
        return new FakturerbareDagsverk(
                other.verdi.add(verdi, CONTEXT)
        );
    }

    /**
     * Multipliserer dette dagsverket med angitt faktor, og returnerer et nytt
     * fakturerbaredagsverk.
     *
     * @param factor faktoren dette fakturerbare dagsverkobjektet skal skaleres med
     * @return eit nytt dagsverk som er produktet av {@link Prosent#toDouble()} / 100 avrundet til 5 desimaler og dette
     * fakturerbare dagsverkobjektet.
     */
    public FakturerbareDagsverk multiply(final Prosent factor) {
        return new FakturerbareDagsverk(
                verdi.multiply(
                        tilFemDesimaler(
                                new BigDecimal(factor.toDouble() / 100d, CONTEXT)
                        )
                )
        );
    }

    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(ANTALL_DESIMALER);
        format.setMinimumFractionDigits(ANTALL_DESIMALER);
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(1);
        return toString(format);
    }

    public String toString(final NumberFormat format) {
        return format.format(verdi.round(CONTEXT)).replaceAll(",", ".");
    }


    private static BigDecimal tilFemDesimaler(final BigDecimal value) {
        return tilDesimaler(value, ANTALL_DESIMALER);
    }

    private static BigDecimal tilDesimaler(final BigDecimal value, final int desimaler) {
        return value.setScale(desimaler, CONTEXT.getRoundingMode());
    }

}
