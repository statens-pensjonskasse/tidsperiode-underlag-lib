package no.spk.pensjon.faktura.tidsserie.domain.internal;

import java.text.NumberFormat;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.internal.Aarsfaktor} representerer kor stor andel av eit bestemt
 * �r ei tidsperiode overlappar.
 * <p>
 * �rsfaktoren er avgrensa til kun � kunne dekke perioder fr� 1 til 365 dagar, �rsfaktoren kan ikkje dekke meir enn
 * eit �r om gangen eller mindre enn 0 dagar.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsfaktor {
    private final double verdi;

    /**
     * Konstruerer ein ny �rsfaktor.
     *
     * @param verdi ein desimalverdi som representerer den prosentvise andelen av �ret som �rsfaktoren dekker
     * @throws IllegalArgumentException dersom �rsfaktoren er mindre enn 0 eller st�rre enn 1
     */
    public Aarsfaktor(final double verdi) throws IllegalArgumentException {
        if (verdi <= 0d) {
            throw new IllegalArgumentException("�rsfaktor m� vere st�rre enn 0, men var " + verdi);
        }
        if (verdi > 1d) {
            throw new IllegalArgumentException("�rsfaktor m� vere mindre enn eller lik 1, men var " + verdi);
        }
        this.verdi = verdi;
    }

    @Override
    public String toString() {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(4);
        return format.format(verdi);
    }

    /**
     * Returnerer verdien som representerer �rsfaktoren.
     *
     * @return �rsfaktor-verdien
     */
    double verdi() {
        return verdi;
    }
}
