package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import java.util.List;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;

/**
 * Premie for produktet GRY beregnes ved å multiplisere grunnlaget for GRU med premiesatsen for produktet.
 * Grunnlaget for GRU er antall dager av året et medlem er aktivt, representert som en prosentandel av året.
 * Detaljer for hvordan prosentandelen beregnes bestemmes av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel}.
 * Grunnlaget på periodenivå blir skalert med årsfaktoren til perioden.
 * <p>
 * {@link GrunnlagForGRU#verdi()} multipliseres med premiesats for GRU for å gi premie for GRU for en periode.
 *
 * @author Snorre E. Brekke - Computas
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor
 */
public class GrunnlagForGRU {
    private static final List<Prosent> LOVLIG_FAKTURERINGSANDEL = asList(Prosent.ZERO, prosent("100%"));
    private static final int SAMMENLIGN_DESIMALER = 4;

    private final Prosent grunnlag;

    /**
     * Lager et nytt grunnlag for GRU basert på en årsfaktor og faktureringsandel.
     * Grunnlag for GRU beregnes fra {@link FaktureringsandelStatus#andel()} multiplisert med årsfaktor.
     *
     * @param aarsfaktor {@link Aarsfaktor} for perioden grunnlaget skal beregnes for
     * @param faktureringsandel stillingsandel som skal faktureres for GRU i perioden, beregnet av  {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel}.
     * {@link FaktureringsandelStatus#andel()} må være 0% eller 100%.
     * @throws IllegalArgumentException dersom {@link FaktureringsandelStatus#andel()} ikke er 0% eller 100%.
     */
    public GrunnlagForGRU(Aarsfaktor aarsfaktor, FaktureringsandelStatus faktureringsandel) {
        requireNonNull(aarsfaktor, "aarsfaktor for grunnlag for GRU kan ikke være null");
        requireNonNull(faktureringsandel, "faktureringsandel for grunnlag for GRU kan ikke være null");
        LOVLIG_FAKTURERINGSANDEL
                .stream()
                .filter(a -> a.equals(faktureringsandel.andel(), SAMMENLIGN_DESIMALER))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                                "FaktureringsandelStatus#andel() må være 0% eller 100%, men var " +
                                        (faktureringsandel.andel().toDouble() * 100) + "%"
                        )
                );

        this.grunnlag = aarsfaktor.tilProsent().multiply(faktureringsandel.andel());
    }

    /**
     * Lager et nytt grunnlag for GRU basert på en årsfaktor og faktureringsandel.
     * Grunnlag for GRU beregnes fra {@link FaktureringsandelStatus#andel()} multiplisert med årsfaktor.
     *
     * @param aarsfaktor {@link Aarsfaktor} for perioden grunnlaget skal beregnes for
     * @param faktureringsandel stillingsandel som skal faktureres for GRU i perioden, beregnet av  {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel}.
     * {@link FaktureringsandelStatus#andel()} må være 0% eller 100%.
     * @return et nytt grunnlag for GRU
     * @throws IllegalArgumentException dersom {@link FaktureringsandelStatus#andel()} ikke er 0% eller 100%.
     */
    public static GrunnlagForGRU grunnlagForGRU(Aarsfaktor aarsfaktor, FaktureringsandelStatus faktureringsandel) {
        return new GrunnlagForGRU(aarsfaktor, faktureringsandel);
    }

    /**
     * @return prosentverdien av året et medlem er aktivt og skal faktureres for GRU for en periode
     */
    Prosent verdi() {
        return grunnlag;
    }
}
