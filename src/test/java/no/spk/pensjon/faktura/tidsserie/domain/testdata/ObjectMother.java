package no.spk.pensjon.faktura.tidsserie.domain.testdata;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon.avtaleversjon;

import java.time.LocalDate;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;

/**
 * Ei samling gjennbrukbare builder-instansar som kan brukast for � danne gyldige grunnlagsdata og
 * tidsperioder av forskjellige typer.
 * <br>
 * Intensjonen her er at ting vi ser vi ofte repetererer all over the place kun fordi vi m�
 * ha verdien eller n�r vi kun treng � overstyre / bry oss om ein liten egenskap, legger vi her
 * s� vi kan redusere dupliseringa i testoppsett. Kvar test blir d� kun ansvarlig for sine spesielle
 * overstyringsbehov.
 *
 * @author Tarjei Skorgenes
 */
public class ObjectMother {
    /**
     * Opprettar ein builder for den angitte avtalen som er satt opp til � konstruere avtaleversjonar
     * med alle p�krevde felt populert.
     * <br>
     * Avtaleversjonane er pre-oppsatt til � starte ved tidenes morgen og vere l�pande.
     * <br>
     * Dei benyttar ukjent premiestatus og {@link Premiekategori#FASTSATS}.
     *
     * @param avtaleId avtalen avtaleversjonane skal koblast til
     * @return ein ny builder for avtaleversjonar
     * @see #tidenesMorgen()
     */
    public static Avtaleversjon.AvtaleversjonBuilder enAvtaleversjon(final AvtaleId avtaleId) {
        return avtaleversjon(avtaleId)
                .fraOgMed(tidenesMorgen())
                .premiestatus(Premiestatus.UKJENT)
                .premiekategori(Premiekategori.FASTSATS);
    }

    /**
     * Vi definerer 1. januar 1917 som tidenes morgen sidan SPK oppstod d�.
     *
     * @return 1. januar 1917
     */
    public static LocalDate tidenesMorgen() {
        return dato("1917.01.01");
    }
}
