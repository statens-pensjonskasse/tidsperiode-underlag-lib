package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.AFP;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.TIP;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import no.spk.pensjon.faktura.tidsserie.domain.at.UnderlagsperiodeDefinisjonar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.FakturerbareDagsverkGRURegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.FakturerbareDagsverkYSKRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import cucumber.api.java8.No;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Stegdefinisjonar for oppsett av underlagsperioder brukt til og verifisering av resultat
 * generert av reglar for avregning.
 */
@ContextConfiguration(classes = AvregningDefinisjonar.SpringConfiguration.class)
public class AvregningDefinisjonar implements No {
    @Configuration
    public static class SpringConfiguration {
    }

    @Autowired
    private UnderlagsperiodeDefinisjonar periode;

    private final Map<Produkt, Class<? extends BeregningsRegel<Premier>>> premieRegelForProdukt = new HashMap<>();

    public AvregningDefinisjonar() {
        premieRegelForProdukt.put(PEN, PENPremieRegel.class);
        premieRegelForProdukt.put(AFP, AFPPremieRegel.class);
        premieRegelForProdukt.put(TIP, TIPPremieRegel.class);
        premieRegelForProdukt.put(GRU, GRUPremieRegel.class);
        premieRegelForProdukt.put(YSK, YSKPremieRegel.class);

        Gitt("^(?:en underlagsperiode med |at )?premiesats er lik ([^%]+ *)%, ([^%]+ *)% og ([^%]+ *)% for produkt (PEN|AFP|TIP)$", (String medlem, String arbeidsgiver, String administrasjonsgebyr, String produkt) -> {
            periode().med(
                    Avtale.class,
                    avtale(avtaleId(100000L))
                            .addPremiesats(
                                    premiesats(Produkt.valueOf(produkt))
                                            .satser(
                                                    new Satser<>(
                                                            prosent(arbeidsgiver),
                                                            prosent(medlem),
                                                            prosent(administrasjonsgebyr)
                                                    )
                                            )
                                            .produktinfo(new Produktinfo(10))
                                            .bygg()
                            )
                            .bygg()
            );
        });

        Gitt("^(?:en underlagsperiode med |at )?premiesats er lik kr ([^%]+ *), kr ([^%]+ *) og kr ([^%]+ *) for produkt (YSK|GRU)$", (String medlem, String arbeidsgiver, String administrasjonsgebyr, String produkt) -> {
            periode().med(
                    Avtale.class,
                    avtale(avtaleId(100000L))
                            .addPremiesats(
                                    premiesats(Produkt.fraKode(produkt))
                                            .satser(
                                                    new Satser<>(
                                                            kroner(parseInt(arbeidsgiver)),
                                                            kroner(parseInt(medlem)),
                                                            kroner(parseInt(administrasjonsgebyr))
                                                    )
                                            )
                                            .produktinfo(new Produktinfo(10))
                                            .bygg()
                            )
                            .bygg()
            );
        });

        Så("^skal totalt premiebeløp for produkt (PEN|AFP|TIP|YSK|GRU) være lik (kr -?[0-9 .]+)$", (String produkttype, String totalPremie) -> {

            final Underlagsperiode p = bygg();

            final Produkt produkt = Produkt.fraKode(produkttype);
            assertThat(ofNullable(premieRegelForProdukt.get(produkt)).map(r -> p.beregn(r).total()))
                    .as("totalt premiebeløp beregna for produkt " + produkt + " basert på underlagsperiode " + p)
                    .isEqualTo(of(premiebeloep(totalPremie)));
        });
    }

    protected Underlagsperiode bygg() {
        return periode()
                .med(new PENPremieRegel())
                .med(new AFPPremieRegel())
                .med(new TIPPremieRegel())
                .med(new GRUPremieRegel())
                .med(new YSKPremieRegel())
                .med(new FakturerbareDagsverkGRURegel())
                .med(new FakturerbareDagsverkYSKRegel())
                .bygg();
    }

    private UnderlagsperiodeBuilder periode() {
        return periode.builder();
    }
}
