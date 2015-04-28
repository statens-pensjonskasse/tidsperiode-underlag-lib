package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StillingsforholdunderlagFactory;

/**
 * {@link AvtaleinformasjonRepository} representerer eit repository for oppslag av avtalerelatert informasjon
 * som kan variere over tid.
 *
 * @author Tarjei Skorgenes
 */
public interface AvtaleinformasjonRepository {
    /**
     * Slår opp all tidsperiodisert informasjon som er relevant for tidsseriegenereringa for ein bestemt avtale.
     *
     * @param avtale avtalen det skal slåast opp tidsperiodisert avtaleinformasjon om
     * @return ein straum med all avtalerelatert informasjon tilknytta <code>avtale</code>
     */
    Stream<Tidsperiode<?>> finn(final AvtaleId avtale);
}
