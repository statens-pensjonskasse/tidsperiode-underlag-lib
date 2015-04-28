package no.spk.pensjon.faktura.tidsserie.domain.it;

import static java.util.Optional.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringStatus;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

public class GruppelivsfaktureringIT {
    /**
     * Verifiserer at det kun er underlagsperioda tilhøyrande stillinga med nest høgaste stillingsprosent
     * som blir fakturert når medlemmet har 2+ parallelle stillingar der den største er tilknytta ein avtale uten
     * gruppelivsforsikring hos SPK.
     */
    @Test
    public void skalFakturereGruppelivstilkoblaStillingMedNestStoerstStillingsprosentDersomPeriodasStoersteStillingIkkjeErTilknyttaGruppeliv() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);
        final StillingsforholdId stilling3 = stillingsforhold(3L);

        final StillingsforholdPeriode stoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(new Stillingsprosent(prosent("100%")))
                );
        final StillingsforholdPeriode nestStoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(new Stillingsprosent(prosent("30%")))
                );
        final StillingsforholdPeriode minste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling3)
                                .stillingsprosent(new Stillingsprosent(prosent("20%")))
                );

        final Stream<StillingsforholdPeriode> stillingsperioder = Stream.of(minste, stoerste, nestStoerste);

        final Map<StillingsforholdId, Optional<GruppelivsfaktureringStatus>> resultat = beregnPerioder(
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
                                        .addProdukt(GRU)
                        )
                        .addAvtale(
                                stilling3,
                                avtale(
                                        avtaleId(223344L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(GRU)
                        ),
                stillingsperioder
        );


        assertErFakturerbar(resultat, stilling1.id())
                .as("er underlagsperioda med størst stillingsprosent men utan gruppeliv fakturerbar for GRU?")
                .isEqualTo(of(false));
        assertErFakturerbar(resultat, stilling2.id())
                .as("er underlagsperioda med nest størst stillingsprosent og gruppeliv fakturerbar for GRU?")
                .isEqualTo(of(true));
        assertErFakturerbar(resultat, stilling3.id())
                .as("er underlagsperioda med nest høgast stillingsprosent men med gruppeliv fakturerbar for GRU?")
                .isEqualTo(of(false));
    }

    /**
     * Verifiserer at det kun er underlagsperioda med størst stillingsprosent som blir fakturert
     * når medlemmet har 2+ parallelle stillingar innanfor tidsperioda underlagsperioda strekker seg over.
     */
    @Test
    public void skalFakturerePeriodePaaStillingMedStoerstStillingsprosent() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);

        final StillingsforholdPeriode stoerste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(new Stillingsprosent(prosent("30%")))
                );
        final StillingsforholdPeriode minste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(new Stillingsprosent(prosent("20%")))
                );
        final Stream<StillingsforholdPeriode> stillingar = Stream.of(stoerste, minste);

        final Map<StillingsforholdId, Optional<GruppelivsfaktureringStatus>> resultat = beregnPerioder(
                medlemsavtalar()
                        .fraOgMed(startDato)
                        .tilOgMed(of(sluttDato))
                        .addAvtale(
                                stilling1,
                                avtale(
                                        avtaleId(123456L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(GRU)
                        )
                        .addAvtale(
                                stilling2,
                                avtale(
                                        avtaleId(234567L)
                                )
                                        .addProdukt(PEN)
                                        .addProdukt(GRU)
                        ),
                stillingar
        );
        assertErFakturerbar(resultat, stilling1.id())
                .as("er underlagsperioda med størst stillingsprosent fakturerbar for GRU?")
                .isEqualTo(of(true));
        assertErFakturerbar(resultat, stilling2.id())
                .as("er underlagsperioda med lavast stillingsprosent fakturerbar for GRU?")
                .isEqualTo(of(false));
    }

    /**
     * Verifiserer at kun ei av underlagsperiodene der medlemmet har 2+ parallelle stillingar med lik stillingsprosent,
     * blir markert som fakturerbar for gruppelivsproduktet.
     * <p>
     * Merk at forretningsregelen er bevist udefinert når det er fleire stillingar med lik stillingsprosent, ergo
     * verifiserer vi ikkje kva for ei av stillingane som blir markert, berre at nøyaktig 1 av dei blir markert som
     * fakturerbar.
     */
    @Test
    public void skalFakturereKunEitAvDeiParallelleStillinganeForPeriodeDerBeggeHarLikStillingsprosent() {
        final LocalDate startDato = dato("2015.01.01");
        final LocalDate sluttDato = dato("2015.12.31");

        final StillingsforholdId stilling1 = stillingsforhold(1L);
        final StillingsforholdId stilling2 = stillingsforhold(2L);
        final Stillingsprosent stillingsprosent = new Stillingsprosent(prosent("30%"));

        final StillingsforholdPeriode foreste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling1)
                                .stillingsprosent(stillingsprosent)
                );
        final StillingsforholdPeriode siste = new StillingsforholdPeriode(startDato, of(sluttDato))
                .leggTilOverlappendeStillingsendringer(
                        new Stillingsendring()
                                .aksjonsdato(startDato)
                                .stillingsforhold(stilling2)
                                .stillingsprosent(stillingsprosent)
                );
        final Stream<StillingsforholdPeriode> stillingar = Stream.of(foreste, siste);

        // Verifiserer at det kun er ei av stillingane som blir fakturert
        assertThat(
                beregnPerioder(
                        medlemsavtalar()
                                .fraOgMed(startDato)
                                .tilOgMed(of(sluttDato))
                                .addAvtale(
                                        stilling1,
                                        avtale(
                                                avtaleId(123456L)
                                        )
                                                .addProdukt(PEN)
                                                .addProdukt(GRU)
                                )
                                .addAvtale(
                                        stilling2,
                                        avtale(
                                                avtaleId(234567L)
                                        )
                                                .addProdukt(PEN)
                                                .addProdukt(GRU)
                                ),
                        stillingar
                )
                        .values()
                        .stream()
                        .map(Optional::get)
                        .filter(GruppelivsfaktureringStatus::erFakturerbar)
                        .collect(toList())
        )
                .as("stillingsforhold som er fakturerbare for GRU i den aktuelle perioden")
                .hasSize(1);
    }

    private static Map<StillingsforholdId, Optional<GruppelivsfaktureringStatus>> beregnPerioder(
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
                    return periode.beregn(GruppelivsfaktureringRegel.class);
                })
                .collect(
                        groupingBy(
                                GruppelivsfaktureringStatus::stillingsforhold,
                                reducing((a, b) -> b)
                        )
                );
    }

    private static Underlagsperiode nyPeriode(final LocalDate startDato, final LocalDate sluttDato) {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(startDato)
                .tilOgMed(sluttDato)
                .med(new GruppelivsfaktureringRegel())
                .bygg();

    }

    private static AbstractObjectAssert<?, Optional<Boolean>> assertErFakturerbar(
            final Map<StillingsforholdId, Optional<GruppelivsfaktureringStatus>> resultat,
            final long stillingsforholdId
    ) {
        return assertThat(erFakturerbar(resultat, stillingsforholdId));
    }

    private static Optional<Boolean> erFakturerbar(
            final Map<StillingsforholdId, Optional<GruppelivsfaktureringStatus>> resultat,
            final long stillingsforholdId
    ) {
        return resultat.get(stillingsforhold(stillingsforholdId)).map(GruppelivsfaktureringStatus::erFakturerbar);
    }

}
