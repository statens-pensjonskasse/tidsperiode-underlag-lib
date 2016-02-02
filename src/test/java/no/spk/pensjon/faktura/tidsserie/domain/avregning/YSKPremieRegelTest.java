package no.spk.pensjon.faktura.tidsserie.domain.avregning;


import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Assertions.assertPremiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class YSKPremieRegelTest {

    private UnderlagsperiodeBuilder builder;

    private YSKPremieRegel regel = new YSKPremieRegel();

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
    public void skal_benytte_yrkesskadefaktureringRegel_for_aa_beregne_premie() throws Exception {
        assertPremiebeloep(
                beregn(builder
                        .med(
                                Avtale.class,
                                enAvtaleMedYSK(
                                        new Satser<>(
                                                kroner(100),
                                                kroner(100),
                                                kroner(100)
                                        )
                                ).bygg()
                        )
                        .med(YrkesskadefaktureringRegel.class, yrkesskadeandel(prosent("50%")))
                ).total(),
                2
        ).isEqualTo(premiebeloep("kr 150"));
    }

    @Test
    public void skal_benytte_aarsfaktor_for_aa_beregne_premie() throws Exception {
        assertPremiebeloep(
                beregn(builder
                        .med(
                                Avtale.class,
                                enAvtaleMedYSK(
                                        new Satser<>(
                                                kroner(100),
                                                kroner(0),
                                                kroner(0)
                                        )
                                ).bygg()
                        )
                        .med(AarsfaktorRegel.class, aarsfaktor(0.5))
                        .med(YrkesskadefaktureringRegel.class, yrkesskadeandel(prosent("100%")))
                ).total(),
                2
        ).isEqualTo(premiebeloep("kr 50"));
    }

    private Premier beregn(final UnderlagsperiodeBuilder builder) {
        return regel.beregn(builder.bygg());
    }

    private AarsfaktorRegel aarsfaktor(final double aarsfaktor) {
        return new AarsfaktorRegel(){
            @Override
            public Aarsfaktor beregn(Beregningsperiode<?> periode) {
                return new Aarsfaktor(aarsfaktor);
            }
        };
    }

    private YrkesskadefaktureringRegel yrkesskadeandel(final Prosent faktureringsandel) {
        return new YrkesskadefaktureringRegel() {
            @Override
            public FaktureringsandelStatus beregn(Beregningsperiode<?> periode) {
                return new FaktureringsandelStatus(StillingsforholdId.valueOf(1), faktureringsandel);
            }
        };
    }

    private static Avtale.AvtaleBuilder enAvtaleMedYSK(final Satser<Kroner> satser) {
        return avtale(avtaleId(240500))
                .addPremiesats(
                        premiesats(YSK)
                                .produktinfo(new Produktinfo(77))
                                .satser(
                                        satser
                                )
                                .bygg()
                );
    }
}