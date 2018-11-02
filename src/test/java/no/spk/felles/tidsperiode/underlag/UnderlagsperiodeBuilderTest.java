package no.spk.felles.tidsperiode.underlag;

import static java.util.Optional.empty;
import static no.spk.felles.tidsperiode.underlag.Assertions.assertKoblingarAvType;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UnderlagsperiodeBuilderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at alle annotasjonane som er lagt til på den opprinnelige builderen, blir kopiert
     * over til den nye builderen.
     */
    @Test
    public void skalKopiereAnnotasjonar() {
        final Integer expected = 123;

        final UnderlagsperiodeBuilder builder = builder();
        builder.fraOgMed(dato("2007.05.12")).tilOgMed(dato("2009.12.30")).med(expected);

        assertThat(builder.kopi().bygg().annotasjonFor(Integer.class)).isSameAs(expected);
    }

    @Test
    public void skalKopiereKoblingar() {
        final Tidsperiode<?> expected = new GenerellTidsperiode(dato("1950.01.01"), empty());

        final UnderlagsperiodeBuilder builder = builder();
        builder.fraOgMed(dato("2007.05.12")).tilOgMed(dato("2009.12.30"))
                .medKobling(expected);

        assertThat(builder.kopi().bygg().koblingAvType(GenerellTidsperiode.class).get()).isSameAs(expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalIkkjeDeleSamlingVedKopieringAvKoblingar() {
        final UnderlagsperiodeBuilder builder = builder().fraOgMed(dato("2007.05.12")).tilOgMed(dato("2009.12.31"));

        final GenerellTidsperiode a = new GenerellTidsperiode(dato("1950.01.01"), empty());
        builder.medKobling(a);

        final UnderlagsperiodeBuilder kopi = builder.kopi();
        assertKoblingarAvType(kopi.bygg(), GenerellTidsperiode.class).hasSize(1).contains(a);

        builder.medKobling(new GenerellTidsperiode(dato("1990.01.01"), empty()));
        assertKoblingarAvType(kopi.bygg(), GenerellTidsperiode.class).hasSize(1).contains(a);
    }

    @Test
    public void skalAnnoterePeriodaBasertPaaVerdiensType() {
        final Underlagsperiode periode = bygg(
                builder()
                        .med(0)
        );
        assertThat(periode.annotasjonFor(Integer.class)).isEqualTo(0);
    }

    /**
     * Verifiserer at {@link UnderlagsperiodeBuilder#med(Object)}
     * kun annoterer perioda basert på verdiens eksakte {@link Object#getClass()}, ikkje eventuelle interface eller
     * superklasser som verdien implementerer eller arvar frå, direkte eller indirekte.
     */
    @Test
    public void skalIkkjeAnnoterePeriodeMedVerdiensSuperTyper() {
        final Underlagsperiode periode = bygg(builder()
                        .med(0)
        );
        assertThat(periode.valgfriAnnotasjonFor(Number.class).isPresent()).isFalse();
        assertThat(periode.valgfriAnnotasjonFor(Object.class).isPresent()).isFalse();
    }

    private static UnderlagsperiodeBuilder builder() {
        return new UnderlagsperiodeBuilder().fraOgMed(dato("2001.01.01")).tilOgMed(dato("2001.12.31"));
    }

    private static Underlagsperiode bygg(final UnderlagsperiodeBuilder builder) {
        return builder.bygg();
    }
}