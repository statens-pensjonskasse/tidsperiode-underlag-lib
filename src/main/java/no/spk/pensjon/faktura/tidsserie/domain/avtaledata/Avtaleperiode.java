package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * En tidsperiode som gjelder så lenge en avtale er gyldig.
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleperiode extends AbstractTidsperiode<Avtaleperiode> implements Avtalerelatertperiode<Avtaleperiode>  {
    private final AvtaleId avtaleId;
    private final ArbeidsgiverId arbeidsgiverId;

    public Avtaleperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, AvtaleId avtaleId, ArbeidsgiverId arbeidsgiverId) {
        super(fraOgMed, tilOgMed);
        this.avtaleId = avtaleId;
        this.arbeidsgiverId = arbeidsgiverId;
    }

    @Override
    public AvtaleId avtale() {
        return avtaleId;
    }

    public ArbeidsgiverId arbeidsgiverId() {
        return arbeidsgiverId;
    }
}
