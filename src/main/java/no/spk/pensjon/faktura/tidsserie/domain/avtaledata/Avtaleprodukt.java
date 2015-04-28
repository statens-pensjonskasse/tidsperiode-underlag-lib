package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleprodukt extends AbstractTidsperiode<Avtaleversjon> {

    final AvtaleId avtaleId;
    final Produkt produkt;
    final Prosent arbeidsgiverpremieProsent;
    final Prosent medlemspremieProsent;
    final Prosent administrasjonsgebyrProsent;

    public Avtaleprodukt(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, AvtaleId avtaleId, Produkt produkt, Prosent arbeidsgiverpremieProsent,
            Prosent medlemspremieProsent, Prosent administrasjonsgebyrProsent) {
        super(fraOgMed, tilOgMed);
        this.avtaleId = avtaleId;
        this.produkt = produkt;
        this.arbeidsgiverpremieProsent = arbeidsgiverpremieProsent;
        this.medlemspremieProsent = medlemspremieProsent;
        this.administrasjonsgebyrProsent = administrasjonsgebyrProsent;
    }

    /**
     * Er avtaleproduktet tillknytta den angitte avtalen?
     *
     * @param avtale avtalenummeret for avtalen vi skal sjekke opp mot
     * @return <code>true</code> dersom avtaleproduktet er tilknytta den angitte avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final AvtaleId avtale) {
        return this.avtaleId.equals(avtale);
    }

    /**
     * Oppdaterer avtalebyggarens tilstand til å reflektere informasjon om at avtalen betalar premie for
     * avtaleproduktets produkt.
     * <p>
     * TODO: Korleis tolke/handtere avtaleprodukt med produktinfo 19, 29, 39 osv (dei betyr typisk "Har ikkje produktet")
     * <p>
     * TODO: Korleis tolke/handtere avtaleprodukt med tomme satsar, skal vi behandle dei som om avtalen betalar premie or not?
     * <p>
     * TODO: Viss vi filtrerer vekk tomme satsar, korleis handtere AFP-produktet for apotekordninga?
     *
     * @param avtale avtalebyggaren som inneheld avtaletilstanda som skal oppdaterast
     */
    public void populer(final AvtaleBuilder avtale) {
        avtale.addProdukt(produkt);
    }

    public AvtaleId avtale() {
        return avtaleId;
    }

    public Produkt produkt() {
        return produkt;
    }

    public Prosent arbeidsgiverpremieProsent() {
        return arbeidsgiverpremieProsent;
    }

    public Prosent medlemspremieProsent() {
        return medlemspremieProsent;
    }

    public Prosent administrasjonsgebyrProsent() {
        return administrasjonsgebyrProsent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Avtaleprodukt)) return false;

        Avtaleprodukt that = (Avtaleprodukt) o;

        if (avtaleId != null ? !avtaleId.equals(that.avtaleId) : that.avtaleId != null) return false;
        if (produkt != that.produkt) return false;
        if (!Prosent.equals(arbeidsgiverpremieProsent, that.arbeidsgiverpremieProsent))
            return false;
        if (!Prosent.equals(medlemspremieProsent, that.medlemspremieProsent)) return false;
        if (!Prosent.equals(administrasjonsgebyrProsent, that.administrasjonsgebyrProsent))
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = avtaleId != null ? avtaleId.hashCode() : 0;
        result = 31 * result + (produkt != null ? produkt.hashCode() : 0);
        result = 31 * result + (arbeidsgiverpremieProsent != null ? arbeidsgiverpremieProsent.hashCode() : 0);
        result = 31 * result + (medlemspremieProsent != null ? medlemspremieProsent.hashCode() : 0);
        result = 31 * result + (administrasjonsgebyrProsent != null ? administrasjonsgebyrProsent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Avtaleprodukt{" +
                "avtaleId=" + avtaleId +
                ", produkt=" + produkt +
                ", arbeidsgiverpremieProsent=" + arbeidsgiverpremieProsent +
                ", medlemspremieProsent=" + medlemspremieProsent +
                ", administrasjonsgebyrProsent=" + administrasjonsgebyrProsent +
                '}';
    }
}