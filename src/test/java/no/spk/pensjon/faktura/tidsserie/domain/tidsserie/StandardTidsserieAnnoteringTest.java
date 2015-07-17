package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn.loennstrinn;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning.POA;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning.SPK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer.personnummer;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.PEN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode.K_STIL_APO_PROVISOR;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder.grupper;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.ObjectMother.eiMedregning;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.enAvtaleversjon;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Month;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverdataperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Produktinfo;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Orgnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.ApotekLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Omregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.StatligLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StandardTidsserieAnnoteringTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private final StandardTidsserieAnnotering annotator = new StandardTidsserieAnnotering();

    private final Underlag underlag = mock(Underlag.class);

    @Before
    public void _before() {
        when(underlag.last()).thenReturn(empty());
    }

    /**
     * Verifiserer at {@link MedlemsavtalarPeriode#annoter(Annoterbar)} blir kalla
     * dersom underlagsperioda er tilkobla ei periode av denne typen.
     */
    @Test
    public void skalAnnotereMedMedlemsavtalarFraaMedlemsavtalane() {
        Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                medlemsavtalar()
                                        .fraOgMed(dato("1917.01.01"))
                                        .addAvtale(
                                                stillingsforhold(123456L),
                                                avtale(avtaleId(200300))
                                                        .addPremiesats(
                                                                premiesats(Produkt.PEN)
                                                                        .produktinfo(new Produktinfo(10))
                                                                        .satser(new Satser<>(prosent("10%"), prosent("2%"), prosent("0.35%")))
                                                                        .bygg()
                                                        )
                                        )
                                        .bygg()
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Medlemsavtalar.class).isNotEqualTo(empty());
    }

    /**
     * Verifiserer at perioda blir annotert med ein {@link Avtale} basert på avtalen avtalekoblinga er tilknytta og
     * avtaleversjonen og avtaleprodukta som tilhøyrer den avtalen.
     */
    @Test
    public void skalAnnotereMedAvtaleBygdOppBasertPaaAvtaleversjonOgAvtaleproduktaTilknyttaAvtalekoblingasAvtale() {
        final AvtaleId avtale = avtaleId(123456L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("1917.01.01"),
                                        empty(),
                                        stillingsforhold(1L),
                                        avtale,
                                        Ordning.SPK
                                )
                        )
                        .medKobling(
                                enAvtaleversjon(avtale)
                                        .premiestatus(Premiestatus.AAO_02)
                                        .bygg()
                        )
                        .medKobling(
                                new Avtaleprodukt(
                                        dato("1917.01.01"),
                                        empty(),
                                        avtale,
                                        PEN,
                                        new Produktinfo(10),
                                        new Satser<>(
                                                prosent("10%"),
                                                prosent("2%"),
                                                prosent("0.35%")
                                        ))
                        )

        );
        final Underlagsperiode periode = underlag.toList().get(0);
        assertAnnotasjon(periode, Avtale.class, a -> of(a.id()))
                .isEqualTo(of(avtale));
        assertAnnotasjon(periode, Avtale.class, a -> of(a.premiestatus()))
                .isEqualTo(of(Premiestatus.AAO_02));
        assertAnnotasjon(periode, Avtale.class, a -> a.premiesatsFor(PEN).map(s -> s.produktinfo))
                .isEqualTo(of(new Produktinfo(10)));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med aktivestillingar frå medlemsperiodene.
     */
    @Test
    public void skalAnnotereMedAktiveStillingaFraaMedlemsperioda() {
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new Medlemsperiode(dato("2015.01.01"), empty())
                                        .kobleTil(
                                                Stream.of(
                                                        new StillingsforholdPeriode(dato("2015.01.01"), empty())
                                                                .leggTilOverlappendeStillingsendringer(
                                                                        new Stillingsendring()
                                                                                .stillingsprosent(fulltid())
                                                                                .aksjonsdato(dato("2015.01.01"))
                                                                )
                                                )
                                        )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), AktiveStillingar.class).isNotEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med medregningskode frå stillingsforholdperioder tilknytta
     * medregning.
     */
    @Test
    public void skalAnnotereMedMedregningskodeFråStillingsforholdperioderTilknyttaMedregning() {
        final Medregningskode expected = Medregningskode.BISTILLING;
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new StillingsforholdPeriode(
                                        eiMedregning()
                                                .kode(expected)
                                                .bygg()
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Medregningskode.class).isEqualTo(of(expected));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med medregna beløp frå stillingsforholdperioder tilknytta
     * medregning.
     */
    @Test
    public void skalAnnotereMedMedregningsbeløpFråStillingsforholdperioderTilknyttaMedregning() {
        final Medregning expected = new Medregning(kroner(10_000));
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new StillingsforholdPeriode(
                                        eiMedregning()
                                                .beloep(expected)
                                                .bygg()
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Medregning.class).isEqualTo(of(expected));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med aksjonskode frå stillingsforholdperioda dei overlappar.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedAksjonskodeFraGjeldandeStillingsendring() {
        final Aksjonskode aksjonskode = Aksjonskode.NYTILGANG;
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("2005.08.15"))
                        .tilOgMed(dato("2005.08.31"))
                        .medKobling(
                                new StillingsforholdPeriode(dato("2005.08.15"), empty())
                                        .leggTilOverlappendeStillingsendringer(
                                                eiStillingsendring()
                                                        .stillingsprosent(fulltid())
                                                        .aksjonsdato(dato("2005.08.15"))
                                                        .aksjonskode(aksjonskode)
                                        )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Aksjonskode.class).isEqualTo(of(aksjonskode));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med stillingsforhold frå stillingsforholdperioder tilknytta
     * medregning.
     */
    @Test
    public void skalAnnotereMedStillingsforholdFraStillingsforholdperioderTilknyttaMedregning() {
        final StillingsforholdId expected = new StillingsforholdId(1L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new StillingsforholdPeriode(
                                        eiMedregning()
                                                .stillingsforhold(expected)
                                                .bygg()
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), StillingsforholdId.class).isEqualTo(of(expected));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med stillingsforhold frå stillingsforholdperioda dei overlappar
     * når den er tilknytta stillingsendring.
     */
    @Test
    public void skalAnnotereUnderlagsperioderMedStillingsforholdFraGjeldandeStillingsendring() {
        final StillingsforholdId expected = new StillingsforholdId(29292L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("2005.08.15"))
                        .tilOgMed(dato("2005.08.31"))
                        .medKobling(
                                new StillingsforholdPeriode(dato("2005.08.15"), empty())
                                        .leggTilOverlappendeStillingsendringer(
                                                eiStillingsendring()
                                                        .stillingsprosent(fulltid())
                                                        .aksjonsdato(dato("2005.08.15"))
                                                        .stillingsforhold(expected)
                                        )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), StillingsforholdId.class).isEqualTo(of(expected));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med premiestatus dersom perioda er tilknytta ein
     * avtaleversjon som har ein premiestatus.
     */
    @Test
    public void skalAnnoterePeriodeMedPremiestatusFraOverlappandeAvtaleversjon() {
        final AvtaleId avtale = avtaleId(123456);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKoblingar(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        avtale,
                                        Ordning.POA
                                ),
                                enAvtaleversjon(avtale)
                                        .premiestatus(Premiestatus.AAO_02)
                                        .bygg()
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Premiestatus.class)
                .isEqualTo(of(Premiestatus.AAO_02));
    }

    /**
     * Verifiserer at underlagsperiodene blir annotert med premiekategori dersom perioda er tilknytta ein
     * avtaleversjon som har ein premiekategori.
     */
    @Test
    public void skalAnnoterePeriodeMedPremiekategoriFraOverlappandeAvtaleversjon() {
        final AvtaleId avtale = avtaleId(123456);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKoblingar(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        avtale,
                                        Ordning.POA
                                ),
                                enAvtaleversjon(avtale)
                                        .premiekategori(Premiekategori.FASTSATS_AARLIG_OPPFOELGING)
                                        .bygg()
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Premiekategori.class)
                .isEqualTo(of(Premiekategori.FASTSATS_AARLIG_OPPFOELGING));
    }

    /**
     * Verifiserer at underlagsperiodene ikkje blir annotert med premiestatus og premiekategori dersom perioda ikkje
     * er tilknytta ein avtaleversjon.
     */
    @Test
    public void skalIkkjeAnnoterePeriodeMedPremiestatusEllerKategoriDersomPeriodaIkkjeOverlapparEinAvtaleversjon() {
        final AvtaleId avtale = avtaleId(123456);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKoblingar(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        avtale,
                                        Ordning.POA
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Premiestatus.class)
                .isEqualTo(empty());
        assertAnnotasjon(underlag.toList().get(0), Premiekategori.class)
                .isEqualTo(empty());
    }

    /**
     * Verifiserer at annoteringa feilar dersom ei periode er tilknytta meir enn ein avtaleversjon.
     */
    @Test
    public void skalFeileDersomUnderlagsperiodeOverlapparMeirEnnEinAvtaleversjon() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Klarer ikkje å entydig avgjere kva som er gjeldande Avtaleversjon");
        AvtaleId avtale = new AvtaleId(12345L);
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKoblingar(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        of(dato("2011.06.30")),
                                        new StillingsforholdId(999999L),
                                        avtale,
                                        SPK
                                ),
                                enAvtaleversjon(avtale).bygg(),
                                enAvtaleversjon(avtale).bygg()
                        )
        );
    }


    @Test
    public void skalAnnotereGrunnbeloepFraOmregningsperiode() {
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .medKobling(
                                new Omregningsperiode(
                                        dato("1985.01.01"),
                                        empty(),
                                        kroner(10_000)
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Grunnbeloep.class)
                .isEqualTo(of(new Grunnbeloep(kroner(10_000))));
    }

    @Test
    public void skalAnnotereStillingskode() {
        final Stillingskode stillingskode = K_STIL_APO_PROVISOR;
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("1990.01.01"))
                        .medKobling(
                                new StillingsforholdPeriode(
                                        dato("1990.01.01"),
                                        empty()
                                )
                                        .leggTilOverlappendeStillingsendringer(
                                                eiStillingsendring()
                                                        .aksjonsdato(dato("1990.01.01"))
                                                        .stillingskode(of(stillingskode))
                                        )
                        )
        );

        assertAnnotasjon(underlag.toList().get(0), Stillingskode.class)
                .isEqualTo(of(stillingskode));
    }

    /**
     * Verifiserer at oppslag av lønnstrinnbeløp for apotekordninga fungerer viss ein har informasjon om både
     * lønnstrinn, stillingskode og lønnstrinnperiode for apotekordninga for avtalar tilknytta Apotekordninga.
     */
    @Test
    public void skalAnnotereLoennstrinnBeloepForApotekOrdninga() {
        final Loennstrinn loennstrinn = new Loennstrinn(12);
        final Stillingskode stillingskode = K_STIL_APO_PROVISOR;
        final LoennstrinnBeloep beloep = new LoennstrinnBeloep(
                new Kroner(10_000)
        );
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("1990.01.01"))
                        .medKobling(
                                new StillingsforholdPeriode(
                                        dato("1990.01.01"),
                                        empty()
                                )
                                        .leggTilOverlappendeStillingsendringer(
                                                eiStillingsendring()
                                                        .aksjonsdato(dato("1990.01.01"))
                                                        .stillingskode(of(stillingskode))
                                                        .loennstrinn(of(loennstrinn))
                                        )
                        )
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        new AvtaleId(123456L),
                                        Ordning.POA
                                )
                        )
                        .medKoblingar(
                                Loennstrinnperioder.grupper(
                                        POA,
                                        new ApotekLoennstrinnperiode(
                                                dato("1989.04.01"),
                                                empty(),
                                                loennstrinn,
                                                stillingskode,
                                                beloep
                                        )
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), LoennstrinnBeloep.class)
                .isEqualTo(of(beloep));
    }

    /**
     * Verifiserer at et stillingsforhold som er kynttet til en avtale som er knyttet en arbeidsgiver
     * annoterer arbeidsgiverid på underlagsperioden
     */
    @Test
    public void skalAnnotereArbeidsgiveridVedTilkobletArbeidsgiverperiode() {
        final ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(1L);
        final AvtaleId avtaleId = new AvtaleId(123456L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("1990.01.01"))
                        .medKobling(
                                new StillingsforholdPeriode(
                                        dato("1990.01.01"),
                                        empty()
                                )
                        )
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        avtaleId,
                                        Ordning.POA
                                )
                        )
                        .medKobling(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtaleId, arbeidsgiverId)
                        )
                        .medKobling(
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId)
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), ArbeidsgiverId.class).isEqualTo(of(arbeidsgiverId));
    }

    /**
     * Verifiserer at et stillingsforhold som er kynttet til en avtale som er knyttet til kundedata via arbeidsgiverid
     * annoterer Orgnummer på underlagsperioden
     */
    @Test
    public void skalAnnotereOrgnummerVedTilkobletKundedataperiode() {
        final ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(1L);
        final Orgnummer orgnummer = new Orgnummer(123456789L);
        final AvtaleId avtaleId = new AvtaleId(123456L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("1990.01.01"))
                        .medKobling(
                                new StillingsforholdPeriode(
                                        dato("1990.01.01"),
                                        empty()
                                )
                        )
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        empty(),
                                        new StillingsforholdId(1L),
                                        avtaleId,
                                        Ordning.POA
                                )
                        )
                        .medKobling(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtaleId, arbeidsgiverId)
                        )
                        .medKobling(
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId)
                        )
                        .medKobling(
                                new Arbeidsgiverdataperiode(dato("1990.01.01"), empty(), orgnummer, arbeidsgiverId)
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), Orgnummer.class).isEqualTo(of(orgnummer));
    }

    /**
     * Verifiserer at et stillingsforhold som gjennomgått avtalebytte slik at stillingsforholdet har kobling
     * til overlappende arbeidsgiverperioder, så skal kun arbeidsgiverperioden som tilhører gjeldende avtale
     * benyttes for å annotere underlagsperioden
     */
    @Test
    public void skalAnnotereArbeidsgiverIdKunForArbeidsgiverSomTilhorerAvtalenForPerioden() {
        final ArbeidsgiverId arbeidsgiverId1 = new ArbeidsgiverId(1L);
        final ArbeidsgiverId arbeidsgiverId2 = new ArbeidsgiverId(2L);
        final AvtaleId avtaleId1 = new AvtaleId(123456L);
        final AvtaleId avtaleId2 = new AvtaleId(654321L);
        final Orgnummer orgnummer1 = new Orgnummer(123456789L);
        final Orgnummer orgnummer2 = new Orgnummer(987654321L);
        final Underlag underlag = annoterAllePerioder(
                eiPeriode()
                        .fraOgMed(dato("1990.01.01"))
                        .medKobling(
                                new StillingsforholdPeriode(
                                        dato("1990.01.01"),
                                        empty()
                                )
                        )
                        .medKoblingar(
                                new Avtalekoblingsperiode(
                                        dato("1990.01.01"),
                                        of(dato("1999.12.31")),
                                        new StillingsforholdId(1L),
                                        avtaleId1,
                                        Ordning.POA
                                )
                        )
                        .medKoblingar(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtaleId1, arbeidsgiverId1),
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtaleId2, arbeidsgiverId2)
                        )
                        .medKoblingar(
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId1),
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId2)
                        )
                        .medKoblingar(
                                new Arbeidsgiverdataperiode(dato("1990.01.01"), empty(), orgnummer1, arbeidsgiverId1),
                                new Arbeidsgiverdataperiode(dato("1990.01.01"), empty(), orgnummer2, arbeidsgiverId2)
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), ArbeidsgiverId.class).isEqualTo(of(arbeidsgiverId1));
        assertAnnotasjon(underlag.toList().get(0), Orgnummer.class).isEqualTo(of(orgnummer1));
    }

    @Test
    public void skalAnnotereUnderlagsperioderMedFunksjonstillegg() {
        final Optional<Funksjonstillegg> expected = of(new Funksjonstillegg(kroner(1_240)));
        assertAnnotasjon(
                annoter(
                        eiTomPeriode()
                                .fraOgMed(dato("2014.01.01"))
                                .tilOgMed(dato("2014.01.31"))
                                .medKobling(
                                        new StillingsforholdPeriode(dato("2012.09.01"), empty())
                                                .leggTilOverlappendeStillingsendringer(
                                                        eiStillingsendring()
                                                                .aksjonsdato(dato("2012.09.01"))
                                                                .stillingsprosent(fulltid())
                                                                .funksjonstillegg(
                                                                        expected
                                                                )
                                                )
                                )
                )
                , Funksjonstillegg.class)
                .isEqualTo(expected);
    }

    @Test
    public void skalAnnotereUnderlagsperioderMedFastetillegg() {
        final Optional<Fastetillegg> expected = of(new Fastetillegg(kroner(10_000)));
        assertAnnotasjon(
                annoter(
                        eiTomPeriode()
                                .fraOgMed(dato("2006.02.01"))
                                .tilOgMed(dato("2006.02.28"))
                                .medKobling(
                                        new StillingsforholdPeriode(dato("2005.08.15"), empty())
                                                .leggTilOverlappendeStillingsendringer(
                                                        eiStillingsendring()
                                                                .aksjonsdato(dato("2005.08.15"))
                                                                .stillingsprosent(fulltid())
                                                                .fastetillegg(
                                                                        expected
                                                                )
                                                )
                                )
                )
                , Fastetillegg.class)
                .isEqualTo(expected);
    }

    @Test
    public void skalAnnotereUnderlagsperioderMedVariabletillegg() {
        final Optional<Variabletillegg> expected = of(new Variabletillegg(kroner(10_000)));
        assertAnnotasjon(
                annoter(
                        eiTomPeriode()
                                .fraOgMed(dato("2007.01.01"))
                                .tilOgMed(dato("2007.01.31"))
                                .medKobling(
                                        new StillingsforholdPeriode(dato("2007.01.01"), empty())
                                                .leggTilOverlappendeStillingsendringer(
                                                        eiStillingsendring()
                                                                .aksjonsdato(dato("2007.01.01"))
                                                                .stillingsprosent(fulltid())
                                                                .variabletillegg(
                                                                        expected
                                                                )
                                                )
                                )
                )
                , Variabletillegg.class)
                .isEqualTo(expected);
    }

    @Test
    public void skalAnnotereUnderlagsperiodeMedAvtaleId() {
        final AvtaleId avtaleId = new AvtaleId(12345L);

        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        Optional.empty(),
                                        new StillingsforholdId(999999L),
                                        avtaleId,
                                        SPK
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), AvtaleId.class).isEqualTo(of(avtaleId));
    }

    /**
     * Verifiserer at annotering av {@link AvtaleId} feilar dersom underlagsperioda har meir enn ei
     * overlappande avtalekobling.
     */
    @Test
    public void skalAvbryteAnnoteringDersomUnderlagsperiodeErTilkoblaMeirEnnEiAvtalekobling() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Underlagsperioda er kobla til meir enn ei tidsperiode av type");
        e.expectMessage("vi forventa berre 1 kobling av denne typen");
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        of(dato("2011.06.30")),
                                        new StillingsforholdId(999999L),
                                        new AvtaleId(12345L),
                                        SPK
                                ))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.05.01"),
                                        Optional.empty(),
                                        new StillingsforholdId(999999L),
                                        new AvtaleId(54321L),
                                        SPK
                                )
                        )
        );
    }

    /**
     * Verifiserer at annotering av {@link ArbeidsgiverId} feilar dersom underlagsperioda har meir enn ei
     * overlappande avtaleperiode, men at avtaleId fremdeles blir annotert
     */
    @Test
    public void skalAvbryteAnnoteringDersomUnderlagsperiodeErTilkoblaMeirEnnEiAvtaleperiodeSomTilhoeyrerSammeAvtale() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Klarer ikkje å entydig avgjere kva som er gjeldande Avtaleperiode");
        AvtaleId avtale = new AvtaleId(12345L);
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        of(dato("2011.06.30")),
                                        new StillingsforholdId(999999L),
                                        avtale,
                                        SPK
                                ))
                        .medKoblingar(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtale, new ArbeidsgiverId(1L)),
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtale, new ArbeidsgiverId(2L))
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), AvtaleId.class).isEqualTo(of(avtale));
    }

    /**
     * Verifiserer at annotering av {@link ArbeidsgiverId} feilar dersom underlagsperioda har meir enn ei
     * overlappande arbeidsgiverperiode koblet til avtaleperioden. Avtaleid skal fremdeles bli annotert.
     */
    @Test
    public void skalAvbryteAnnoteringDersomUnderlagsperiodeErTilkoblaMeirEnnEiArbeidsgiverperiodeSomTilhoeyrerSammeAvtaleperiode() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Klarer ikkje å entydig avgjere kva som er gjeldande Arbeidsgiverperiode");
        AvtaleId avtale = new AvtaleId(12345L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(2L);
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        of(dato("2011.06.30")),
                                        new StillingsforholdId(999999L),
                                        avtale,
                                        SPK
                                ))
                        .medKoblingar(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtale, arbeidsgiverId)
                        )
                        .medKoblingar(
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId),
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId)
                        )
        );

        assertAnnotasjon(underlag.toList().get(0), AvtaleId.class).isEqualTo(of(avtale));
    }


    /**
     * Verifiserer at annotering av {@link Orgnummer} feilar dersom underlagsperioda har meir enn ei
     * overlappande arbeidsgiverdataperiode koblet til avtaleperiode. ArbeidsgiverId skal fremdeles bli annotert.
     */
    @Test
    public void skalAvbryteAnnoteringDersomUnderlagsperiodeErTilkoblaMeirEnnEiArbeidsgiverdataperiodeSomTilhoeyrerSammeAvtaleperiode() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Klarer ikkje å entydig avgjere kva som er gjeldande Arbeidsgiverdataperiode");
        AvtaleId avtale = new AvtaleId(12345L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(2L);
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Avtalekoblingsperiode(
                                        dato("2011.01.01"),
                                        of(dato("2011.06.30")),
                                        new StillingsforholdId(999999L),
                                        avtale,
                                        SPK
                                ))
                        .medKoblingar(
                                new Avtaleperiode(dato("1990.01.01"), empty(), avtale, arbeidsgiverId)
                        )
                        .medKoblingar(
                                new Arbeidsgiverperiode(dato("1990.01.01"), empty(), arbeidsgiverId)
                        )
                        .medKoblingar(
                                new Arbeidsgiverdataperiode(dato("1990.01.01"), empty(), new Orgnummer(123L), arbeidsgiverId),
                                new Arbeidsgiverdataperiode(dato("1990.01.01"), empty(), new Orgnummer(234L), arbeidsgiverId)
                        )
        );

        assertAnnotasjon(underlag.toList().get(0), AvtaleId.class).isEqualTo(of(avtale));
        assertAnnotasjon(underlag.toList().get(0), ArbeidsgiverId.class).isEqualTo(of(avtale));
    }

    /**
     * Verifiserer at annoteringa slår opp alle regelperiodene som overlappar underlagsperioda og brukar desse for å
     * annotere kvar underlagsperiode med alle gjeldande beregingsreglar.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedAlleBeregningsreglarSomOverlapparPerioda() {
        final MaskineltGrunnlagRegel maskineltGrunnlag = new MaskineltGrunnlagRegel();
        final AarsfaktorRegel aarsfaktor = new AarsfaktorRegel();

        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2011.05.01"))
                        .tilOgMed(dato("2011.05.31"))
                        .medKobling(
                                new Regelperiode<>(dato("1917.01.01"), empty(), maskineltGrunnlag)
                        )
                        .medKobling(
                                new Regelperiode<>(dato("1917.01.01"), empty(), aarsfaktor)
                        )
        );

        assertAnnotasjon(underlag.toList().get(0), MaskineltGrunnlagRegel.class)
                .isEqualTo(of(maskineltGrunnlag));
        assertAnnotasjon(underlag.toList().get(0), AarsfaktorRegel.class)
                .isEqualTo(of(aarsfaktor));
    }

    /**
     * Verifiserer at annoteringa feila viss lønnstrinnperiodene er inkonsitente og det eksisterer meir
     * enn ei lønnstrinnperiode som er gjeldande for eit og samme lønnstrinn innanfor samme tidsrom.
     */
    @Test
    public void skalFeileDersomMeirEnnEiLoennstrinnperiodeErGjeldandeInnanforSammeTidsperiode() {
        final Loennstrinn loennstrinn = new Loennstrinn(53);

        e.expect(IllegalStateException.class);
        e.expectMessage("Det er oppdaga fleire lønnstrinnperioder for");
        e.expectMessage(loennstrinn.toString());

        final StillingsforholdPeriode stilling = new StillingsforholdPeriode(dato("2007.01.23"), empty())
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .stillingsprosent(fulltid())
                                .aksjonsdato(dato("2007.01.23"))
                                .aksjonskode(Aksjonskode.NYTILGANG)
                                .loennstrinn(of(loennstrinn))
                );
        annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2013.02.25"))
                        .tilOgMed(dato("2013.02.28"))
                        .med(SPK)
                        .medKobling(
                                stilling
                        )
                        .medKoblingar(
                                grupper(
                                        SPK, Stream.of(
                                                new StatligLoennstrinnperiode(dato("2009.05.01"), empty(),
                                                        loennstrinn, new Kroner(150_000)
                                                ),
                                                new StatligLoennstrinnperiode(dato("2010.05.01"), empty(),
                                                        loennstrinn, new Kroner(300_000)
                                                )
                                        )
                                )
                        )
        );
    }

    /**
     * Verifiserer at underlagsperioder der stillinga er innrapportert med lønnstrinn og der vi har ei kobling til
     * lønnstrinnperioder, blir annotert med gjeldande lønnstrinnbeløp.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedLoennstrinnBeloepForLoennstrinnetDersomStillingaBrukarLoennstrinnOgPeriodaErKoblaTilLoennstrinnPerioder() {
        final StillingsforholdPeriode stilling = new StillingsforholdPeriode(dato("2007.01.23"), empty())
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .stillingsprosent(fulltid())
                                .aksjonsdato(dato("2007.01.23"))
                                .aksjonskode(Aksjonskode.NYTILGANG)
                                .loennstrinn(of(loennstrinn(42)))
                );
        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2012.01.01"))
                        .tilOgMed(dato("2012.06.30"))
                        .med(SPK)
                        .medKobling(
                                stilling
                        )
                        .medKoblingar(
                                grupper(
                                        SPK, Stream.of(
                                                new StatligLoennstrinnperiode(dato("2009.05.01"), empty(),
                                                        loennstrinn(41), kroner(150_000)
                                                ),
                                                new StatligLoennstrinnperiode(dato("2010.05.01"), empty(),
                                                        loennstrinn(42), kroner(300_000)
                                                ),
                                                new StatligLoennstrinnperiode(dato("2011.05.01"), empty(),
                                                        loennstrinn(43), kroner(450_000)
                                                )
                                        )
                                )
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), LoennstrinnBeloep.class)
                .isEqualTo(of(new LoennstrinnBeloep(kroner(300_000))));
    }

    /**
     * Verifiserer at annoteringa ikkje feilar dersom den ikkje klarer å finne gjeldande lønnstrinnbeløp for
     * stillingas lønnstrinn.
     */
    @Test
    public void skalIkkjeFeileDersomAnnoteringaIkkjeFinnEitLoennstrinnBeloepForPeriodasLoennstrinn() {
        final StillingsforholdPeriode stilling = new StillingsforholdPeriode(dato("2007.01.23"), empty())
                .leggTilOverlappendeStillingsendringer(
                        eiStillingsendring()
                                .stillingsprosent(fulltid())
                                .aksjonsdato(dato("2007.01.23"))
                                .aksjonskode(Aksjonskode.NYTILGANG)
                                .loennstrinn(of(new Loennstrinn(42)))
                );
        final Underlag underlag = annoterAllePerioder(
                eiTomPeriode()
                        .fraOgMed(dato("2012.01.01"))
                        .tilOgMed(dato("2012.06.30"))
                        .med(SPK)
                        .medKobling(
                                stilling
                        )
        );
        assertAnnotasjon(underlag.toList().get(0), LoennstrinnBeloep.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med stillingsprosent dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedStillingsprosentDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Stillingsprosent.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med deltidsjustert lønn dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedDeltidsjustertLoennDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), DeltidsjustertLoenn.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med lønnstrinn dersom
     * den ikkje har ei kobling til ei stillingsforholdperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedLoennstinnDersomStillingsforholdperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Loennstrinn.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperiode ikkje blir annotert med årstall dersom
     * den ikkje har ei kobling til månedsperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedMaanedIAarDersomMaanedsperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Month.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda ikkje blir annotert med årstall dersom
     * den ikkje har ei kobling til årsperiode.
     */
    @Test
    public void skalIkkjeAnnotereUnderlagsperiodeMedAarstallDersomAarsperiodeManglar() {
        assertAnnotasjon(eiAnnotertPeriodeUtanKoblingar(), Aarstall.class).isEqualTo(empty());
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med lønnstrinn dersom den er tilknytta
     * ei stillingsforholdperiode der gjeldande stillingsendring er innrapportert med lønnstrinn.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedLoennstrinnFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode()
                .med(SPK)
                .bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1985.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1985.01.01"))
                        .loennstrinn(of(new Loennstrinn(32)))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), Loennstrinn.class)
                .isEqualTo(of(new Loennstrinn(32)));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med deltidsjustert lønn dersom den er tilknytta
     * ei stillingsforholdperiode der gjeldande stillingsendring er innrapportert med lønn.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedDeltidsjustertLoennFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode().bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1995.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1995.01.01"))
                        .loenn(of(new DeltidsjustertLoenn(new Kroner(600_000))))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), DeltidsjustertLoenn.class)
                .isEqualTo(of(new DeltidsjustertLoenn(new Kroner(600_000))));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med stillingsprosent dersom den er tilknytta
     * ei stillingsforholdperiode.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedStillingsprosentFraTilkoblaStillingsforholdperiode() {
        final Underlagsperiode periode = eiPeriode().bygg();

        final StillingsforholdPeriode stillingsforhold = new StillingsforholdPeriode(dato("1985.01.01"), empty());
        stillingsforhold.leggTilOverlappendeStillingsendringer(
                eiStillingsendring()
                        .aksjonsdato(dato("1985.01.01"))
                        .stillingsprosent(new Stillingsprosent(new Prosent("14%")))
        );
        periode.kobleTil(stillingsforhold);

        assertAnnotasjon(annoter(periode), Stillingsprosent.class)
                .isEqualTo(of(new Stillingsprosent(new Prosent("14%"))));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med samme årstall som årsperioda den er tilknytta.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedMaanedIAarFraTilkoblaMaanedsperiode() {
        final Underlagsperiode periode = eiTomPeriode()
                .fraOgMed(dato("1990.04.01"))
                .tilOgMed(dato("1990.04.30"))
                .bygg();
        periode.kobleTil(new Maaned(new Aarstall(1990), Month.APRIL));

        assertAnnotasjon(annoter(periode), Month.class).isEqualTo(of(Month.APRIL));
    }

    /**
     * Verifiserer at underlagsperioda blir annotert med samme årstall som årsperioda den er tilknytta.
     */
    @Test
    public void skalAnnotereUnderlagsperiodeMedAarstallFraTilkoblaAarsperiode() {
        final Underlagsperiode periode = eiTomPeriode()
                .fraOgMed(dato("1990.01.01"))
                .tilOgMed(dato("1990.12.31")).bygg();
        periode.kobleTil(new Aar(new Aarstall(1990)));

        assertAnnotasjon(annoter(periode), Aarstall.class).isEqualTo(of(new Aarstall(1990)));
    }

    private Underlagsperiode annoter(final UnderlagsperiodeBuilder builder) {
        return annoter(builder.bygg());
    }

    private Underlagsperiode annoter(final Underlagsperiode periode) {
        annotator.annoter(underlag, periode);
        return periode;
    }

    private Underlag annoterAllePerioder(final UnderlagsperiodeBuilder... perioder) {
        final Underlag nyttUnderlag = new Underlag(
                asList(perioder)
                        .stream()
                        .map(UnderlagsperiodeBuilder::bygg)
        );
        annotator.annoter(nyttUnderlag);
        return nyttUnderlag;
    }

    private Underlagsperiode eiAnnotertPeriodeUtanKoblingar() {
        return annoter(eiPeriode().bygg());
    }

    private static Stillingsendring eiStillingsendring() {
        return new Stillingsendring()
                .stillingsprosent(fulltid())
                .foedselsdato(foedselsdato(dato("1917.01.01")))
                .personnummer(personnummer(12345))
                .registreringsdato(dato("2099.01.01"));
    }

    private static UnderlagsperiodeBuilder eiPeriode() {
        return eiTomPeriode()
                .fraOgMed(dato("1990.01.01"))
                .tilOgMed(dato("1990.12.31"));
    }

    private static UnderlagsperiodeBuilder eiTomPeriode() {
        return new UnderlagsperiodeBuilder();
    }
}