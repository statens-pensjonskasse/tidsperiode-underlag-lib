package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer.personnummer;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.eiMedregning;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer;
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
                .foedselsdato(foedselsdato(dato("1978.01.05")))
                .personnummer(personnummer(78742))
                .bygg()
                .annoter(periode);

        assertAnnotasjon(periode, Foedselsnummer.class)
                .isEqualTo(
                        of(
                                new Foedselsnummer(
                                        foedselsdato(dato("1978.01.05")),
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