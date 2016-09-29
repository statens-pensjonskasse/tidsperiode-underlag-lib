package no.spk.pensjon.faktura.tidsserie.domain.avregning;


import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Assertions.assertPremiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class GRUPremieRegelTest {

    private UnderlagsperiodeBuilder builder;

    private GRUPremieRegel regel = new GRUPremieRegel();

    @Before
    public void setUp() throws Exception {
        Aarstall premieaar = new Aarstall(2015);
        builder = new UnderlagsperiodeBuilder()
                .fraOgMed(premieaar.atStartOfYear())
                .tilOgMed(premieaar.atEndOfYear())
                .med(Aarstall.class, premieaar)
                .med(AntallDagarRegel.class, new AntallDagarRegel())
                .med(AarsLengdeRegel.class, new AarsLengdeRegel())
                .med(AarsfaktorRegel.class, new AarsfaktorRegel());
    }

    @Test
    public void skal_ha_0_kr_i_premie_paa_periodenivaa() throws Exception {
        assertPremiebeloep(
                beregn(builder
                        .med(
                                Avtale.class,
                                enAvtaleMedGRU(
                                        new Satser<>(
                                                kroner(100),
                                                kroner(100),
                                                kroner(100)
                                        )
                                ).bygg()
                        )
                        .med(GruppelivsfaktureringRegel.class, gruppelivsandel(prosent("10%")))
                ).total(),
                2
        ).isEqualTo(premiebeloep("kr 0"));
    }


    private Premier beregn(final UnderlagsperiodeBuilder builder) {
        return regel.beregn(builder.bygg());
    }

    private GruppelivsfaktureringRegel gruppelivsandel(final Prosent faktureringsandel) {
        return new GruppelivsfaktureringRegel() {
            @Override
            public FaktureringsandelStatus beregn(Beregningsperiode<?> periode) {
                return new FaktureringsandelStatus(StillingsforholdId.valueOf(1), faktureringsandel);
            }
        };
    }

    private static Avtale.AvtaleBuilder enAvtaleMedGRU(final Satser<Kroner> satser) {
        return avtale(avtaleId(240500))
                .addPremiesats(
                        premiesats(GRU)
                                .produktinfo(Produktinfo.GRU_35)
                                .satser(
                                        satser
                                )
                                .bygg()
                );
    }
}