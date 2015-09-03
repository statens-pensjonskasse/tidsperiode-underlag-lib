package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link ObservasjonsunderlagFactory} representerer
 * algoritma for å generere eit nytt underlag som kan benyttast for å utføre ein observasjon for heile året basert
 * på kun dei endringane som har aksjonsdato fram til og med dato ein utfører observasjonen.
 * <p>
 * Ettersom observasjonsunderlaget vil kunne endre seg basert på kva for dato ein ønskjer å utføre ein observasjon
 * av underlaget på, blir det generert opp eit observasjonsunderlag pr observasjonsdato ein skal kunne observere. Kvart og eit
 * av desse observasjonsunderlaga baserer seg på eit årsunderlag som kun inneheld periode avgrensa til å ligge innanfor
 * eit bestemt årstall.
 * <p>
 * Den andre forutsetninga for at observasjonsunderlaget skal kunne genererast, er at årsunderlaget er periodisert og
 * splitta i perioder der både frå og med- og til og med-dato for perioda ligg innanfor samme månad.
 * <p>
 * Ei siste sentral forutsetning for at observasjonsunderlaget skal vere korrekt, er at ein ikkje treng ta hensyn til
 * tilbakedaterte endringar. Dette betyr at periodiseringa av stillingsforhold-, års- og observasjonsunderlag ikkje
 * tar hensyn til registreringsdato for endringar, kun aksjonsdato (for historikk) og frå og med- og til og med-datoar
 * for allereie periodiserte input-data (ala medregningar, avtalekoblingar) slik dei ser ut på datoen grunnlagsdata for
 * dei forskjellige underlagstypene blir henta ut, prosessert og brukt for å bygge opp dei forskjellige underlaga.
 *
 * @author Tarjei Skorgenes
 */
class ObservasjonsunderlagFactory {
    private static final List<Month> SORTED_MONTHS = asList(Month.values())
            .stream()
            .sorted(Month::compareTo)
            .collect(toList());

    /**
     * Genererer eit nytt observasjonsunderlag for kvar unike måned i <code>aarsunderlag</code>.
     *
     * @param aarsunderlag årsunderlaget som observasjonsunderlaget skal genererast for
     * @return ein straum som inneheld eit observasjonsunderlag pr måned i <code>aarsunderlag</code>
     * @throws IllegalArgumentException           viss <code>aarsunderlag</code> ikkje er eit gyldig årsunderlag og
     *                                            inneheld perioder tilknytta forskjellige årstall
     * @throws PaakrevdAnnotasjonManglarException viss <code>aarsunderlag</code> ikkje er annotert med {@link Aarstall}
     */
    Stream<Underlag> genererUnderlagPrMaaned(final Underlag aarsunderlag)
            throws IllegalArgumentException, PaakrevdAnnotasjonManglarException {
        if (!aarsunderlag.valgfriAnnotasjonFor(Aarstall.class).isPresent()) {
            throw new IllegalStateException(
                    "Generering av observasjonsunderlag er kun støtta for årsunderlag, "
                            + aarsunderlag + " er ikkje eit årsunderlag sidan det ikkje er annotert med årstall"
            );
        }
        return SORTED_MONTHS.stream()
                .map(m -> Observasjonsdato.forSisteDag(aarsunderlag.annotasjonFor(Aarstall.class), m))
                .map(od -> nyttObservasjonsunderlag(aarsunderlag, od))
                .filter(isEmpty().negate());
    }

    /**
     * Genererer eit nytt observasjonsunderlag som inneheld alle perioder i årsunderlaget som er synlige pr observasjonsdato.
     * Dersom årsunderlaget inneheld perioder som ikkje er synlige pr observasjonsdato
     * blir det og generert ei ny, fiktiv periode basert på siste synlige periodes annotasjonar og koblingar.
     * Den einaste forskjellen mellom den fiktive perioda og siste synlige perioda blir tidsperioda den strekker seg
     * over, den fiktive perioda løper frå dagen etter siste synlige periodes til og med-dato, til og med siste dag i
     * året.
     *
     * @param aarsunderlag     årsunderlaget som observasjonsunderlaget hentar synlige og bygger opp fiktive perioder frå
     * @param observasjonsdato som regulerer kva perioder i årsunderlaget som er synlige
     * @return eit nytt observasjonsunderlag
     */
    private Underlag nyttObservasjonsunderlag(final Underlag aarsunderlag, final Observasjonsdato observasjonsdato) {
        final Underlag synlige = new Underlag(synligePerioderFramTilOgMed(aarsunderlag, observasjonsdato));
        final Optional<Underlagsperiode> siste = synlige.last();
        return new Underlag(
                concat(
                        synlige.stream(),
                        fiktivPeriodeUtAaret(observasjonsdato, siste)
                )
        )
                .annoterFra(aarsunderlag)
                .annoter(Observasjonsdato.class, observasjonsdato);
    }


    /**
     * Genererer ei ny underlagsperiode som strekker seg frå dagen etter observasjonsdatoen og ut året.
     * <p>
     * Den fiktive perioda får med annotasjonane frå <code>sisteSynlige</code> periode, men ikkje koblingane.
     * <p>
     * Den fiktive perioda blir i tillegg annotert med {@link FiktivPeriode} for å tydelig markere at perioda er fiktiv.
     * <p>
     * <h2>Unntakssituasjonar</h2>
     * <p>
     * Det er to situasjonar der det ikkje vil bli generert ei fiktiv periode:
     * <ul>
     * <li>Månaden er desember.</li>
     * <li>Stillingsforholdet er sluttmeldt før observasjonsdato.</li>
     * </ul>
     * <h3>1. Desember månad</h3>
     * Det blir ikkje generert ei fiktiv periode viss <code>observasjonsdato</code> er siste dag i året ettersom
     * årsunderlaget og observasjonsunderlaget alltid vil vere like i desember måned.
     * <p>
     * <h3>2. Stillingsforholdet blir sluttmeldt før observasjonsdato</h3>
     * Dersom <code>sisteSynlige</code> underlagsperiode har til og med-dato <i>før</i> <code>observasjonsdato</code>
     * vil det ikkje bli generert ei fiktiv periode. Dette fordi ein då ser at stillinga er avslutta i fortida og dermed
     * ikkje har noko ønske om å prognostisere den som aktiv ut året.
     * <br>
     * Dersom <code>sisteSynlige</code> underlagsperiode har til-dato <i>lik</i> observasjonsdato vil det bli generert
     * ei fiktiv periode. Dette gjøres for å unngå en nedgang i observert maskinellt grunnlag for stillinger som
     * sluttmeldes på observasjonsdato, da det er vanlig at at medlemmer tiltrer i ny stilling dagen etter.
     *
     * @param observasjonsdato som avgrensar kva perioder som er synlige
     * @param sisteSynlige     siste synlige underlagsperiode i årsunderlaget for <code>observasjonsdato</code>en
     * @return ein straum som inneheld ei fiktiv periode som strekker seg frå dagen etter observasjonsdato
     * til siste dag i året, eller ein {@link java.util.stream.Stream#empty() tom} straum dersom observasjonsdato er
     * siste dag i året.
     */
    private Stream<Underlagsperiode> fiktivPeriodeUtAaret(final Observasjonsdato observasjonsdato,
                                                          final Optional<Underlagsperiode> sisteSynlige) {
        if (observasjonsdato.erAaretsSisteDag()) {
            return Stream.empty();
        }
        return sisteSynlige
                .filter((Underlagsperiode p) -> p.tilOgMed().get().equals(observasjonsdato.dato()))
                .map(this::nyFiktivPeriodeUtAaret)
                .map(Stream::of)
                .orElse(Stream.empty());
    }

    /**
     * Returnerer alle underlagsperioder som er startar og sluttar før eller på observasjonsdato
     * og som dermed skal inkluderast i observasjonsunderlaget.
     *
     * @param aarsunderlag     årsunderlaget som underlagsperiodene skal hentast frå
     * @param observasjonsdato der ein skal observere maskinelt grunnlag for heile året
     * @return ein straum med alle underlagsperioder synlige på observasjonsdatoen
     */
    private Stream<Underlagsperiode> synligePerioderFramTilOgMed(final Underlag aarsunderlag,
                                                                 final Observasjonsdato observasjonsdato) {
        return aarsunderlag
                .stream()
                .filter(observasjonsdato::erPeriodenSynligFra);
    }

    /**
     * Lagar ei ny fiktiv periode som ein kopi av <code>periode</code>.
     * <p>
     * Alle annotasjonar og koblingar blir kopiert over til den fiktive perioda.
     * <p>
     * Den einaste forskjellen mellom <code>periode</code> og den nye fiktive perioda er frå og med- og til og med-dato.
     * Den fiktive perioda startar dagen etter <code>periode</code> sin til og med-dato og løper fram til siste dag
     * i året.
     * <p>
     * I tillegg får den fiktive perioda ein ekstra annotasjon i forhold til <code>periode</code> sine annotasjonar,
     * {@link FiktivPeriode}.
     *
     * @param periode underlagsperioda som den fiktive perioda skal kopiere annotasjonar og koblingar frå
     * @return ei ny fiktiv periode
     */
    private Underlagsperiode nyFiktivPeriodeUtAaret(final Underlagsperiode periode) {
        final LocalDate tilOgMed = periode.tilOgMed().get();
        return periode
                .kopierUtenKoblinger(
                        tilOgMed.plusDays(1),
                        tilOgMed.with(lastDayOfYear())
                )
                .annoter(
                        FiktivPeriode.class,
                        FiktivPeriode.FIKTIV
                );
    }

    private static Predicate<Underlag> isEmpty() {
        return (Underlag u) -> u.stream().count() == 0;
    }
}
