package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.time.LocalDate.now;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.*;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode.TILLEGG_ANNEN_ARBGIV;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer.personnummer;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;

/**
 * Gjennbrukbare pre-oppsatte datatilstander som ein kan gjennbruke as-is eller modifisere ved behov.
 * <br>
 * Ref. http://martinfowler.com/bliki/ObjectMother.html.
 */
public class ObjectMother {
    private static final Medregningsperiode.Builder medregning = Medregningsperiode.medregning()
            .fraOgMed(now().minusYears(3))
            .loepende()
            .beloep(kroner(1))
            .kode(TILLEGG_ANNEN_ARBGIV)
            .foedselsdato(foedselsdato(now().minusYears(30)))
            .personnummer(personnummer(12345))
            .stillingsforhold(stillingsforhold(1L));

    /**
     * Opprettar ein kopi av ein pre-populert builder for medregningsperioder.
     * <br>
     * Alle felt i builderen blir pre-populert slik at klienten kun skal trenge å overstyre/endre dei felta ein har spesifikke krav til verdien på.
     *
     * @return ein ny medregningsperioderbuilder
     */
    public static Medregningsperiode.Builder eiMedregning() {
        return medregning.kopi();
    }
}
