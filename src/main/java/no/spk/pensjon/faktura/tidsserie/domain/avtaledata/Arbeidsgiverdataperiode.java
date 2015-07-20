package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;

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
public class Arbeidsgiverdataperiode extends AbstractTidsperiode<Arbeidsgiverdataperiode> implements Arbeidsgiverrelatertperiode<Arbeidsgiverdataperiode> {
    private final Orgnummer orgnummer;
    private final ArbeidsgiverId arbeidsgiverId;

    public Arbeidsgiverdataperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, Orgnummer orgnummer, ArbeidsgiverId arbeidsgiverId) {
        super(fraOgMed, tilOgMed);
        requireNonNull(arbeidsgiverId, "Arbeidsgiverdataperiode krever arbeidsgiverId, men aarbeidsgiverId var null.");
        requireNonNull(orgnummer, "Arbeidsgiverdataperiode krever orgnummer, men orgnummer var null.");
        this.orgnummer = orgnummer;
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public Orgnummer orgnummer() {
        return orgnummer;
    }

    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Orgnummer.class, orgnummer);
    }

    /**
     * Er arbeidsgiverdataperioden tillknytta den angitte avtalen?
     *
     * @param arbeidsgiver arbeidsgiverid for arbeidsgiver vi skal sjekke opp mot
     * @return <code>true</code> dersom arbeidsgiverdataperioden er tilknytta den angitte arbeidsgiveren, <code>false</code> ellers
     */
    public boolean tilhoeyrer(ArbeidsgiverId arbeidsgiver) {
        return this.arbeidsgiverId.equals(arbeidsgiver);
    }

    @Override
    public ArbeidsgiverId arbeidsgiver() {
        return arbeidsgiverId;
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s,%s]", "Arbeidsgiverdata", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), orgnummer, arbeidsgiverId);
    }
}
