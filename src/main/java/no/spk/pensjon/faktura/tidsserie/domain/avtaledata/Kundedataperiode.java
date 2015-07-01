package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Orgnummer;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * Kundedataperiode holder på {@link Orgnummer} som en arbeidsgiver med gitt {@link ArbeidsgiverId} har i en gitt periode.
 * @author Snorre E. Brekke - Computas
 */
public class Kundedataperiode extends AbstractTidsperiode<Kundedataperiode> implements Arbeidsgiverrelatertperiode<Kundedataperiode> {
    private final Orgnummer orgnummer;
    private final ArbeidsgiverId arbeidsgiverId;

    public Kundedataperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, Orgnummer orgnummer, ArbeidsgiverId arbeidsgiverId) {
        super(fraOgMed, tilOgMed);
        this.orgnummer = orgnummer;
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public Orgnummer orgnummer() {
        return orgnummer;
    }

    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Orgnummer.class, orgnummer);
    }

    @Override
    public ArbeidsgiverId arbeidsgiver() {
        return arbeidsgiverId;
    }
}
