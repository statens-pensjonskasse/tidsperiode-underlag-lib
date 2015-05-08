package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertFraOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.Assertions.assertTilOgMed;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.assertBetalarTilSPKFor;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.assertPremiestatus;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.eiAvtalekobling;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.eiOrdning;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.eiStilling;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.enAvtale;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarFactoryTestHelpers.tidenesMorgen;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import org.junit.Test;

public class MedlemsavtalarFactoryTest {
    private final MedlemsavtalarFactoryTestHelpers this_ = new MedlemsavtalarFactoryTestHelpers();

    @Test
    public void skalPeriodisereVedEndringPaaAvtalekobling() {
        final LocalDate endringsdato = tidenesMorgen().plusYears(50);
        final AvtaleId nyAvtale = avtaleId(enAvtale().id() + 1000);

        final List<MedlemsavtalarPeriode> perioder = periodiser(
                new Avtalekoblingsperiode(
                        tidenesMorgen(),
                        of(endringsdato.minusDays(1)),
                        eiStilling(),
                        enAvtale(),
                        eiOrdning()
                ),
                new Avtalekoblingsperiode(
                        endringsdato,
                        empty(),
                        eiStilling(),
                        nyAvtale,
                        eiOrdning()
                )
        );
        assertThat(perioder).hasSize(2);

        assertTilOgMed(perioder.get(0)).isEqualTo(of(endringsdato.minusDays(1)));
        assertThat(perioder.get(0).avtaleFor(eiStilling()).id()).isEqualTo(enAvtale());

        assertFraOgMed(perioder.get(1)).isEqualTo(endringsdato);
        assertThat(perioder.get(1).avtaleFor(eiStilling()).id()).isEqualTo(nyAvtale);
    }

    @Test
    public void skalPeriodisereVedFjerningAvAvtaleproduktFraaAvtale() {
        final LocalDate endringsdato = tidenesMorgen().plusYears(50);

        addAvtaleinformasjon(
                enAvtale(),
                new Avtaleprodukt(
                        tidenesMorgen(),
                        of(endringsdato.minusDays(1)
                        ),
                        enAvtale(),
                        PEN,
                        36,
                        empty(),
                        empty()
                )
        );

        final List<MedlemsavtalarPeriode> perioder = periodiser(eiAvtalekobling(eiStilling()));
        assertThat(perioder).hasSize(2);

        assertTilOgMed(perioder.get(0)).isEqualTo(of(endringsdato.minusDays(1)));
        assertBetalarTilSPKFor(perioder.get(0), eiStilling(), PEN).isTrue();

        assertFraOgMed(perioder.get(1)).isEqualTo(endringsdato);
        assertBetalarTilSPKFor(perioder.get(1), eiStilling(), PEN).isFalse();
    }

    @Test
    public void skalPeriodisereVedInnleggingAvNyttAvtaleproduktPaaAvtale() {
        final LocalDate endringsdato = tidenesMorgen().plusYears(47);

        addAvtaleinformasjon(
                enAvtale(),
                new Avtaleprodukt(
                        tidenesMorgen(),
                        empty(),
                        enAvtale(),
                        PEN,
                        36,
                        empty(),
                        empty()
                ),
                new Avtaleprodukt(
                        endringsdato,
                        empty(),
                        enAvtale(),
                        GRU,
                        36,
                        empty(),
                        empty()
                )
        );

        final List<MedlemsavtalarPeriode> perioder = periodiser(eiAvtalekobling(eiStilling()));
        assertThat(perioder).hasSize(2);

        assertTilOgMed(perioder.get(0)).isEqualTo(of(endringsdato.minusDays(1)));
        assertFraOgMed(perioder.get(1)).isEqualTo(endringsdato);

        assertBetalarTilSPKFor(perioder.get(0), eiStilling(), PEN).isTrue();
        assertBetalarTilSPKFor(perioder.get(1), eiStilling(), PEN).isTrue();

        assertBetalarTilSPKFor(perioder.get(0), eiStilling(), GRU).isFalse();
        assertBetalarTilSPKFor(perioder.get(1), eiStilling(), GRU).isTrue();
    }

    @Test
    public void skalPeriodisereVedEndringAvAvtaleproduktPaaAvtale() {
        final LocalDate endringsdato = tidenesMorgen().plusYears(47);

        addAvtaleinformasjon(
                enAvtale(),
                new Avtaleprodukt(
                        tidenesMorgen(),
                        empty(),
                        enAvtale(),
                        PEN,
                        36,
                        empty(),
                        empty()
                ),
                new Avtaleprodukt(
                        endringsdato,
                        empty(),
                        enAvtale(),
                        PEN,
                        37,
                        empty(),
                        empty()
                )
        );

        final List<MedlemsavtalarPeriode> perioder = periodiser(eiAvtalekobling(eiStilling()));
        assertThat(perioder).hasSize(2);

        assertTilOgMed(perioder.get(0)).isEqualTo(of(endringsdato.minusDays(1)));
        assertFraOgMed(perioder.get(1)).isEqualTo(endringsdato);

        assertBetalarTilSPKFor(perioder.get(0), eiStilling(), PEN).isTrue();
        assertBetalarTilSPKFor(perioder.get(1), eiStilling(), PEN).isTrue();
    }

    @Test
    public void skalPeriodisereVedEndringAvPremiestatusPaaAvtale() {
        final LocalDate endringsdato = tidenesMorgen().plusYears(50);
        addAvtaleinformasjon(
                enAvtale(),
                new Avtaleversjon(
                        tidenesMorgen(),
                        of(endringsdato.minusDays(1)),
                        enAvtale(),
                        Premiestatus.AAO_01
                ),
                new Avtaleversjon(
                        endringsdato,
                        empty(),
                        enAvtale(),
                        Premiestatus.AAO_02
                )
        );
        final List<MedlemsavtalarPeriode> perioder = periodiser(eiAvtalekobling(eiStilling()));
        assertThat(perioder).hasSize(2);

        assertTilOgMed(perioder.get(0)).isEqualTo(of(endringsdato.minusDays(1)));
        assertPremiestatus(perioder.get(0), eiStilling()).isEqualTo(Premiestatus.AAO_01);

        assertFraOgMed(perioder.get(1)).isEqualTo(endringsdato);
        assertPremiestatus(perioder.get(1), eiStilling()).isEqualTo(Premiestatus.AAO_02);
    }

    private void addAvtaleinformasjon(final AvtaleId avtale, final Tidsperiode<?>... perioder) {
        this_.avtaleperioderFor(avtale).addAll(asList(perioder));
    }

    private List<MedlemsavtalarPeriode> periodiser(final Avtalekoblingsperiode... koblingar) {
        return this_.periodiser(asList(koblingar));
    }
}