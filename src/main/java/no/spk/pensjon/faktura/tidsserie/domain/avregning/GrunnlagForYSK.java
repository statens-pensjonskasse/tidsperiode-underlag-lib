package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Objects.requireNonNull;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * Premie for YSK beregnes ved å multiplisere grunnlaget for YSK med premiesatser for YSK.
 * Grunnlaget for YSK er på antall årsverk medlemmet er aktivt i løpet av et år, avkortet til maksimalt ett årsverk per medlem.
 * Detaljer for hvordan årsverkandelen beregnes bestemmes av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel}.
 * Grunnlaget på periodenivå blir skalert med årsfaktoren til perioden.
 *
 * {@link GrunnlagForYSK} multipliseres med premiesats for YSK for å gi premie for YSK for en periode.
 *
 * @author Snorre E. Brekke - Computas
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor
 */
public class GrunnlagForYSK {
    private Prosent verdi;

    /**
     * Lager et nytt grunnlag for YSK basert på en prosentsats som representerer antall årsverk et medlem er aktivt,
     * og skal faktureres for YSK.
     * @param verdi antall årsverk et medlem er aktivt, representert som en prosentandel av året.
     */
    public GrunnlagForYSK(Prosent verdi) {
        this.verdi = requireNonNull(verdi, "verdi for grunnlag for YSK kan ikke være null");
    }

    /**
     * Lager et nytt grunnlag for YSK basert på en prosentsats som representerer antall årsverk et medlem er aktivt,
     * og skal faktureres for YSK.
     * @param verdi antall årsverk et medlem er aktivt, representert som en prosentandel av året.
     * @return et nytt grunnlag for YSK
     */
    public static GrunnlagForYSK grunnlagForYSK(Prosent verdi) {
        return new GrunnlagForYSK(verdi);
    }

    /**
     * Lager et nytt grunnlag for YSK basert på en prosentsats som representerer antall årsverk et medlem er aktivt,
     * og skal faktureres for YSK.
     * @param verdi antall årsverk et medlem er aktivt, representert som en prosentandel av året.
     * @return et nytt grunnlag for YSK
     * @see Prosent#prosent(String)
     */
    public static GrunnlagForYSK grunnlagForYSK(String verdi) {
        return grunnlagForYSK(Prosent.prosent(verdi));
    }

    /**
     * @return prosentverdien av et årsverk et medlem er aktivt og skal faktureres for YSK for en periode
     */
    Prosent verdi(){
        return verdi;
    }
}
