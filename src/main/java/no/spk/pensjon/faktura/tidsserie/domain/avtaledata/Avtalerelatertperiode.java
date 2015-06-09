package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * Rolle for tidsperioder som er tilknytta ein spesifikk avtale.
 *
 * @author Tarjei Skorgenes
 */
public interface Avtalerelatertperiode<T extends Tidsperiode<T>> extends Tidsperiode<T> {
    /**
     * Avtalen perioda er tilknytta.
     *
     * @return avtalenummeret
     */
    AvtaleId avtale();
}
