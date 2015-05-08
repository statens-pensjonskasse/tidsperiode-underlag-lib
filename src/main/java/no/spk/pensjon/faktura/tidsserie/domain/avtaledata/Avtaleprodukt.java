package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * <p>Avtaleprodukt representrer et pensjonsprodukt med gitt produktinfo knyttet til en avtale.<br>
 * En avtale kan være knyttet til flere forskjellig produkt. <br>
 * Betydningen av produktinfo er avhengig av produktet, og kan påvirke om avtalen faktisk har produktet eller ikke.
 * </p>
 * <p>
 * For eksempel:
 * For produkt 'GRU' betyr produktinfo 35 og 36 at avtalen <i>skal</i> faktureres for 'GRU'.
 * Alle andre produktinfo-verdier betyr at avtalen <i>ikke skal</i> faktureres for produktet 'GRU'.
 * </p>
 * <p>
 * <p>Avtaleprodukt er oppført med premiesatser. Disse er oppgitt enten i prosent eller i kronebeløp, men ikke begge deler. Begge kan være 0.</p>
 *
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleprodukt extends AbstractTidsperiode<Avtaleversjon> {

    private final AvtaleId avtaleId;
    private final Produkt produkt;
    private final int produktinfo;
    private final Optional<Satser<Kroner>> kronesatser;
    private final Optional<Satser<Prosent>> prosentsatser;

    /**
     * Konstruktør for å opprette et avtaleprodukt koblet til en avtale som for en bestemt tidsperiode.
     *
     * @param fraOgMed fra og med-dato for perioden
     * @param tilOgMed til og med-datp for perioden
     * @param avtaleId avtalid for avtalen avtaleproduktet er koblet til
     * @param produkt produkt-typen til avtaleproduktet
     * @param produktinfo produktinfo for avtaleproduktet. Betydningen av koden er avhengig av produkt.
     * @param prosentsatser Valgfri parameter som holder på premiesatser representert i prosent. Dersom denne er i bruk, må kronessatser være
     * Optional.empty()
     * @param kronesatser Valgfri parameter som holder på premiesatser representert i kroner.
     * Dersom denne er i bruk, må kronessatser være Optional.empty()
     * @throws IllegalStateException dersom både prosentsatser og kronesatser 'isPresent'. Kun en av de to, eller ingen er tillatt.
     */
    public Avtaleprodukt(LocalDate fraOgMed, Optional<LocalDate> tilOgMed,
            AvtaleId avtaleId, Produkt produkt, int produktinfo,
            Optional<Satser<Prosent>> prosentsatser, Optional<Satser<Kroner>> kronesatser) {
        super(fraOgMed, tilOgMed);
        assertIkkeProsentsatserOgKronesatserSammen(prosentsatser, kronesatser);
        this.kronesatser = kronesatser;
        this.avtaleId = avtaleId;
        this.produkt = produkt;
        this.produktinfo = produktinfo;
        this.prosentsatser = prosentsatser;
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
     * TODO: Skal satsene brukes her?
     *
     * @param avtale avtalebyggaren som inneheld avtaletilstanda som skal oppdaterast
     */
    public AvtaleBuilder populer(final AvtaleBuilder avtale) {
        if (erFakturerbar()) {
            avtale.addProdukt(produkt);
        }
        return avtale;
    }

    /**
     * @return AvtaleId som avtaleproduktet er knyttet til.
     */
    public AvtaleId avtale() {
        return avtaleId;
    }

    /**
     * @return Produktet som avtaleproduktet representerer.
     */
    public Produkt produkt() {
        return produkt;
    }

    @Override
    public String toString() {
        return "Avtaleprodukt for avtaleid=" + avtaleId +
                ", produkt=" + produkt + ", produktinfo=" + produktinfo;
    }

    private boolean erFakturerbar() {
        if (produkt.equals(Produkt.GRU)) {
            return produktinfo == 35 || produktinfo == 36;
        }
        if (produkt.equals(Produkt.YSK)) {
            return produktinfo != 79;
        }
        return true;
    }

    private void assertIkkeProsentsatserOgKronesatserSammen(Optional<?> prosentsatser, Optional<?> kronesatser) {
        if (prosentsatser.isPresent() && kronesatser.isPresent()) {
            throw new IllegalStateException("Både prosentsatser og kronesatser kan ikke være i bruk for et avtaleprodukt.");
        }
    }
}