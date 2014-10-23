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
     * Beregninga foretar inga form for avkortning for stillingsprosentar over 100% eller under 0%, prosenten blir brukt as-is.
     *
     * @param stillingsprosent stillingsprosenten som bruttolønna skal justerast i henhold til
     * @return den deltidsjusterte lønna for lønnstrinnbeløpet og stillingsprosenten
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
