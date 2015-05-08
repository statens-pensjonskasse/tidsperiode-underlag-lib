package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.Objects;

/**
 * Representerer premiestatser oppgitt i type T knyttet til et avtaleprodukt. <br> Typisk er dette Kroner og Prosent.
 * Et avtaleprodukt som har kronesatser kan ikke ha prosentsatser. <br>
 * Det er regel-/kontekst-avhengig hvordan satsene brukes, og hva de multipliseres med.
 *  @param <T> Typen sats - typisk Kroner eller Prosent.
 * @see Kroner
 * @see Prosent
 *
 * @author Snorre E. Brekke - Computas
 */
public class Satser<T> {
    private final T arbeidsgiverpremie;
    private final T medlemspremie;
    private final T administrasjonsgebyr;

    public Satser(T arbeidsgiverpremie, T medlemspremie, T administrasjonsgebyr) {
        this.arbeidsgiverpremie = Objects.requireNonNull(arbeidsgiverpremie);
        this.medlemspremie = Objects.requireNonNull(medlemspremie);
        this.administrasjonsgebyr = Objects.requireNonNull(administrasjonsgebyr);
    }

    public T arbeidsgiverpremie() {
        return arbeidsgiverpremie;
    }

    public T medlemspremie() {
        return medlemspremie;
    }

    public T administrasjonsgebyr() {
        return administrasjonsgebyr;
    }
}
