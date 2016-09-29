package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.BISTILLING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer.personnummer;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medregningsperiode.medregning;
import static no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.ObjectMother.eiMedregning;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import org.junit.Test;

public class MedregningsperiodeTest {
    @Test
    public void skalTilhoeyreStillingsforholdMedSammeStillingsforholdNummer() {
        final StillingsforholdId id = StillingsforholdId.stillingsforhold(6189726L);
        final Medregningsperiode periode = eiMedregning()
                .stillingsforhold(id)
                .bygg();
        assertThat(
                periode.tilhoerer(id))
                .as("tilhøyrer medregningsperioda " + id + "?\n" + periode)
                .isTrue();
    }

    @Test
    public void skalAnnotereMedFoedselsnummer() {
        final Underlagsperiode periode = eiPeriode();

        eiMedregning()
                .foedselsdato(foedselsdato(19780105))
                .personnummer(personnummer(78742))
                .bygg()
                .annoter(periode);

        assertAnnotasjon(periode, Foedselsnummer.class)
                .isEqualTo(
                        of(
                                new Foedselsnummer(
                                        foedselsdato(19780105),
                                        personnummer(78742)
                                )
                        )
                );
    }

    private static Underlagsperiode eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2010.01.01"))
                .tilOgMed(dato("2010.01.31"))
                .bygg();
    }
}