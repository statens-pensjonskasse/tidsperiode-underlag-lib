package no.spk.felles.tidsperiode.underlag;

import static java.util.Optional.empty;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static no.spk.felles.tidsperiode.underlag.Assertions.assertThat;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.AbstractOptionalAssert;
import org.junit.Test;

public class UnderlagsperiodeBuilderTest {
    /**
     * Verifiserer at alle annotasjonane som er lagt til på den opprinnelige builderen, blir kopiert
     * over til den nye builderen.
     */
    @Test
    public void skalKopiereAnnotasjonar() {
        final Integer expected = 123;

        assertThat(
                builder()
                        .fraOgMed(dato("2007.05.12"))
                        .tilOgMed(dato("2009.12.30"))
                        .med(expected)
                        .kopi()
        )
                .harAnnotasjon(Integer.class, expected);
    }

    @Test
    public void skalKopiereKoblingar() {
        final Tidsperiode<?> expected = new GenerellTidsperiode(dato("1950.01.01"), empty());

        assertThat(
                builder()
                        .fraOgMed(dato("2007.05.12"))
                        .tilOgMed(dato("2009.12.30"))
                        .medKobling(expected)

                        .kopi()
        )
                .harKoblingAvType(
                        GenerellTidsperiode.class,
                        kobling -> assertThat(kobling).isSameAs(expected)
                );
    }

    @Test
    public void skalIkkjeDeleSamlingVedKopieringAvKoblingar() {
        final UnderlagsperiodeBuilder builder = builder().fraOgMed(dato("2007.05.12")).tilOgMed(dato("2009.12.31"));

        final GenerellTidsperiode a = new GenerellTidsperiode(dato("1950.01.01"), empty());
        builder.medKobling(a);

        final UnderlagsperiodeBuilder kopi = builder.kopi();
        assertThat(
                kopi
        )
                .harKoblingarAvType(
                        GenerellTidsperiode.class,
                        actual ->
                                actual
                                        .hasSize(1)
                                        .contains(a)
                );

        builder.medKobling(new GenerellTidsperiode(dato("1990.01.01"), empty()));
        assertThat(
                kopi
        )
                .harKoblingarAvType(
                        GenerellTidsperiode.class,
                        actual ->
                                actual
                                        .hasSize(1)
                                        .contains(a)
                );
    }

    @Test
    public void skalAnnoterePeriodaBasertPaaVerdiensType() {
        assertThat(
                builder()
                        .med(0)
        )
                .harAnnotasjon(Integer.class, 0);
    }

    /**
     * Verifiserer at {@link UnderlagsperiodeBuilder#med(Object)}
     * kun annoterer perioda basert på verdiens eksakte {@link Object#getClass()}, ikkje eventuelle interface eller
     * superklasser som verdien implementerer eller arvar frå, direkte eller indirekte.
     */
    @Test
    public void skalIkkjeAnnoterePeriodeMedVerdiensSuperTyper() {
        final Underlagsperiode periode = bygg(
                builder()
                        .med(0)
        );
        assertThat(
                periode
        )
                .annotasjon(Integer.class, AbstractOptionalAssert::isPresent)
                .annotasjon(Number.class, AbstractOptionalAssert::isEmpty)
                .annotasjon(Object.class, AbstractOptionalAssert::isEmpty)
        ;
    }

    private static UnderlagsperiodeBuilder builder() {
        return new UnderlagsperiodeBuilder().fraOgMed(dato("2001.01.01")).tilOgMed(dato("2001.12.31"));
    }

    private static Underlagsperiode bygg(final UnderlagsperiodeBuilder builder) {
        return builder.bygg();
    }
}