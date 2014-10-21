package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.time.LocalDate;
import java.util.Optional;

/**
 * {@link StillingsforholdPeriode} representerer ei periode der det ikkje skjer nokon endringar på eit bestemt
 * stillingsforhold.
 * <p>
 * Periodene kan bli bygd opp enten basert på ei medregningsperiode, eller som ei periode mellom to endringar i
 * stillingshistorikken tilknytta stillingsforholdet.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPeriode {
    private final LocalDate fraOgMed;
    private final Optional<LocalDate> tilOgMed;

    /**
     * Konstruerer ei ny periode for eit stillingsforhold.
     *
     * @param fraOgMed aksjonsdatoen stillingsforholdet endrar tilstand
     * @param tilOgMed dagen før neste endring i tilstanden til stillingsforholdet, eller stillingsforholdets sluttdato
     *                 viss perioda representerer siste periode stillingsforholdet er aktivt før det blir sluttmeldt
     */
    public StillingsforholdPeriode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        this.fraOgMed = fraOgMed;
        this.tilOgMed = tilOgMed;
    }

    public LocalDate fraOgMed() {
        return fraOgMed;
    }

    public Optional<LocalDate> tilOgMed() {
        return tilOgMed;
    }
}
