package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Representerer premiestatser oppgitt i type T knyttet til et avtaleprodukt. <br> Typisk er dette Kroner og Prosent.
 * Et avtaleprodukt som har kronesatser kan ikke ha prosentsatser. <br>
 * Det er regel-/kontekst-avhengig hvordan satsene brukes, og hva de multipliseres med.
 *
 * @param <T> Typen sats - typisk Kroner eller Prosent.
 * @author Snorre E. Brekke - Computas
 * @see Kroner
 * @see Prosent
 */
public class Satser<T extends Sats> {
    private static final Satser<? extends Sats> INGEN_SATSER = new Satser<>(IngenSats.sats(), IngenSats.sats(), IngenSats.sats());

    private final T arbeidsgiverpremie;
    private final T medlemspremie;
    private final T administrasjonsgebyr;

    public Satser(T arbeidsgiverpremie, T medlemspremie, T administrasjonsgebyr) {
        this.arbeidsgiverpremie = requireNonNull(arbeidsgiverpremie, "sats for arbeidsgiver er påkrevd, men var null");
        this.medlemspremie = requireNonNull(medlemspremie, "sats for medlem er påkrevd, men var null");
        this.administrasjonsgebyr = requireNonNull(administrasjonsgebyr, "sats for administrasjonsgebyr er påkrevd, men var null");

        final Set<Class<?>> typer = Stream.of(arbeidsgiverpremie, medlemspremie, administrasjonsgebyr).map(Sats::getClass).distinct().collect(toSet());
        if (typer.size() > 1) {
            throw new IllegalArgumentException(
                    "Alle satser på et enkelt avtaleprodukt må være av samme type, men "
                            + typer.size()
                            + " forskjellige typer satser vart forsøkt brukt.\n"
                            + typer.stream().map(Class::getSimpleName).map(n -> "- " + n).collect(joining("\n")
                    )
            );
        }
    }

    /**
     * @return Satser som representer at ingen satser er satt. Operasjoner på statsene vil gi 0-verdier.
     */
    public static Satser<? extends Sats> ingenSatser() {
        return INGEN_SATSER;
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

    @Override
    public String toString() {
        return "arbeidsgiversats=" + arbeidsgiverpremie
                + ", medlemssats=" + medlemspremie
                + ", administrasjonsgebyr=" + administrasjonsgebyr;
    }

    /**
     * Returnerer en sterkt typa versjon av satsane angitt som prosent forutsatt at
     * alle satsane er av type {@link Prosent}.
     *
     * @return <code>this</code> viss alle satsane er angitt som prosentsatsar, ellers {@link Optional#empty()}
     * @since 1.1.1
     */
    @SuppressWarnings("unchecked")
    public Optional<Satser<Prosent>> somProsent() {
        return erProsent(arbeidsgiverpremie)
                && erProsent(medlemspremie)
                && erProsent(administrasjonsgebyr)
                ? of((Satser<Prosent>) this) : Optional.<Satser<Prosent>>empty();
    }

    /**
     * Returnerer en sterkt typa versjon av satsane angitt som beløp forutsatt at
     * alle satsane er av type {@link Kroner}.
     *
     * @return <code>this</code> viss alle satsane er angitt som kronebeløp, ellers {@link Optional#empty()}
     * @since 1.1.1
     */
    @SuppressWarnings("unchecked")
    public Optional<Satser<Kroner>> somKroner() {
        return erKroner(arbeidsgiverpremie)
                && erKroner(medlemspremie)
                && erKroner(administrasjonsgebyr)
                ? of((Satser<Kroner>) this) : Optional.<Satser<Kroner>>empty();
    }

    private boolean erKroner(final T sats) {
        return sats instanceof Kroner;
    }

    private boolean erProsent(final T sats) {
        return sats instanceof Prosent;
    }
}
