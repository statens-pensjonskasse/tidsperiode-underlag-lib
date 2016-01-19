package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * En tidsperiode som gjelder så lenge en avtale er gyldig.
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleperiode extends AbstractTidsperiode<Avtaleperiode> implements Avtalerelatertperiode<Avtaleperiode>  {
    private final AvtaleId avtaleId;
    private final ArbeidsgiverId arbeidsgiverId;
    private final Optional<Ordning> ordning;

    public Avtaleperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, AvtaleId avtaleId, ArbeidsgiverId arbeidsgiverId) {
        this(fraOgMed, tilOgMed, avtaleId, arbeidsgiverId, empty());
    }

    public Avtaleperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, AvtaleId avtaleId, ArbeidsgiverId arbeidsgiverId, Optional<Ordning> ordning) {
        super(fraOgMed, tilOgMed);
        this.avtaleId = requireNonNull(avtaleId, "Avtaleperiode må ha avtaleid, men avtaleid var null");
        this.arbeidsgiverId = requireNonNull(arbeidsgiverId, "Avtaleperiode må ha arbeidsgiverId, men arbeidsgiverId var null");
        this.ordning = requireNonNull(ordning, "Avtaleperiode må ha valgfri ordning, men ordning var null");
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

    public Optional<Ordning> ordning() {
        return ordning;
    }

    /**
     * Oppdaterer avtalebyggarens tilstand til å reflektere kva som er gjeldande ordning for avtalen.
     *
     * @param avtale avtalebyggaren som inneheld avtaletilstanda som skal oppdaterast
     */
    public void populer(final Avtale.AvtaleBuilder avtale) {
        avtale.ordning(ordning);
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s,%s]", "Avtale", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), avtaleId, arbeidsgiverId);
    }
}
