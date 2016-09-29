package no.spk.pensjon.faktura.tidsserie.domain.testdata;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon.avtaleversjon;

import java.time.LocalDate;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;

/**
 * Ei samling gjennbrukbare builder-instansar som kan brukast for å danne gyldige grunnlagsdata og
 * tidsperioder av forskjellige typer.
 * <br>
 * Intensjonen her er at ting vi ser vi ofte repetererer all over the place kun fordi vi må
 * ha verdien eller når vi kun treng å overstyre / bry oss om ein liten egenskap, legger vi her
 * så vi kan redusere dupliseringa i testoppsett. Kvar test blir då kun ansvarlig for sine spesielle
 * overstyringsbehov.
 *
 * @author Tarjei Skorgenes
 */
public class ObjectMother {
    /**
     * Opprettar ein builder for den angitte avtalen som er satt opp til å konstruere avtaleversjonar
     * med alle påkrevde felt populert.
     * <br>
     * Avtaleversjonane er pre-oppsatt til å starte ved tidenes morgen og vere løpande.
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
     * Vi definerer 1. januar 1917 som tidenes morgen sidan SPK oppstod då.
     *
     * @return 1. januar 1917
     */
    public static LocalDate tidenesMorgen() {
        return dato("1917.01.01");
    }
}
