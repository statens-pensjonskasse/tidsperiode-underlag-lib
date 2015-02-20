package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.GenerellTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * {@link Loennstrinnperioder} representerer ei samling l�nnstrinnperioder med
 * samme fr� og med- og til og med-datoar.
 * <p>
 * Hovedhensikta med klassa er ytelsesoptimalisering med bakgrunn i at det er 80-100 l�nnstrinnperioder pr
 * �r. For {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} med finkorna oppdeling av
 * underlagsperiodene kostar det etterkvart relativt sett mykje � sjekke om 80-100 l�nnstrinnperioder
 * overlappar underlagsperiodene i underlaget. Sidan dei aller fleste l�nnstrinnperioder har samme
 * gyldigheitsperiode er det derfor mykje � hente p� � kun sjekke overlapp ein gang pr samling med
 * l�nnstrinnperioder.
 *
 * @author Tarjei Skorgenes
 */
public class Loennstrinnperioder extends AbstractTidsperiode<Loennstrinnperioder> {
    private final List<Loennstrinnperiode<?>> perioder;
    private Ordning ordning;

    private Loennstrinnperioder(final Tidsperiode<?> periode, final List<Loennstrinnperiode<?>> perioder,
                                final Ordning ordning) {
        super(periode.fraOgMed(), periode.tilOgMed());
        this.perioder = perioder;
        this.ordning = ordning;
    }

    /**
     * Feilmelding for tidsperioder som blir overlappa av meir enn 1
     * l�nnstrinnperiode tilknytta samme l�nnstrinn.
     * <p>
     * Denne situasjonen indikerer at l�nntrinna systemet opererer med er
     * inkonsistente ettersom det aldri kan vere to aktive l�nnstrinnperioder
     * tilknytta samme l�nnstrinn innanfor ei tidsperiode.
     * <p>
     * For apotekordninga er det ein heilt normal situasjon � finne fleire gjeldande l�nnstrinnperioder med samme trinn
     * i samme periode, men der l�nnstrinna er tilknytta forskjellige stillingskoder. Stillingskode blir derfor tatt
     * hensyn til kun for POA og b�r kun sendast inn til feilmeldinga for oppslag som feilar for POA.
     *
     * @param periode            tidsperioda som l�nnstrinnperiodene overlappar
     * @param ordning            pensjonsordninga som l�nnstrinnperiodene tilh�yrer
     * @param loennstrinn        l�nnstrinnet som vi har detektert meir enn ei overlappande
     *                           l�nnstrinnperiode for
     * @param stillingskode      den valgfrie stillingskoda som kan ha p�virka kva l�nnstrinntabell ein har sl�tt opp l�nnstrinnperiodene fr� for apotekordninga
     * @param loennstrinnperiode l�nnstrinnperiodene som alle er tilknytta l�nnstrinnet og som
     *                           alle er gyldige innanfor den aktuelle tidsperioda  @return ei feilmelding med ein beskrivelse av kva som har feila og kva perioder som f�rte til feilen
     */
    public static String meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
            final Tidsperiode<?> periode,
            final Ordning ordning, final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode,
            final Tidsperiode<?>... loennstrinnperiode) {
        return "Det er oppdaga fleire l�nnstrinnperioder for "
                + loennstrinn
                + stillingskode.map(k -> " for " + k).orElse("")
                + " tilknytta ordning "
                + ordning
                + " som overlappar perioda "
                + periode
                + ", oppslag av l�nn for l�nnstrinn krever "
                + "at det kun eksisterer 1 overlappande l�nnstrinnperiode pr tidsperiode\n"
                + "Overlappande loennstrinnperiode:\n"
                + asList(loennstrinnperiode)
                .stream()
                .map(p -> "- " + p)
                .collect(joining("\n"));
    }

    /**
     * St�ttar alle l�nnstrinnperiodene (som inng�r i gjeldande gruppering) den angitte ordninga?
     *
     * @param ordning pensjonsordninga som alle dei grupperte l�nnstrinnperiodene skal sjekkast mot
     * @return <code>true</code> dersom alle l�nnstrinnperioder i grupperinga gjeld for den angitte ordninga,
     * <code>false</code> viss l�nnstrinnperiodene gjeld for ei anna ordning enn den som er angitt
     */
    public boolean tilhoeyrer(final Ordning ordning) {
        return ordning.equals(this.ordning);
    }

    /**
     * Sjekkar om grupperinga inneheld minst ei l�nnstrinnperiode som har l�nn for l�nnstrinnet.
     *
     * @param loennstrinn   l�nnstrinnet som det skal sjekkast mot
     * @param stillingskode stillingskoda som l�nnstrinnet skal sjekkast for
     * @return <code>true</code> dersom grupperinga inneheldt minst ei l�nnstrinnperiode tilknyttta
     * l�nnstrinnet for den angitte stillingskoda, <code>false</code> ellers
     */
    public boolean harLoennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
        return loennFor(loennstrinn, stillingskode).isPresent();
    }

    /**
     * Kva er gjeldande l�nn i perioda for ei 100% stilling med det angitte l�nnstrinnet?
     *
     * @param loennstrinn   l�nnstrinnet som det skal bli sl�tt opp l�nn for
     * @param stillingskode stillingskoda som kan regulere kva l�nnstrinntabell l�nna for l�nnstrinnet skal bli sl�tt
     *                      opp fr�
     * @return gjeldande l�nn i 100% stilling for ei stilling med det angitte l�nnstrinnet,
     * {@link java.util.Optional#empty()} dersom det ikkje eksisterer noka l�nnstrinnperiode
     * som tilh�yrer det aktuelle l�nnstrinnet
     * @throws IllegalStateException viss grupperinga inneheld meir enn ei periode som
     *                               definerer gjeldande l�nn for l�nnstrinnet
     */
    public Optional<LoennstrinnBeloep> loennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
        return perioder
                .stream()
                .filter(p -> p.harLoennFor(loennstrinn, stillingskode))
                .reduce((a, b) -> {
                    throw new IllegalStateException(
                            meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
                                    new GenerellTidsperiode(this), ordning, loennstrinn, stillingskode, a, b
                            )
                    );
                })
                .map((Loennstrinnperiode<?> loennstrinnperiode) -> loennstrinnperiode.beloep());
    }

    /**
     * Antall l�nnstrinnperioder som er gjeldande innanfor perioda.
     *
     * @return antall l�nnstrinnperioder
     */
    public int size() {
        return perioder.size();
    }

    @Override
    public String toString() {
        return "L�nnstrinnperioder[" + fraOgMed() + "->"
                + tilOgMed().map(LocalDate::toString).orElse("")
                + "," + perioder.size() + " stk]";
    }

    /**
     * Grupperer alle l�nnstrinnperiodene fr� <code>perioder</code> basert p� fr� og med- og til og med-dato
     * og returnerer ein ny straum som inneheld ein ny {@link Loennstrinnperioder} pr gruppering.
     * <p>
     * Dersom <code>perioder</code> inneheld l�nnstrinnperioder som ikkje tilh�yrer den angitte ordninga, vil desse
     * periodene ikkje bli med i grupperinga, dei blir filtrert vekk og vil ikkje f�re til at metoda feilar.
     *
     * @param ordning  pensjonsordninga som l�nnstrinna er tilknytta
     * @param perioder l�nnstrinnperiodene som skal grupperast etter tidperiode
     * @return ein straum som inneheld l�nntrinnperioder for kvar gruppering
     */
    public static Stream<Loennstrinnperioder> grupper(final Ordning ordning, final Stream<Loennstrinnperiode<?>> perioder) {
        return perioder
                .filter(p -> p.tilhoeyrer(ordning))
                .collect(groupingBy(GenerellTidsperiode::new))
                .entrySet()
                .stream()
                .map(e -> new Loennstrinnperioder(e.getKey(), e.getValue(), ordning))
                .sorted(
                        Comparator
                                .comparing(Loennstrinnperioder::fraOgMed)
                                .thenComparing((Loennstrinnperioder p) -> p.tilOgMed().orElse(LocalDate.MAX))
                );
    }

    /**
     * @see #grupper(Ordning, Stream)
     */
    public static Stream<Loennstrinnperioder> grupper(final Ordning ordning, final Loennstrinnperiode<?>... perioder) {
        return grupper(ordning, asList(perioder).stream());
    }
}
