package no.spk.felles.tidsperiode.underlag;

import static java.util.stream.Collectors.toList;
import static no.spk.felles.tidsperiode.Datoar.dato;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.data.Index;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class Assertions extends org.assertj.core.api.Assertions {
    public static UnderlagAssertion assertThat(final Underlag actual) {
        return new UnderlagAssertion(actual);
    }

    public static UnderlagsperiodeAssertion assertThat(final Underlagsperiode actual) {
        return new UnderlagsperiodeAssertion(actual);
    }

    public static UnderlagsperiodeAssertion assertThat(final UnderlagsperiodeBuilder actual) {
        return assertThat(actual.bygg());
    }

    static class UnderlagAssertion extends ObjectAssert<Underlag> {
        private UnderlagAssertion(final Underlag underlag) {
            super(underlag);
        }

        UnderlagAssertion periode(
                final Index index,
                final Consumer<UnderlagsperiodeAssertion> assertion
        ) {
            final List<Underlagsperiode> perioder = actual.stream().collect(toList());
            assertThat(perioder).hasSizeGreaterThan(index.value);

            assertion.accept(
                    assertThat(
                            perioder.get(index.value)
                    )
            );
            return this;
        }

        UnderlagAssertion harPerioder(final int expected) {
            assertThat(actual.stream())
                    .as("Perioder fra <%s>", actual)
                    .hasSize(expected);
            return this;
        }

        UnderlagAssertion harFraOgMed(final String expected) {
            return periode(
                    atIndex(0),
                    actual -> actual.harFraOgMed(expected)
            );
        }

        UnderlagAssertion harTilOgMed(final String expected) {
            assertThat(
                    actual.last()
            )
                    .as("<%s>.last()")
                    .hasValueSatisfying(
                            siste ->
                                    assertThat(siste)
                                            .harTilOgMed(expected)
                    );
            return this;
        }

        UnderlagAssertion allSatisfy(final Consumer<UnderlagsperiodeAssertion> assertion) {
            actual
                    .stream()
                    .map(Assertions::assertThat)
                    .forEach(assertion);
            return this;
        }
    }

    static class UnderlagsperiodeAssertion extends ObjectAssert<Underlagsperiode> {
        private UnderlagsperiodeAssertion(final Underlagsperiode underlagsperiode) {
            super(underlagsperiode);
        }

        <T extends Tidsperiode<T>> UnderlagsperiodeAssertion harKoblingAvType(
                final Class<T> kobling,
                final Consumer<T> assertion
        ) {
            assertThat(
                    actual
                            .koblingarAvType(kobling)
                            .collect(toList())
            )
                    .as("koblingar av type %s", kobling)
                    .hasSize(1);
            actual
                    .koblingAvType(kobling)
                    .ifPresent(assertion);
            return this;
        }

        <T extends Tidsperiode<T>> UnderlagsperiodeAssertion harKoblingarAvType(
                final Class<T> kobling,
                final Consumer<ListAssert<T>> assertion
        ) {
            assertion.accept(
                    assertThat(
                            actual
                                    .koblingarAvType(kobling)
                                    .collect(toList())
                    )
            );
            return this;
        }

        UnderlagsperiodeAssertion manglarKoblingAvType(final Class<GenerellTidsperiode> kobling) {
            assertThat(
                    actual.koblingarAvType(kobling)
            )
                    .as("koblingar av type %s for %s", kobling, actual)
                    .isEmpty();
            return this;
        }

        <T> UnderlagsperiodeAssertion annotasjon(
                final Class<T> annotasjon,
                final Consumer<OptionalAssert<T>> assertion
        ) {
            assertion.accept(
                    assertThat(
                            actual.valgfriAnnotasjonFor(annotasjon)
                    )
            );
            return this;
        }

        <T, E extends T> UnderlagsperiodeAssertion harAnnotasjon(final Class<T> annotasjon, final E expected) {
            return annotasjon(
                    annotasjon,
                    actual -> actual.contains(expected)
            );
        }

        UnderlagsperiodeAssertion harFraOgMed(final String expected) {
            assertThat(actual.fraOgMed())
                    .as("<%s>.fraOgMed()", actual)
                    .isEqualTo(dato((expected)));
            return this;
        }

        UnderlagsperiodeAssertion harTilOgMed(final String expected) {
            return harTilOgMed(dato(expected));
        }

        UnderlagsperiodeAssertion harTilOgMed(final LocalDate expected) {
            assertThat(actual.tilOgMed())
                    .as("<%s>.tilOgMed()", actual)
                    .contains(expected);
            return this;
        }
    }
}
