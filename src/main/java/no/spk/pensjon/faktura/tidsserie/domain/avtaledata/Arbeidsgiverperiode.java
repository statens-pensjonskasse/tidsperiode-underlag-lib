package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * Holder på en arbeidsgiverid for en gitt periode.
 * @author Snorre E. Brekke - Computas
 */
public class Arbeidsgiverperiode extends AbstractTidsperiode<Arbeidsgiverperiode> implements Arbeidsgiverrelatertperiode<Arbeidsgiverperiode>  {

    private final ArbeidsgiverId arbeidsgiverId;

    public Arbeidsgiverperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, ArbeidsgiverId arbeidsgiverId) {
        super(fraOgMed, tilOgMed);
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public ArbeidsgiverId arbeidsgiver() {
        return arbeidsgiverId;
    }

    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(ArbeidsgiverId.class, arbeidsgiverId);
    }
}
