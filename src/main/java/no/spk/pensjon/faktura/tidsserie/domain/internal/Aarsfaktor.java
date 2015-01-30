package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import java.text.NumberFormat;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.internal.Aarsfaktor} representerer kor stor andel av eit bestemt
 * år ei tidsperiode overlappar.
 * <p>
 * Årsfaktoren er avgrensa til kun å kunne dekke perioder frå 1 til 365 dagar, årsfaktoren kan ikkje dekke meir enn
 * eit år om gangen eller mindre enn 0 dagar.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsfaktor {
    private final Prosent verdi;

    /**
     * Konstruerer ein ny årsfaktor.
     *
     * @param verdi ein desimalverdi som representerer den prosentvise andelen av året som årsfaktoren dekker
     * @throws IllegalArgumentException dersom årsfaktoren er mindre enn 0 eller større enn 1
     */
    public Aarsfaktor(final double verdi) throws IllegalArgumentException {
        if (verdi <= 0d) {
            throw new IllegalArgumentException("årsfaktor må vere større enn 0, men var " + verdi);
        }
        if (verdi > 1d) {
            throw new IllegalArgumentException("årsfaktor må vere mindre enn eller lik 1, men var " + verdi);
        }
        this.verdi = new Prosent(verdi);
    }

    /**
     * Listar ut årsfaktoren avrunda til 4 desimalar.
     * <br>
     * NB: Dette er ein potensielt dyr operasjon då formateringa og avrundinga blir utført via ein ny
     * {@link java.text.NumberFormat} pr kall.
     */
    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(4);
        return format.format(verdi.toDouble());
    }

    /**
     * Returnerer verdien som representerer årsfaktoren.
     * <br>
     * Dette er ein intern implementasjonsdetalj og skal kun benyttast for testing.
     *
     * @return årsfaktor-verdien
     */
    double verdi() {
        return verdi.toDouble();
    }

    /**
     * Multipliserer kronebeløpet med årsfaktoren.
     *
     * @param beloep eit kronebeløp som skal avkortast i henhold til årsfaktoren
     * @return det nye kronebeløpet, avkorta i henhold til årsfaktoren
     * @see Kroner#multiply(double)
     */
    public Kroner multiply(final Kroner beloep) {
        return beloep.multiply(verdi);
    }

    /**
     * Reknar ut antall årsverk basert på gjeldande årsfaktor og den angitte stillingsprosenten.
     *
     * @param deltid gjeldande stillingsprosent for perioda årsfaktoren er beregna for
     * @return antall årsverk
     */
    public Aarsverk multiply(final Prosent deltid) {
        return new Aarsverk(verdi.multiply(deltid));
    }
}
