package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;

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
        requireNonNull(avtaleId, "Avtaleperiode må ha avtaleid, men avtaleid var null");
        requireNonNull(avtaleId, "Avtaleperiode må ha arbeidsgiverId, men arbeidsgiverId var null");
        this.avtaleId = avtaleId;
        this.arbeidsgiverId = arbeidsgiverId;
    }

    @Override
    public AvtaleId avtale() {
        return avtaleId;
    }

    /**
     * Er avtaleperioden tillknytta den angitte avtalen?
     *
     * @param avtale avtalenummeret for avtalen vi skal sjekke opp mot
     * @return <code>true</code> dersom avtaleperioden er tilknytta den angitte avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(AvtaleId avtale) {
        return this.avtaleId.equals(avtale);
    }

    public ArbeidsgiverId arbeidsgiverId() {
        return arbeidsgiverId;
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s,%s]", "Avtale", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), avtaleId, arbeidsgiverId);
    }
}
