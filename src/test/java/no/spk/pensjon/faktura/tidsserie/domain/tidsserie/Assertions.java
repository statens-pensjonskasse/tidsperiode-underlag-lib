package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractObjectAssert;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.paakrevdAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

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
    public static <T> AbstractIterableAssert<?, ? extends Iterable<T>, T> assertUnikeUnderlagsAnnotasjonar(
            final Collection<Underlag> underlagene, final Class<T> type) {
        return assertThat(underlagene.stream().map(paakrevdAnnotasjon(type)).collect(toSet()))
                .as(type.getSimpleName() + " annotert på underlaga " + underlagene);
    }

    /**
     * Opprettar ein ny assertion som opererer på underlagsperiodas valgfrie annotasjon av den angitte typen.
     *
     * @param periode underlagsperioda som annotasjonen skal hentast frå
     * @param <T>     annotasjonstypen som det skal hentast ut verdiar for
     * @param type    typen til dei påkrevde annotasjonsverdiane som skal hentast ut frå kvart underlag
     * @return ein ny assertion for periodas valgfrie annotasjon av den angitte typen
     */
    public static <T> AbstractObjectAssert<?, Optional<T>> assertAnnotasjon(final Underlagsperiode periode, final Class<T> type) {
        return assertThat(periode.valgfriAnnotasjonFor(type)).as(type.getSimpleName() + "-annotasjon for periode " + periode);
    }
}
