package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;

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

    /**
     * Listar ut �rsfaktoren avrunda til 4 desimalar.
     * <br>
     * NB: Dette er ein potensielt dyr operasjon d� formateringa og avrundinga blir utf�rt via ein ny
     * {@link java.text.NumberFormat} pr kall.
     */
    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(4);
        return format.format(verdi);
    }

    /**
     * Returnerer verdien som representerer �rsfaktoren.
     * <br>
     * Dette er ein intern implementasjonsdetalj og skal kun benyttast for testing.
     *
     * @return �rsfaktor-verdien
     */
    double verdi() {
        return verdi;
    }

    /**
     * Multipliserer kronebel�pet med �rsfaktoren.
     *
     * @param beloep eit kronebel�p som skal avkortast i henhold til �rsfaktoren
     * @return det nye kronebel�pet, avkorta i henhold til �rsfaktoren
     * @see Kroner#multiply(double)
     */
    public Kroner multiply(final Kroner beloep) {
        return beloep.multiply(verdi);
    }
}
