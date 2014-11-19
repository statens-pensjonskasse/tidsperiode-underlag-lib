package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep} representerer hvilket beløp
 * som er gjeldende for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} innenfor
 * underlagsperioden.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstrinnBeloep {
    private final Kroner bruttoloenn;

    /**
     * Konstruerer eit nytt lønnstrinnbeløp.
     *
     * @param bruttoloenn bruttolønna som for ei 100% stilling tilknytta lønnstrinnet som beløpet gjeld for
     */
    public LoennstrinnBeloep(final Kroner bruttoloenn) {
        this.bruttoloenn = bruttoloenn;
    }

    /**
     * Deltidsjusterer 100% bruttolønn i henhold til stillingas stillingsprosent.
     * <br>
     * Beregninga foretar inga form for avkortning for stillingsprosentar over 100% eller under 0%,
     * prosenten blir brukt as-is.
     *
     * @param stillingsprosent stillingsprosenten som bruttolønna skal justerast i henhold til
     * @return den deltidsjusterte lønna for lønnstrinnbeløpet og stillingsprosenten
     */
    public DeltidsjustertLoenn deltidsJuster(final Stillingsprosent stillingsprosent) {
        return new DeltidsjustertLoenn(
                bruttoloenn.multiply(stillingsprosent.prosent)
        );
    }

    /**
     * Returnerer ein hash for lønnstrinbeløpet.
     *
     * @return ein hash for lønnstrinbeløpet
     */
    @Override
    public int hashCode() {
        return bruttoloenn.hashCode();
    }

    /**
     * Sjekkar om <code>obj</code> er eit lønnstrinnbeløp med samme kroneverdi
     * som dette objektet.
     *
     * @param obj det andre objektet som dette objektet skal samanliknast med
     * @return <code>true</code> dersom begge objekta er lønnstrinnbeløp og dei har samme kroneverdi, <code>false</code> ellers
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoennstrinnBeloep other = (LoennstrinnBeloep) obj;
        return bruttoloenn.equals(other.bruttoloenn);
    }

    @Override
    public String toString() {
        return bruttoloenn + " i 100% stilling";
    }
}
