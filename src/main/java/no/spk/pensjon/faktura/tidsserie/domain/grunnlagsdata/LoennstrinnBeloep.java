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
     * Beregninga foretar inga form for avkortning for stillingsprosentar over 100% eller under 0%, prosenten blir brukt as-is.
     *
     * @param stillingsprosent stillingsprosenten som bruttol�nna skal justerast i henhold til
     * @return den deltidsjusterte l�nna for l�nnstrinnbel�pet og stillingsprosenten
     */
    public DeltidsjustertLoenn deltidsJuster(final Stillingsprosent stillingsprosent) {
        return new DeltidsjustertLoenn(
                bruttoloenn.multiply(stillingsprosent.prosent)
        );
    }

    @Override
    public String toString() {
        return bruttoloenn + " i  100% stilling";
    }
}
