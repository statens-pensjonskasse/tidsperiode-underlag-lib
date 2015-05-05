package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringStatus;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;
import static org.assertj.core.api.Assertions.assertThat;

public class YrkesskadefaktureringIT {
    /**
     * Verifiserer at aktive stillingar som er ute i permisjon utan lønn, ikkje får tildelt nokon yrkesskadeandel.
     */
    @Test
    public void skalIgnorereStillingarSomErUteIPermisjonUtanLoennVedBeregningAvYrkesskadeAndel() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);

        final StillingsforholdPeriode stoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .aksjonskode(PERMISJON_UTAN_LOENN)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(new Stillingsprosent(prosent("100%")))
                );
        final StillingsforholdPeriode minste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(new Stillingsprosent(prosent("20%")))
                );

        final Stream<StillingsforholdPeriode> stillingsperioder = Stream.of(minste, stoerste);

        final Map<StillingsforholdId, Optional<YrkesskadefaktureringStatus>> resultat = beregnPerioder(
                medlemsavtalar()
                        .fraOgMed(startDato)
                        .tilOgMed(of(sluttDato))
                        .addAvtale(
                                stilling1,
                                avtale(
                                        avtaleId(123456L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        )
                        .addAvtale(
                                stilling2,
                                avtale(
                                        avtaleId(234567L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        ),
                stillingsperioder
        );

        assertYrkesskadeandel(resultat, stilling1)
                .as("yrkesskadeandelen for underlagsperioda med størst stillingsprosent men permisjon utan lønn")
                .isEqualTo(of("0%"));
        assertYrkesskadeandel(resultat, stilling2)
                .as("yrkesskadeandelen for underlagsperioda med lavast stillingsprosent")
                .isEqualTo(of("20%"));
    }

    /**
     * Verifiserer at det kun er stillingane tilknytta YSK som får ein yrkesskadeandel når medlemmet har 2+ parallelle
     * stillingar der den største er tilknytta ein avtale uten yrkesskadeforsikring hos SPK.
     */
    @Test
    public void skalKunFakturereYrkesskadeForStillingarTilkoblaAvtalarSomBetalarYrkesskadePremieTilSPK() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);
        final StillingsforholdId stilling3 = stillingsforhold(3L);

        final StillingsforholdPeriode stoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(new Stillingsprosent(prosent("100%")))
                );
        final StillingsforholdPeriode nestStoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(new Stillingsprosent(prosent("30%")))
                );
        final StillingsforholdPeriode minste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling3)
                                .stillingsprosent(new Stillingsprosent(prosent("20%")))
                );

        final Stream<StillingsforholdPeriode> stillingsperioder = Stream.of(minste, stoerste, nestStoerste);

        final Map<StillingsforholdId, Optional<YrkesskadefaktureringStatus>> resultat = beregnPerioder(
                medlemsavtalar()
                        .fraOgMed(startDato)
                        .tilOgMed(of(sluttDato))
                        .addAvtale(
                                stilling1,
                                avtale(
                                        avtaleId(123456L)
                                )
                                        .addProdukt(PEN)
                        )
                        .addAvtale(
                                stilling2,
                                avtale(
                                        avtaleId(234567L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        )
                        .addAvtale(
                                stilling3,
                                avtale(
                                        avtaleId(223344L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        ),
                stillingsperioder
        );

        assertYrkesskadeandel(resultat, stilling1)
                .as("yrkesskadeandel for underlagsperioda med størst stillingsprosent men utan YSK hos SPK")
                .isEqualTo(of("0%"));
        assertYrkesskadeandel(resultat, stilling2)
                .as("yrkesskadeandel for underlagsperioda med nest størst stillingsprosent som har YSK hos SPK")
                .isEqualTo(of("30%"));
        assertYrkesskadeandel(resultat, stilling3)
                .as("yrkesskadeandel for underlagsperioda med lavast stillingsprosent som har YSK hos SPK")
                .isEqualTo(of("20%"));
    }

    /**
     * Verifiserer at parallelle stillingar som fører til at total stillingsprosent for perioda overstig 100%,
     * får ein yrkesskadeandel som blir avkorta i henhold til kor stor andel av total stillingsprosent <= 100%
     * som er "ledig".
     */
    @Test
    public void skalAvkorteParallelleStillingarNaarTotalStillingsprosentOverstig100ProsentBasertPaaSorteringPaaStillingsstoerrelse() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);
        final StillingsforholdId stilling3 = stillingsforhold(3L);

        final StillingsforholdPeriode stoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(new Stillingsprosent(prosent("80%")))
                );
        final StillingsforholdPeriode avkorta = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(new Stillingsprosent(prosent("30%")))
                );

        final StillingsforholdPeriode minste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling3)
                                .stillingsprosent(new Stillingsprosent(prosent("10%")))
                );
        final Stream<StillingsforholdPeriode> stillingar = Stream.of(stoerste, avkorta, minste);

        final Map<StillingsforholdId, Optional<YrkesskadefaktureringStatus>> resultat = beregnPerioder(
                medlemsavtalar()
                        .fraOgMed(startDato)
                        .tilOgMed(of(sluttDato))
                        .addAvtale(
                                stilling1,
                                avtale(
                                        avtaleId(123456L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        )
                        .addAvtale(
                                stilling2,
                                avtale(
                                        avtaleId(234567L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        )
                        .addAvtale(
                                stilling3,
                                avtale(
                                        avtaleId(223344L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(YSK)
                        ),
                stillingar
        );

        assertYrkesskadeandel(resultat, stilling1).isEqualTo(of("80%"));
        assertYrkesskadeandel(resultat, stilling2).isEqualTo(of("20%"));
        assertYrkesskadeandel(resultat, stilling3).isEqualTo(of("0%"));
    }

    private static Stillingsendring eiStillingsendring() {
        return new Stillingsendring()
                .aksjonskode(Aksjonskode.ENDRINGSMELDING);
    }

    private static AbstractObjectAssert<?, Optional<String>> assertYrkesskadeandel(
            final Map<StillingsforholdId, Optional<YrkesskadefaktureringStatus>> resultat,
            final StillingsforholdId stilling) {
        return assertThat(resultat.get(stilling).map(YrkesskadefaktureringStatus::andel).map(Object::toString));
    }

    private static Map<StillingsforholdId, Optional<YrkesskadefaktureringStatus>> beregnPerioder(
            final MedlemsavtalarPeriode.Builder avtalar, final Stream<StillingsforholdPeriode> perioder
    ) {
        final List<StillingsforholdPeriode> tmp = perioder.collect(toList());
        final Medlemsperiode medlem = new Medlemsperiode(avtalar.fraOgMed(), avtalar.tilOgMed()).kobleTil(tmp.stream());
        return tmp.stream()
                .map(stilling -> {
                    final Underlagsperiode periode = nyPeriode(medlem.fraOgMed(), medlem.tilOgMed().get());
                    avtalar.bygg().annoter(periode);
                    medlem.annoter(periode);
                    stilling.annoter(periode);
                    return periode.beregn(YrkesskadefaktureringRegel.class);
                })
                .collect(
                        groupingBy(
                                YrkesskadefaktureringStatus::stillingsforhold,
                                reducing((a, b) -> b)
                        )
                );
    }

    private static Underlagsperiode nyPeriode(final LocalDate startDato, final LocalDate sluttDato) {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(startDato)
                .tilOgMed(sluttDato)
                .med(new YrkesskadefaktureringRegel())
                .bygg();

    }
}
