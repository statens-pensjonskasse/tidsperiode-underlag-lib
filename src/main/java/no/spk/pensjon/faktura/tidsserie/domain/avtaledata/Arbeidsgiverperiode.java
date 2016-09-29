package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * Holder på en arbeidsgiverid for en gitt periode.
 *
 * @author Snorre E. Brekke - Computas
 */
public class Arbeidsgiverperiode extends AbstractTidsperiode<Arbeidsgiverperiode> implements Arbeidsgiverrelatertperiode<Arbeidsgiverperiode> {

    private final ArbeidsgiverId arbeidsgiverId;

    public Arbeidsgiverperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, ArbeidsgiverId arbeidsgiverId) {
        super(fraOgMed, tilOgMed);
        requireNonNull(arbeidsgiverId, "Arbeidsgiverperiode krever arbeidsgiverId, men aarbeidsgiverId var null.");
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public ArbeidsgiverId arbeidsgiver() {
        return arbeidsgiverId;
    }

    /**
     * Er arbeidsgiverperioden tillknytta den angitte avtalen?
     *
     * @param arbeidsgiver arbeidsgiverid for arbeidsgiver vi skal sjekke opp mot
     * @return <code>true</code> dersom arbeidsgiverperioden er tilknytta den angitte arbeidsgiveren, <code>false</code> ellers
     */
    public boolean tilhoeyrer(ArbeidsgiverId arbeidsgiver) {
        return this.arbeidsgiverId.equals(arbeidsgiver);
    }

    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(ArbeidsgiverId.class, arbeidsgiverId);
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s]", "Arbeidsgiver", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), arbeidsgiverId);
    }
}
