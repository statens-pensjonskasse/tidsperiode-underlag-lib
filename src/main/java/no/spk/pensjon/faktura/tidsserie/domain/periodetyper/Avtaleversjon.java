package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link Avtaleversjon} representerer tilstanden til ein avtale innanfor ei bestemt tidsperiode.
 * <p>
 * For fastsats fakturering er det kun premiestatus og premiekategori som er relevant informasjon som kan variere
 * over tid.
 *
 * @author Tarjei Skorgenes
 */
public class Avtaleversjon extends AbstractTidsperiode<Avtaleversjon> {
    private final AvtaleId avtale;
    private final Premiestatus status;

    /**
     * Konstruerer ei ny avtaleversjon som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed første dag i tidsperioda
     * @param tilOgMed viss {@link java.util.Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @throws NullPointerException dersom nokon av parameterverdiane er <code>null</code>
     */
    public Avtaleversjon(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                         final AvtaleId avtale, final Premiestatus status) {
        super(fraOgMed, tilOgMed);
        this.avtale = requireNonNull(avtale, () -> "avtalenummer er påkrevd, men var null");
        this.status = requireNonNull(status, () -> "premiestatus er påkrevd, men var null");
    }

    /**
     * Annoterer underlagsperioda med gjeldande premiestatus for avtalen innanfor perioda.
     *
     * @param periode perioda som skal annoterast med premiestatus
     */
    public void annoter(final Underlagsperiode periode) {
        periode.annoter(Premiestatus.class, status);
    }

    /**
     * Avtalens avtalenummer, denne verdien kan ikkje variere over tid og vil vere den samme for alle
     * avtaleversjonar tilknytta samme avtale.
     *
     * @return avtalenummeret
     */
    public AvtaleId avtale() {
        return avtale;
    }

    /**
     * Er avtaleversjonen tillknytta den angitte avtalen?
     *
     * @param avtale avtalenummeret for avtalen vi skal sjekke opp mot
     * @return <code>true</code> dersom avtaleversjonen er tilknytta den angitte avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final AvtaleId avtale) {
        return this.avtale.equals(avtale);
    }

    @Override
    public String toString() {
        return "avtaleversjon " + fraOgMed() + "->" + tilOgMed.map(LocalDate::toString).orElse("") + " med " + status + " med hash " + Objects.hashCode(this);
    }
}
