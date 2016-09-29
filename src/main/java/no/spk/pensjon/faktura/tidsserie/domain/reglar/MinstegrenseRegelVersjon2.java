package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link MinstegrenseRegelVersjon2} implementerer ny minstegrense gjeldende fom 2016-01-01 (SPKMASTER-11812)
 */
public class MinstegrenseRegelVersjon2 implements MinstegrenseRegel {

    private static final Minstegrense MINSTEGRENSE_20_PROSENT = new Minstegrense(new Prosent("20%"));
    private static final Minstegrense MINSTEGRENSE_50_PROSENT = new Minstegrense(new Prosent("50%"));

    @Override
    public Minstegrense beregn(Beregningsperiode<?> periode) {
        final Ordning ordning = periode.annotasjonFor(Ordning.class);
        if (Ordning.OPERA.equals(ordning)) {
            return MINSTEGRENSE_50_PROSENT;
        } else if (Ordning.SPK.equals(ordning) || Ordning.POA.equals(ordning)) {
            return MINSTEGRENSE_20_PROSENT;
        }
        throw new IllegalStateException("Minstegrense er ikkje definert for " + ordning + ", minstegrensereglane er kun definert for SPK-, POA- og Opera-ordningane");

    }
}
