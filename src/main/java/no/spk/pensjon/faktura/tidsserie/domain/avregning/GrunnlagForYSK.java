package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;

/**
 * Premie for YSK beregnes ved å multiplisere grunnlaget for YSK med premiesatser for YSK.
 * Grunnlaget for YSK er på antall årsverk medlemmet er aktivt i løpet av et år, avkortet til maksimalt ett årsverk per medlem.
 * Detaljer for hvordan årsverkandelen beregnes bestemmes av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel}.
 * Grunnlaget på periodenivå blir skalert med årsfaktoren til perioden.
 * <p>
 * {@link GrunnlagForYSK} multipliseres med premiesats for YSK for å gi premie for YSK for en periode.
 *
 * @author Snorre E. Brekke - Computas
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor
 */
public class GrunnlagForYSK {
    private static final Prosent MIN_ANDEL = Prosent.ZERO;
    private static final Prosent MAKS_ANDEL = prosent("100%");
    private static final int SAMMENLIGN_DESIMALER = 4;

    private Prosent grunnlag;

    /**
     * Lager et nytt grunnlag for YSK basert på en årsfaktor og faktureringsandel.
     * Grunnlag for YSK beregnes fra {@link FaktureringsandelStatus#andel()} multiplisert med årsfaktor.
     *
     * @param aarsfaktor {@link Aarsfaktor} for perioden grunnlaget skal beregnes for
     * @param faktureringsandel stillingsandel som skal faktureres for YSK i perioden, beregnet av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel}.
     * {@link FaktureringsandelStatus#andel()} må være i intervallet [0%, 100%].
     * @throws IllegalArgumentException dersom {@link FaktureringsandelStatus#andel()} er mindre enn 0% eller større enn 100%.
     */
    public GrunnlagForYSK(Aarsfaktor aarsfaktor, FaktureringsandelStatus faktureringsandel) {
        requireNonNull(aarsfaktor, "aarsfaktor for grunnlag for GRU kan ikke være null");
        requireNonNull(faktureringsandel, "faktureringsandel for grunnlag for GRU kan ikke være null");

        of(faktureringsandel)
                .map(FaktureringsandelStatus::andel)
                .filter(this::storreEllerLik0)
                .filter(this::mindreEllerLik100)
                .orElseThrow(() -> new IllegalArgumentException(
                                "FaktureringsandelStatus#andel() kan ikke være mindre enn 0% eller større enn 100%, " +
                                        "men var " + (faktureringsandel.andel().toDouble() * 100) + "%"
                        )
                );

        this.grunnlag = aarsfaktor.tilProsent().multiply(faktureringsandel.andel());
    }

    /**
     * Lager et nytt grunnlag for YSK basert på en årsfaktor og faktureringsandel.
     * Grunnlag for YSK beregnes fra {@link FaktureringsandelStatus#andel()} multiplisert med årsfaktor.
     *
     * @param aarsfaktor {@link Aarsfaktor} for perioden grunnlaget skal beregnes for
     * @param faktureringsandel stillingsandel som skal faktureres for YSK i perioden, beregnet av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel}.
     * {@link FaktureringsandelStatus#andel()} må være i intervallet [0%, 100%].
     * @throws IllegalArgumentException dersom {@link FaktureringsandelStatus#andel()} er mindre enn 0% eller større enn 100%.
     */
    public static GrunnlagForYSK grunnlagForYSK(Aarsfaktor aarsfaktor, FaktureringsandelStatus faktureringsandel) {
        return new GrunnlagForYSK(aarsfaktor, faktureringsandel);
    }

    private boolean storreEllerLik0(Prosent andel) {
        return andel.toDouble() >= MIN_ANDEL.toDouble() || andel.equals(MIN_ANDEL, SAMMENLIGN_DESIMALER);
    }

    private boolean mindreEllerLik100(Prosent andel) {
        return andel.toDouble() <= MAKS_ANDEL.toDouble() || andel.equals(MAKS_ANDEL, SAMMENLIGN_DESIMALER);
    }

    /**
     * @return prosentverdien av et årsverk et medlem er aktivt og skal faktureres for YSK for en periode
     */
    Prosent verdi() {
        return grunnlag;
    }

}
