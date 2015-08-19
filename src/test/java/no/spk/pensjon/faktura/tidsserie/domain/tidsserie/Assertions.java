package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.stream.Collectors.toSet;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.paakrevdAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.OptionalAssert;

public class Assertions {
    /**
     * Opprettar ein ny assertion som opererer på samlinga av unike annotasjonsverdiar
     * for den angitte typen frå underlaga i <code>underlagene</code>.
     *
     * @param <T>         annotasjonstypen som det skal hentast ut verdiar for
     * @param underlagene ei samling med underlag som annotasjonsverdiane skal hentast frå
     * @param type        typen til dei påkrevde annotasjonsverdiane som skal hentast ut frå kvart underlag
     * @return ein ny asserter med verdiar henta frå underlagene underlaga
     */
    public static <T> AbstractIterableAssert<?, ? extends Iterable<? extends T>, T> assertUnikeUnderlagsAnnotasjonar(
            final Collection<Underlag> underlagene, final Class<T> type) {
        return assertThat(underlagene.stream().map(paakrevdAnnotasjon(type)).collect(toSet()))
                .as(type.getSimpleName() + " annotert på underlaga " + underlagene);
    }

    /**
     * Opprettar ein ny assertion som opererer på underlagsperiodas valgfrie annotasjon av den angitte typen.
     *
     * @param <T>     annotasjonstypen som det skal hentast ut verdiar for
     * @param periode underlagsperioda som annotasjonen skal hentast frå
     * @param type    typen til dei påkrevde annotasjonsverdiane som skal hentast ut frå kvart underlag
     * @return ein ny assertion for periodas valgfrie annotasjon av den angitte typen
     */
    public static <T> OptionalAssert<T> assertAnnotasjon(
            final Underlagsperiode periode, final Class<T> type) {
        return assertThat(periode.valgfriAnnotasjonFor(type))
                .as(type.getSimpleName() + "-annotasjon for periode " + periode);
    }

    /**
     * Opprettar ein ny assertion som opererer på ein utleda verdi frå underlagsperiodas valgfrie annotasjon av den angitte typen.
     *
     * @param <T>     annotasjonstypen som det skal hentast ut verdiar for
     * @param periode underlagsperioda som annotasjonen skal hentast frå
     * @param type    typen til dei påkrevde annotasjonsverdiane som skal hentast ut frå kvart underlag
     * @param mapper  funksjon som konverterer annotasjonsverdien til verdien som skal verifiserast
     * @return ein ny assertion for den valgfrie verdien som <code>mapper</code> utledar
     * frå periodas valgfrie annotasjon av den angitte typen
     */
    public static <T, R> OptionalAssert<R> assertAnnotasjon(
            final Underlagsperiode periode, final Class<T> type, final Function<T, Optional<R>> mapper) {
        return assertThat(periode.valgfriAnnotasjonFor(type).flatMap(mapper))
                .as("utleda verdi frå " + type.getSimpleName() + "-annotasjon for periode " + periode);
    }
}
