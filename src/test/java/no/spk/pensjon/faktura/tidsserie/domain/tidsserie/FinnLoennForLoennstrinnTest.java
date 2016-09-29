package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn.loennstrinn;
import static no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder.grupper;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.StatligLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.OptionalAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FinnLoennForLoennstrinnTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final UnderlagsperiodeBuilder eiPeriode = new UnderlagsperiodeBuilder()
            .fraOgMed(dato("2007.07.01"))
            .tilOgMed(dato("2007.07.31"));

    /**
     * Verifiserer at algoritma ikkje feilar dersom det ikkje eksisterer ei gruppering som inneheld lønnstrinnperioder
     * som er gjeldande for lønnstrinnet innanfor underlagsperioda.
     */
    @Test
    public void skalIkkjeFeileDersomDetIkkjeEksistererEiLoennstrinnperiodeSomTilhoeyrerLoennstrinnet() {
        final Loennstrinn loennstrinn = new Loennstrinn(27);
        assertLoennForLoennstrinn(
                eiPeriode
                        .kopi()
                        .med(Ordning.SPK)
                        .med(loennstrinn)
                ,
                loennstrinn
        ).isEqualTo(empty());
    }

    /**
     * Verifiserer at algoritma feilar dersom det eksisterer fleire grupperingar som inneheld lønnstrinnperioder
     * som er gjeldande for lønnstrinnet innanfor ei og samme underlagsperiode.
     */
    @Test
    public void skalFeileDersomDetEksistererFleireLoennstrinnperioderSomTilhoeyrerLoennstrinnet() {
        final LocalDate fraOgMed = dato("1979.05.06");
        final Loennstrinn loennstrinn = loennstrinn(27);

        e.expect(IllegalStateException.class);
        e.expectMessage("Det er oppdaga fleire lønnstrinnperioder for");
        e.expectMessage(loennstrinn.toString());

        new FinnLoennForLoennstrinn(
                eiPeriode
                        .kopi()
                        .med(Ordning.SPK)
                        .med(loennstrinn)
                        .medKoblingar(
                                Stream.of(
                                        grupper(Ordning.SPK,
                                                loepende(loennstrinn, fraOgMed, kroner(372_000))
                                        ),
                                        grupper(Ordning.SPK,
                                                loepende(loennstrinn, fraOgMed.minusYears(1), kroner(172_000))
                                        ),
                                        grupper(Ordning.SPK,
                                                loepende(loennstrinn, fraOgMed.plusYears(1), kroner(272_000))
                                        )
                                ).flatMap(gruppe -> gruppe)
                        )
                        .bygg()
        )
                .loennForLoennstrinn();
    }

    /**
     * Verifiserer at algoritma klarer å finne lønn for lønnstrinnet både når det eksisterer kun ei og når det
     * eksisterer fleire grupperingar som er gjeldande innanfor underlagsperioda.
     */
    @Test
    public void skalHenteLoennFraaLoennstrinnetSomTilhoeyrerLoennstrinnet() {
        final LocalDate fraOgMed = dato("1965.01.29");
        final Kroner beloep = kroner(272_000);

        final Loennstrinn loennstrinn = loennstrinn(27);
        assertLoennForLoennstrinn(
                eiPeriode
                        .kopi()
                        .med(Ordning.SPK)
                        .med(loennstrinn)
                        .medKoblingar(
                                grupper(Ordning.SPK,
                                        loepende(loennstrinn(26), fraOgMed, kroner(172_000))
                                )
                        )
                        .medKoblingar(
                                grupper(Ordning.SPK,
                                        loepende(loennstrinn(28), fraOgMed, kroner(372_000))
                                )
                        )
                        .medKoblingar(
                                grupper(Ordning.SPK,
                                        loepende(loennstrinn, fraOgMed, beloep)
                                )
                        )
                ,
                loennstrinn
        ).isEqualTo(of(new LoennstrinnBeloep(beloep)));
    }

    private static StatligLoennstrinnperiode loepende(final Loennstrinn loennstrinn,
                                                      final LocalDate fraOgMed, final Kroner beloep) {
        return new StatligLoennstrinnperiode(fraOgMed, empty(), loennstrinn, beloep);
    }

    private static OptionalAssert<LoennstrinnBeloep> assertLoennForLoennstrinn(
            final UnderlagsperiodeBuilder builder, final Loennstrinn loennstrinn) {
        return assertThat(new FinnLoennForLoennstrinn(builder.bygg()).loennForLoennstrinn())
                .as("lønn for lønnstrinn " + loennstrinn);
    }
}