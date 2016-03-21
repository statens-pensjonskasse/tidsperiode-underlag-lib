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
 * @author Snorre E. Brekke - Computas
 */
public class FakturerbareDagsverk {
    private static final MathContext CONTEXT = new MathContext(14, RoundingMode.HALF_UP);
    private static final int ANTALL_DESIMALER = 5;

    private final BigDecimal verdi;

    public FakturerbareDagsverk(final double verdi) {
        this.verdi = tilFemDesimaler(new BigDecimal(verdi));
    }

    public FakturerbareDagsverk(final int verdi) {
        this.verdi = tilFemDesimaler(new BigDecimal(verdi));
    }

    public FakturerbareDagsverk(final BigDecimal verdi) {
        this.verdi = tilFemDesimaler(verdi);
    }

    public BigDecimal verdi() {
        return verdi;
    }

    /**
     * Legger saman verdien av dei to årsverka.
     *
     * @param other årsverket som vi skal legge saman verdien med
     * @return eit nytt årsverk som inneheld summen av dei to årsverk som er lagt saman
     */
    public FakturerbareDagsverk plus(final FakturerbareDagsverk other) {
        return new FakturerbareDagsverk(
                other.verdi.add(verdi, CONTEXT)
        );
    }

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
