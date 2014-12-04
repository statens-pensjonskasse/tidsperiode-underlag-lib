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
 * {@link Loennstrinnperioder} representerer ei samling lønnstrinnperioder med
 * samme frå og med- og til og med-datoar.
 * <p>
 * Hovedhensikta med klassa er ytelsesoptimalisering med bakgrunn i at det er 80-100 lønnstrinnperioder pr
 * år. For {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} med finkorna oppdeling av
 * underlagsperiodene kostar det etterkvart relativt sett mykje å sjekke om 80-100 lønnstrinnperioder
 * overlappar underlagsperiodene i underlaget. Sidan dei aller fleste lønnstrinnperioder har samme
 * gyldigheitsperiode er det derfor mykje å hente på å kun sjekke overlapp ein gang pr samling med
 * lønnstrinnperioder.
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
     * Støttar alle lønnstrinnperiodene (som inngår i gjeldande gruppering) den angitte ordninga?
     *
     * @param ordning pensjonsordninga som alle dei grupperte lønnstrinnperiodene skal sjekkast mot
     * @return <code>true</code> dersom alle lønnstrinnperioder i grupperinga gjeld for den angitte ordninga,
     * <code>false</code> viss lønnstrinnperiodene gjeld for ei anna ordning enn den som er angitt
     */
    public boolean tilhoeyrer(final Ordning ordning) {
        return ordning.equals(this.ordning);
    }

    /**
     * Sjekkar om grupperinga inneheld minst ei lønnstrinnperiode som har lønn for lønnstrinnet.
     *
     * @param loennstrinn   lønnstrinnet som det skal sjekkast mot
     * @param stillingskode stillingskoda som lønnstrinnet skal sjekkast for
     * @return <code>true</code> dersom grupperinga inneheldt minst ei lønnstrinnperiode tilknyttta
     * lønnstrinnet for den angitte stillingskoda, <code>false</code> ellers
     */
    public boolean harLoennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
        return loennFor(loennstrinn, stillingskode).isPresent();
    }

    /**
     * Kva er gjeldande lønn i perioda for ei 100% stilling med det angitte lønnstrinnet?
     *
     * @param loennstrinn   lønnstrinnet som det skal bli slått opp lønn for
     * @param stillingskode stillingskoda som kan regulere kva lønnstrinntabell lønna for lønnstrinnet skal bli slått
     *                      opp frå
     * @return gjeldande lønn i 100% stilling for ei stilling med det angitte lønnstrinnet,
     * {@link java.util.Optional#empty()} dersom det ikkje eksisterer noka lønnstrinnperiode
     * som tilhøyrer det aktuelle lønnstrinnet
     * @throws IllegalStateException viss grupperinga inneheld meir enn ei periode som
     *                               definerer gjeldande lønn for lønnstrinnet
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
     * Antall lønnstrinnperioder som er gjeldande innanfor perioda.
     *
     * @return antall lønnstrinnperioder
     */
    public int size() {
        return perioder.size();
    }

    @Override
    public String toString() {
        return "Lønnstrinnperioder[" + fraOgMed() + "->"
                + tilOgMed().map(LocalDate::toString).orElse("")
                + "," + perioder.size() + " stk]";
    }

    /**
     * Grupperer alle lønnstrinnperiodene frå <code>perioder</code> basert på frå og med- og til og med-dato
     * og returnerer ein ny straum som inneheld ein ny {@link Loennstrinnperioder} pr gruppering.
     * <p>
     * Dersom <code>perioder</code> inneheld lønnstrinnperioder som ikkje tilhøyrer den angitte ordninga, vil desse
     * periodene ikkje bli med i grupperinga, dei blir filtrert vekk og vil ikkje føre til at metoda feilar.
     *
     * @param ordning  pensjonsordninga som lønnstrinna er tilknytta
     * @param perioder lønnstrinnperiodene som skal grupperast etter tidperiode
     * @return ein straum som inneheld lønntrinnperioder for kvar gruppering
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
