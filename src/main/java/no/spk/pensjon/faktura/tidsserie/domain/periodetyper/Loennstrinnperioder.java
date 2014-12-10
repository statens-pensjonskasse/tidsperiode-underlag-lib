package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Feilmeldingar.meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid;

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
