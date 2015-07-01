package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * Rolle for tidsperioder som er tilknyttet en spesiell arbeidsgiver
 *
 * @author Snorre E. Brekke - Computas
 */
public interface Arbeidsgiverrelatertperiode<T extends Tidsperiode<T>> extends Tidsperiode<T> {
    ArbeidsgiverId arbeidsgiver();
}
