package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link MinstegrenseRegel} interface for reglene som bestemmer hva som er minste stillingsstørrelse som skal kunne faktureres.
 * {@link MinstegrenseRegelVersjon1} implementerer regelen som gjelder for tidsperioden tom 2015.12.31
 * {@link MinstegrenseRegelVersjon2} implementerer regelen som gjelder for tidsperioden fom 2016.01.01
 */
public interface MinstegrenseRegel extends BeregningsRegel<Minstegrense> {

    @Override
    public Minstegrense beregn(final Beregningsperiode<?> periode);
}
