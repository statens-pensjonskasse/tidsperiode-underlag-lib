package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * Premie for produktet GRY beregnes ved å multiplisere grunnlaget for GRU med premiesatsen for produktet.
 * Grunnlaget for GRU er antall dager av året et medlem er aktivt, representert som en prosentandel av året.
 * Grunnlaget på periodenivå blir skalert med årsfaktoren til perioden.
 *
 * {@link GrunnlagForGRU}  multipliseres med premiesats for GRU for å gi premie for GRU for en periode.
 *
 * @author Snorre E. Brekke - Computas
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor
 */
public class GrunnlagForGRU {
    private Prosent verdi;

    /**
     * Lager et nytt grunnlag for GRU basert på en prosentsats som representerer antall dager av året et medlem er aktivt,
     * og skal faktureres for GRU.
     * @param verdi antall dager av året et medlem er aktivt, representert som en prosentandel av året.
     */
    public GrunnlagForGRU(Prosent verdi) {
        this.verdi = verdi;
    }

    /**
     * Lager et nytt grunnlag for GRU basert på en prosentsats som representerer antall dager av året et medlem er aktivt,
     * og skal faktureres for GRU.
     * @param verdi antall dager av året et medlem er aktivt, representert som en prosentandel av året.
     * @return et nytt grunnlag for GRU
     */
    public static GrunnlagForGRU grunnlagForGRU(Prosent verdi) {
        return new GrunnlagForGRU(verdi);
    }

    /**
     * Lager et nytt grunnlag for GRU basert på en prosentsats som representerer antall dager av året et medlem er aktivt,
     * og skal faktureres for GRU.
     * @param verdi antall dager av året et medlem er aktivt, representert som en prosentandel av året.
     * @return et nytt grunnlag for GRU
     * @see Prosent#prosent(String)
     */
    public static GrunnlagForGRU grunnlagForGRU(String verdi) {
        return grunnlagForGRU(Prosent.prosent(verdi));
    }

    /**
     * @return prosentverdien av året et medlem er aktivt og skal faktureres for GRU for en periode
     */
    Prosent verdi(){
        return verdi;
    }
}
