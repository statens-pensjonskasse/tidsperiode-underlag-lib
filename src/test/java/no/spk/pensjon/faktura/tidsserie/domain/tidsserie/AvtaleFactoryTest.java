package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleperiode.avtaleperiode;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus.AAO_01;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus.UKJENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.AFP;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.TIP;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.enAvtaleversjon;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.tidenesMorgen;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link AvtaleFactory}.
 *
 * @author Tarjei Skorgenes
 */
public class AvtaleFactoryTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final AvtaleFactory factory = new AvtaleFactory();

    private final AvtaleId annanAvtale = avtaleId(223344L);
    private final AvtaleId avtaleId = avtaleId(123456L);
    private final Satser<Prosent> standardProsentsatsar = new Satser<>(
            prosent("20%"),
            prosent("4%"),
            prosent("2.35%")
    );

    private final Satser<Kroner> standardBeloepsatsar = new Satser<>(
            kroner(535),
            kroner(0),
            kroner(35)
    );

    /**
     * Verifiserer at avtaleversjonar tilhøyrande andre avtalar ikkje populerer avtalen med nokon verdiar frå desse
     * avtaleversjonane.
     */
    @Test
    public void skalIgnorereAvtaleversjonarTilhoeyrandeAndreAvtalar() {
        assertPremiestatus(
                lagAvtale(
                        enAvtaleversjon(annanAvtale)
                                .premiestatus(Premiestatus.AAO_01)
                                .bygg()
                )
        ).isEqualTo(UKJENT);
    }

    @Test
    public void skalIgnorereAvtaleproduktTilhoeyrandeAndreAvtalar() {
        final Avtale avtale = lagAvtale(
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        annanAvtale,
                        PEN,
                        new Produktinfo(11),
                        standardProsentsatsar)
        );
        assertThat(avtale.premiesatsFor(PEN)).as("premiesatsar for " + PEN).isEqualTo(empty());
    }

    @Test
    public void skalPopulerePremiestatusFraUnderlagsperiodasAvtaleversjon() {
        assertPremiestatus(
                lagAvtale(
                        enAvtaleversjon(avtaleId)
                                .premiestatus(Premiestatus.AAO_01)
                                .bygg()
                )
        ).isEqualTo(AAO_01);
    }

    @Test
    public void skalIkkePopulereOrdningFraUnderlagsperiodeFraAnnenAvtale() {
        assertThat(
                lagAvtale(
                        avtaleperiode(AvtaleId.avtaleId(avtaleId.id() + 1))
                                .fraOgMed(tidenesMorgen())
                                .tilOgMed(empty())
                                .arbeidsgiverId(ArbeidsgiverId.valueOf(1))
                                .ordning(of(Ordning.SPK))
                                .bygg()
                ).ordning()
        ).isEmpty();
    }

    @Test
    public void skalPopulereOrdningFraUnderlagsperiodasAvtaleperiode() {
        assertThat(
                lagAvtale(
                        avtaleperiode(avtaleId)
                                .fraOgMed(tidenesMorgen())
                                .tilOgMed(empty())
                                .arbeidsgiverId(ArbeidsgiverId.valueOf(1))
                                .ordning(of(Ordning.SPK))
                                .bygg()
                ).ordning()
        ).contains(Ordning.SPK);
    }

    @Test
    public void skalPopulereAvtalenPremiesatsFraUnderlagsperiodasAvtaleprodukt() {
        final Avtale avtale = lagAvtale(
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        AFP,
                        new Produktinfo(40),
                        standardProsentsatsar
                )
        );

        final Optional<Premiesats> premiesats = avtale.premiesatsFor(AFP);
        assertThat(premiesats.map(p -> p.produktinfo))
                .as("produktinfo for AFP fra avtale " + avtale)
                .isEqualTo(of(new Produktinfo(40)));

        assertThat(premiesats.flatMap(p -> p.satser.somProsent()).map(Satser::arbeidsgiverpremie))
                .as("arbeidsgiversats for AFP fra avtale " + avtale)
                .isEqualTo(of(standardProsentsatsar.arbeidsgiverpremie()));

        assertThat(premiesats.flatMap(p -> p.satser.somProsent()).map(Satser::medlemspremie))
                .as("medlemssats for AFP fra avtale " + avtale)
                .isEqualTo(of(standardProsentsatsar.medlemspremie()));

        assertThat(premiesats.flatMap(p -> p.satser.somProsent()).map(Satser::administrasjonsgebyr))
                .as("administrasjonsgebyr for AFP fra avtale " + avtale)
                .isEqualTo(of(standardProsentsatsar.administrasjonsgebyr()));
    }

    @Test
    public void skalPopulerePremiesatsarForKvarAvUnderlagsperiodasAvtaleprodukt() {
        final Avtale avtale = lagAvtale(
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        PEN,
                        new Produktinfo(10),
                        standardProsentsatsar
                ),
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        AFP,
                        new Produktinfo(40),
                        standardProsentsatsar
                ),
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        TIP,
                        new Produktinfo(94),
                        standardProsentsatsar
                ),
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        GRU,
                        new Produktinfo(35),
                        standardBeloepsatsar
                ),
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        YSK,
                        new Produktinfo(70),
                        standardBeloepsatsar
                )
        );
        assertPremiesats(avtale, PEN).isNotEqualTo(empty());
        assertPremiesats(avtale, AFP).isNotEqualTo(empty());
        assertPremiesats(avtale, TIP).isNotEqualTo(empty());
        assertPremiesats(avtale, GRU).isNotEqualTo(empty());
        assertPremiesats(avtale, YSK).isNotEqualTo(empty());
    }

    @Test
    public void skalFeileDersomUnderlagsperiodaErTilkoblaMeirEnnEinAvtaleversjonTilhoeyrandeAvtalen() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Underlagsperioda er kobla til meir enn ei tidsperiode av type");
        e.expectMessage(Avtaleversjon.class.getSimpleName());

        lagAvtale(
                enAvtaleversjon(avtaleId)
                        .fraOgMed(dato("1917.01.01"))
                        .bygg(),
                enAvtaleversjon(avtaleId)
                        .fraOgMed(dato("1947.01.01"))
                        .bygg()
        );
    }

    @Test
    public void skalFeileDersomUnderlagsperiodaErTilkoblaMeirEnnEitAvtaleproduktMedLikeProdukt() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Avtale");
        e.expectMessage(avtaleId.id().toString());
        e.expectMessage("har meir enn ein premiesats for produkt ");
        e.expectMessage(AFP.kode());
        e.expectMessage("- produkt=AFP, produktinfo=40");
        e.expectMessage("- produkt=AFP, produktinfo=42");

        lagAvtale(
                new Avtaleprodukt(
                        dato("1917.01.01"),
                        empty(),
                        avtaleId,
                        AFP,
                        new Produktinfo(40),
                        standardProsentsatsar
                ),
                new Avtaleprodukt(
                        dato("1947.01.01"),
                        empty(),
                        avtaleId,
                        AFP,
                        new Produktinfo(42),
                        standardProsentsatsar
                )
        );
    }

    private Avtale lagAvtale(final Tidsperiode<?>... koblingar) {
        return factory.lagAvtale(
                eiPeriode()
                        .medKoblingar(
                                koblingar
                        )
                        .bygg(),
                avtaleId
        );
    }

    private static UnderlagsperiodeBuilder eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("1990.01.01"))
                .tilOgMed(dato("1990.12.31"));
    }

    private static AbstractObjectAssert<?, Premiestatus> assertPremiestatus(Avtale avtale) {
        return assertThat(avtale.premiestatus()).as("premiestatus fra avtale " + avtale);
    }

    private static OptionalAssert<Premiesats> assertPremiesats(Avtale avtale, Produkt produkt) {
        return assertThat(avtale.premiesatsFor(produkt)).as("premiesatsar for " + produkt + " fra avtale " + avtale);
    }
}