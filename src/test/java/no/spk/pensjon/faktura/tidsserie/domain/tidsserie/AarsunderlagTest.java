package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractMapAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertUnikeUnderlagsAnnotasjonar;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.assertUnikeAnnotasjonsverdiar;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.harAnnotasjon;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.or;
import static no.spk.pensjon.faktura.tidsserie.domain.underlag.Assertions.paakrevdAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link Aarsunderlag}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsunderlagTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final Aarsunderlag aarsunderlag = new Aarsunderlag();

    /**
     * Verifiserer at årsunderlaget kopierer over alle annotasjonar frå stillingsforholdunderlaget det er generert frå.
     */
    @Test
    public void skalKopiereAlleAnnotasjonarFraaStillingsforholdUnderlaget() {
        final List<Underlag> alle = aarsunderlag.genererUnderlagPrAar(
                etFlerAarigStillingsforholdUnderlag()
                        .annoter(StillingsforholdId.class, new StillingsforholdId(92272722L))
                        .annoter(String.class, "Hello World!")
        )
                .collect(toList());

        final Predicate<Underlag> manglerAnnotasjonFraStillingsforholdUnderlaget = or(
                Assertions.<Underlag>harAnnotasjon(StillingsforholdId.class).negate(),
                Assertions.<Underlag>harAnnotasjon(String.class).negate()
        );
        assertThat(
                alle
                        .stream()
                        .filter(manglerAnnotasjonFraStillingsforholdUnderlaget)
                        .collect(toList())
        )
                .as("årsunderlag utan alle annotasjonar som stillingsforholdunderlaget var annotert med")
                .hasSize(0);

        assertUnikeUnderlagsAnnotasjonar(alle, String.class).contains("Hello World!");
        assertUnikeUnderlagsAnnotasjonar(alle, StillingsforholdId.class).contains(new StillingsforholdId(92272722L));
    }

    /**
     * Verifiserer at årsunderlaget blir annotert med årstallet det er generert for.
     */
    @Test
    public void skalAnnotereAarsunderlagMedAarstall() {
        final List<Underlag> alle = aarsunderlag.genererUnderlagPrAar(
                etFlerAarigStillingsforholdUnderlag()
        )
                .collect(toList());

        final Predicate<Underlag> predikat = harAnnotasjon(Aarstall.class);
        assertThat(alle.stream().filter(predikat.negate()).collect(toList()))
                .as("årsunderlag som ikkje er annotert med årstall")
                .isEmpty();

        assertThat(alle.stream().map(paakrevdAnnotasjon(Aarstall.class)).collect(toList()))
                .as("årstall annotert på årsunderlaga")
                .containsExactly(new Aarstall(2000), new Aarstall(2001), new Aarstall(2002));
    }

    /**
     * Verifiserer at underlaget blir splitta opp i tre mindre underlag der kvart nye underlag kun inneheld perioder
     * tilknytta eit og samme årstall.
     */
    @Test
    public void skalKunInneholdeUnderlagsperioderTilknyttaEitAarOmGangen() {
        final List<Underlag> alle = aarsunderlag
                .genererUnderlagPrAar(
                        etFlerAarigStillingsforholdUnderlag()
                )
                .collect(toList());
        assertAarsunderlag(
                alle.stream()
        ).hasSize(3);

        alle.stream().forEach((Underlag underlag) -> {
            assertUnikeAnnotasjonsverdiar(underlag, Aarstall.class).hasSize(1);
        });

        assertUnikeAnnotasjonsverdiar(alle.get(0), Aarstall.class).contains(new Aarstall(2000));
        assertUnikeAnnotasjonsverdiar(alle.get(1), Aarstall.class).contains(new Aarstall(2001));
        assertUnikeAnnotasjonsverdiar(alle.get(2), Aarstall.class).contains(new Aarstall(2002));
    }

    /**
     * Verifiserer at årsunderlaga blir generert basert på underlagsperiodenes årstall-annotasjon, ikkje basert på
     * periodenes koblingar.
     */
    @Test
    public void skalFiltrereUnderlagsperioderBasertPaaAarstallAnnotasjonIkkjeBasertPaPeriodeKoblingar() {
        final Underlag underlag = underlag(
                // Ei periode som er inkonsistent satt opp, tidsperioda den dekker og årstallet den er annotert med
                // er ikkje samsvarande med årsperioda den er kobla mot
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.10.12"))
                        .med(
                                new Aarstall(2000)
                        )
                        .medKobling(
                                new Aar(new Aarstall(2001))
                        )
        );
        final List<Underlag> alle = aarsunderlag.genererUnderlagPrAar(underlag).collect(toList());
        assertAarsunderlag(alle.stream()).hasSize(1);
        assertUnikeAnnotasjonsverdiar(alle.get(0), Aarstall.class).contains(new Aarstall(2000));
    }

    /**
     * Verifiserer at genereringa av årsunderlag feilar dersom minst ei underlagsperiode manglar
     * Aarstall-annotasjonen.
     */
    @Test
    public void skalFeileDersomInputUnderlagetsPerioderManglarAarstallAnnotasjon() {
        e.expect(PaakrevdAnnotasjonManglarException.class);
        aarsunderlag.genererUnderlagPrAar(
                underlag(
                        periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.10.12"))
                                .med(new Aarstall(2000)),
                        periode().fraOgMed(dato("2000.10.13")).tilOgMed(dato("2000.12.12"))
                )
        ).collect(toList());
    }

    private static UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder();
    }

    private static Underlag underlag(final UnderlagsperiodeBuilder... perioder) {
        return new Underlag(asList(perioder).stream().map((UnderlagsperiodeBuilder builder) -> builder.bygg()));
    }

    private static Underlag etFlerAarigStillingsforholdUnderlag() {
        return underlag(
                periode().fraOgMed(dato("2000.01.01")).tilOgMed(dato("2000.12.31")).med(new Aarstall(2000)),
                periode().fraOgMed(dato("2001.01.01")).tilOgMed(dato("2001.12.31")).med(new Aarstall(2001)),
                periode().fraOgMed(dato("2002.01.01")).tilOgMed(dato("2002.12.31")).med(new Aarstall(2002))
        );
    }

    private static AbstractMapAssert<?, ? extends Map<Aarstall, List<Underlag>>, Aarstall, List<Underlag>> assertAarsunderlag(final Stream<Underlag> underlag) {
        return assertThat(
                underlag.collect(
                        groupingBy((Underlag u) -> u
                                        .stream()
                                        .map((Underlagsperiode p) -> p.annotasjonFor(Aarstall.class))
                                        .reduce((a1, a2) -> {
                                                    throw new IllegalArgumentException("Underlaget inneheld perioder som er annotert med forskjellige årstall (" + a1 + "," + a2 + ")");
                                                }
                                        )
                                        .get()
                        )
                )
        ).as("årsunderlag");
    }

}
