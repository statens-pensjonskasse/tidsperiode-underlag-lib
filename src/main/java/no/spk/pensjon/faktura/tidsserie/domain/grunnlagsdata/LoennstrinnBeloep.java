package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep} representerer hvilket bel�p
 * som er gjeldende for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} innenfor
 * underlagsperioden.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstrinnBeloep {
    private final Kroner bruttoloenn;

    /**
     * Konstruerer eit nytt l�nnstrinnbel�p.
     *
     * @param bruttoloenn bruttol�nna som for ei 100% stilling tilknytta l�nnstrinnet som bel�pet gjeld for
     */
    public LoennstrinnBeloep(final Kroner bruttoloenn) {
        this.bruttoloenn = bruttoloenn;
    }

    /**
     * Deltidsjusterer 100% bruttol�nn i henhold til stillingas stillingsprosent.
     * <br>
     * Beregninga foretar inga form for avkortning for stillingsprosentar over 100% eller under 0%,
     * prosenten blir brukt as-is.
     *
     * @param stillingsprosent stillingsprosenten som bruttol�nna skal justerast i henhold til
     * @return den deltidsjusterte l�nna for l�nnstrinnbel�pet og stillingsprosenten
     */
    public DeltidsjustertLoenn deltidsJuster(final Stillingsprosent stillingsprosent) {
        return new DeltidsjustertLoenn(
                bruttoloenn.multiply(stillingsprosent.prosent)
        );
    }

    /**
     * Returnerer ein hash for l�nnstrinbel�pet.
     *
     * @return ein hash for l�nnstrinbel�pet
     */
    @Override
    public int hashCode() {
        return bruttoloenn.hashCode();
    }

    /**
     * Sjekkar om <code>obj</code> er eit l�nnstrinnbel�p med samme kroneverdi
     * som dette objektet.
     *
     * @param obj det andre objektet som dette objektet skal samanliknast med
     * @return <code>true</code> dersom begge objekta er l�nnstrinnbel�p og dei har samme kroneverdi, <code>false</code> ellers
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
