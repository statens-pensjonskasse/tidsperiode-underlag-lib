package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats.premiesats;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Risikoklasse;
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
 * <p>Avtaleprodukt er oppført med premiesatser. Disse er oppgitt enten i prosent eller i kronebeløp.</p>
 *
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleprodukt extends AbstractTidsperiode<Avtaleprodukt> implements Avtalerelatertperiode<Avtaleprodukt> {
    private final AvtaleId avtaleId;

    private final Premiesats premiesats;

    private Optional<Risikoklasse> risikoklasse = empty();

    /**
     * Konstruktør for å opprette et avtaleprodukt koblet til en avtale som for en bestemt tidsperiode.
     *
     * @param fraOgMed    fra og med-dato for perioden
     * @param tilOgMed    til og med-datp for perioden
     * @param avtaleId    avtalid for avtalen avtaleproduktet er koblet til
     * @param produkt     produkt-typen til avtaleproduktet
     * @param produktinfo produktinfo for avtaleproduktet. Betydningen av koden er avhengig av produkt.
     * @param satser      Satser som gjelder for avtaleproduktet.
     * @throws NullPointerException viss nokon av argumenta er <code>null</code>
     */
    public Avtaleprodukt(LocalDate fraOgMed, Optional<LocalDate> tilOgMed,
                         AvtaleId avtaleId, Produkt produkt, Produktinfo produktinfo, Satser<?> satser) {
        super(fraOgMed, tilOgMed);
        this.avtaleId = requireNonNull(avtaleId, "avtaleId er påkrevd, men var null");
        this.premiesats = premiesats(produkt).produktinfo(produktinfo).satser(satser).bygg();
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
     * Legger til avtaleproduktet sin premiesats på builderen.
     * <br>
     * Merk at premiesatsen blir lagt til uavhengig av om produktet er fakturerbart eller ikkje. For å avgjere
     * om ein faktisk skal fakturere for produktet, sjå {@link Premiesats#erFakturerbar()}.
     *
     * @param avtale builderen som inneheld avtaletilstanda som skal oppdaterast
     * @return <code>avtale</code>
     * @see AvtaleBuilder#addPremiesats(Premiesats)
     */
    public AvtaleBuilder populer(final AvtaleBuilder avtale) {
        if (produkt() == Produkt.YSK) {
            avtale.risikoklasse(risikoklasse);
        }
        return avtale.addPremiesats(premiesats);
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
        return premiesats.produkt;
    }

    /**
     * Overstyrer avtaleproduktets risikoklasse.
     *
     * @param risikoklasse risikoklassa avtalen tilhøyrer, eller {@link Optional#empty()} viss avtalen
     *                     ikkje har ei risikoklasse
     * @return <code>this</code>
     * @throws IllegalArgumentException viss avtaleproduktet ikkje er {@link Produkt#YSK} og risikoklassa ikkje er tom
     * @since 1.1.1
     */
    public Avtaleprodukt risikoklasse(final Optional<Risikoklasse> risikoklasse) {
        requireNonNull(risikoklasse, "risikoklasse er påkrevd, men var null");
        if (produkt() != Produkt.YSK && risikoklasse.isPresent()) {
            throw new IllegalArgumentException(
                    "risikoklasse er ikkje støtta for "
                            + produkt()
                            + ", risikoklasse er kun støtta for avtaleprodukt tilknytta "
                            + Produkt.YSK
            );
        }
        this.risikoklasse = risikoklasse;
        return this;
    }

    @Override
    public String toString() {
        return "Avtaleprodukt for avtaleid=" + avtaleId +
                ", produkt=" + premiesats.produkt + ", premiesats=" + premiesats
                + ", risikoklasse=" + risikoklasse;
    }
}