package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Termintype;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Test;

public class TermintypeRegelTest {

    @Test
    public void skalSetteTermintypeLikSpkNaarIkkeIpbOgIkkeHendelsesbasert() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.SPK)
                        .med(Premiestatus.class, Premiestatus.AAO_02)
                        .med(Premiekategori.class, Premiekategori.FASTSATS_AARLIG_OPPFOELGING)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.SPK);
    }

    @Test
    public void skalSetteTermintypeLikAndreNaarLop() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.SPK)
                        .med(Premiestatus.class, Premiestatus.AAO_01)
                        .med(Premiekategori.class, Premiekategori.HENDELSESBASERT)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.ANDRE);
    }

    @Test
    public void skalSetteTermintypeLikAndreNaarFasIpb() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.SPK)
                        .med(Premiestatus.class, Premiestatus.IPB)
                        .med(Premiekategori.class, Premiekategori.FASTSATS)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.ANDRE);
    }

    @Test
    public void skalSetteTermintypeLikAndreNaarFsaOgIkkeIpb() {
            assertThat(periode()
                            .med(Ordning.class, Ordning.SPK)
                            .med(Premiestatus.class, Premiestatus.IPB)
                            .med(Premiekategori.class, Premiekategori.FASTSATS_AARLIG_OPPFOELGING)
                            .bygg()
                            .beregn(TermintypeRegel.class)
            ).isEqualTo(Termintype.ANDRE);
    }

    @Test
    public void skalSetteTermintypeLikPoaNaarPoagIkkeIpb() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.POA)
                        .med(Premiestatus.class, Premiestatus.AAO_12)
                        .med(Premiekategori.class, Premiekategori.FASTSATS)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.POA);
    }

    @Test
    public void skalSetteTermintypeLikAndreNaarPoaOgLop() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.POA)
                        .med(Premiestatus.class, Premiestatus.AAO_12)
                        .med(Premiekategori.class, Premiekategori.HENDELSESBASERT)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.ANDRE);
    }

    @Test
    public void skalSetteTermintypeLikAndreNaarPoaOgIpb() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.POA)
                        .med(Premiestatus.class, Premiestatus.IPB)
                        .med(Premiekategori.class, Premiekategori.FASTSATS)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.ANDRE);
    }

    @Test
    public void skalSetteTermintypeLikUkjentNaarPremiekategoriMangler() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.POA)
                        .med(Premiestatus.class, Premiestatus.AAO_12)
                        .med(Premiekategori.class, null)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.UKJENT);
    }

    @Test
    public void skalSetteTermintypeLikUkjentNaarPremiestatusMangler() {
        assertThat(periode()
                        .med(Ordning.class, Ordning.POA)
                        .med(Premiestatus.class, null)
                        .med(Premiekategori.class, Premiekategori.FASTSATS_AARLIG_OPPFOELGING)
                        .bygg()
                        .beregn(TermintypeRegel.class)
        ).isEqualTo(Termintype.UKJENT);
    }

    private static UnderlagsperiodeBuilder periode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2015.01.01"))
                .tilOgMed(dato("2015.12.31"))
                .med(new TermintypeRegel());
    }

}