package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link MinstegrenseRegelVersjon2} implementerer ny minstegrense gjeldende fom 2016-01-01 (SPKMASTER-11812)
 * <p>
 */
public class MinstegrenseRegelVersjon2 implements BeregningsRegel<Minstegrense> {

    private static final Minstegrense MINSTEGRENSE_20_PROSENT = new Minstegrense(new Prosent("20%"));

    @Override
    public Minstegrense beregn(Beregningsperiode<?> periode) {
        return MINSTEGRENSE_20_PROSENT;
    }
}
