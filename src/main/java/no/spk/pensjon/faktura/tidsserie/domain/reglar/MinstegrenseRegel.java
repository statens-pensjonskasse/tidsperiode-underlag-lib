package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

public interface MinstegrenseRegel extends BeregningsRegel<Minstegrense> {

    @Override
    public Minstegrense beregn(final Beregningsperiode<?> periode);
}
