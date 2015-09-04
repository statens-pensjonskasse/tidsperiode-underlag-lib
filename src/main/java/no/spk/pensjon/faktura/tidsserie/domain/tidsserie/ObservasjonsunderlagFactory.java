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
 * algoritma for � generere eit nytt underlag som kan benyttast for � utf�re ein observasjon for heile �ret basert
 * p� kun dei endringane som har aksjonsdato fram til og med dato ein utf�rer observasjonen.
 * <p>
 * Ettersom observasjonsunderlaget vil kunne endre seg basert p� kva for dato ein �nskjer � utf�re ein observasjon
 * av underlaget p�, blir det generert opp eit observasjonsunderlag pr observasjonsdato ein skal kunne observere. Kvart og eit
 * av desse observasjonsunderlaga baserer seg p� eit �rsunderlag som kun inneheld periode avgrensa til � ligge innanfor
 * eit bestemt �rstall.
 * <p>
 * Den andre forutsetninga for at observasjonsunderlaget skal kunne genererast, er at �rsunderlaget er periodisert og
 * splitta i perioder der b�de fr� og med- og til og med-dato for perioda ligg innanfor samme m�nad.
 * <p>
 * Ei siste sentral forutsetning for at observasjonsunderlaget skal vere korrekt, er at ein ikkje treng ta hensyn til
 * tilbakedaterte endringar. Dette betyr at periodiseringa av stillingsforhold-, �rs- og observasjonsunderlag ikkje
 * tar hensyn til registreringsdato for endringar, kun aksjonsdato (for historikk) og fr� og med- og til og med-datoar
 * for allereie periodiserte input-data (ala medregningar, avtalekoblingar) slik dei ser ut p� datoen grunnlagsdata for
 * dei forskjellige underlagstypene blir henta ut, prosessert og brukt for � bygge opp dei forskjellige underlaga.
 *
 * @author Tarjei Skorgenes
 */
class ObservasjonsunderlagFactory {
    private static final List<Month> SORTED_MONTHS = asList(Month.values())
            .stream()
            .sorted(Month::compareTo)
            .collect(toList());

    /**
     * Genererer eit nytt observasjonsunderlag for kvar unike m�ned i <code>aarsunderlag</code>.
     *
     * @param aarsunderlag �rsunderlaget som observasjonsunderlaget skal genererast for
     * @return ein straum som inneheld eit observasjonsunderlag pr m�ned i <code>aarsunderlag</code>
     * @throws IllegalArgumentException           viss <code>aarsunderlag</code> ikkje er eit gyldig �rsunderlag og
     *                                            inneheld perioder tilknytta forskjellige �rstall
     * @throws PaakrevdAnnotasjonManglarException viss <code>aarsunderlag</code> ikkje er annotert med {@link Aarstall}
     */
    Stream<Underlag> genererUnderlagPrMaaned(final Underlag aarsunderlag)
            throws IllegalArgumentException, PaakrevdAnnotasjonManglarException {
        if (!aarsunderlag.valgfriAnnotasjonFor(Aarstall.class).isPresent()) {
            throw new IllegalStateException(
                    "Generering av observasjonsunderlag er kun st�tta for �rsunderlag, "
                            + aarsunderlag + " er ikkje eit �rsunderlag sidan det ikkje er annotert med �rstall"
            );
        }
        return SORTED_MONTHS.stream()
                .map(m -> Observasjonsdato.forSisteDag(aarsunderlag.annotasjonFor(Aarstall.class), m))
                .map(od -> nyttObservasjonsunderlag(aarsunderlag, od))
                .filter(isEmpty().negate());
    }

    /**
     * Genererer eit nytt observasjonsunderlag som inneheld alle perioder i �rsunderlaget som er synlige pr observasjonsdato.
     * Dersom �rsunderlaget inneheld perioder som ikkje er synlige pr observasjonsdato
     * blir det og generert ei ny, fiktiv periode basert p� siste synlige periodes annotasjonar og koblingar.
     * Den einaste forskjellen mellom den fiktive perioda og siste synlige perioda blir tidsperioda den strekker seg
     * over, den fiktive perioda l�per fr� dagen etter siste synlige periodes til og med-dato, til og med siste dag i
     * �ret.
     *
     * @param aarsunderlag     �rsunderlaget som observasjonsunderlaget hentar synlige og bygger opp fiktive perioder fr�
     * @param observasjonsdato som regulerer kva perioder i �rsunderlaget som er synlige
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
     * Genererer ei ny underlagsperiode som strekker seg fr� dagen etter observasjonsdatoen og ut �ret.
     * <p>
     * Den fiktive perioda f�r med annotasjonane fr� <code>sisteSynlige</code> periode, men ikkje koblingane.
     * <p>
     * Den fiktive perioda blir i tillegg annotert med {@link FiktivPeriode} for � tydelig markere at perioda er fiktiv.
     * <p>
     * <h2>Unntakssituasjonar</h2>
     * <p>
     * Det er to situasjonar der det ikkje vil bli generert ei fiktiv periode:
     * <ul>
     * <li>M�naden er desember.</li>
     * <li>Stillingsforholdet er sluttmeldt f�r observasjonsdato.</li>
     * </ul>
     * <h3>1. Desember m�nad</h3>
     * Det blir ikkje generert ei fiktiv periode viss <code>observasjonsdato</code> er siste dag i �ret ettersom
     * �rsunderlaget og observasjonsunderlaget alltid vil vere like i desember m�ned.
     * <p>
     * <h3>2. Stillingsforholdet blir sluttmeldt f�r observasjonsdato</h3>
     * Dersom <code>sisteSynlige</code> underlagsperiode har til og med-dato <i>f�r</i> <code>observasjonsdato</code>
     * vil det ikkje bli generert ei fiktiv periode. Dette fordi ein d� ser at stillinga er avslutta i fortida og dermed
     * ikkje har noko �nske om � prognostisere den som aktiv ut �ret.
     * <br>
     * Dersom <code>sisteSynlige</code> underlagsperiode har til-dato <i>lik</i> observasjonsdato vil det bli generert
     * ei fiktiv periode. Dette gj�res for � unng� en nedgang i observert maskinellt grunnlag for stillinger som
     * sluttmeldes p� observasjonsdato, da det er vanlig at at medlemmer tiltrer i ny stilling dagen etter.
     *
     * @param observasjonsdato som avgrensar kva perioder som er synlige
     * @param sisteSynlige     siste synlige underlagsperiode i �rsunderlaget for <code>observasjonsdato</code>en
     * @return ein straum som inneheld ei fiktiv periode som strekker seg fr� dagen etter observasjonsdato
     * til siste dag i �ret, eller ein {@link java.util.stream.Stream#empty() tom} straum dersom observasjonsdato er
     * siste dag i �ret.
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
     * Returnerer alle underlagsperioder som er startar og sluttar f�r eller p� observasjonsdato
     * og som dermed skal inkluderast i observasjonsunderlaget.
     *
     * @param aarsunderlag     �rsunderlaget som underlagsperiodene skal hentast fr�
     * @param observasjonsdato der ein skal observere maskinelt grunnlag for heile �ret
     * @return ein straum med alle underlagsperioder synlige p� observasjonsdatoen
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
     * Den einaste forskjellen mellom <code>periode</code> og den nye fiktive perioda er fr� og med- og til og med-dato.
     * Den fiktive perioda startar dagen etter <code>periode</code> sin til og med-dato og l�per fram til siste dag
     * i �ret.
     * <p>
     * I tillegg f�r den fiktive perioda ein ekstra annotasjon i forhold til <code>periode</code> sine annotasjonar,
     * {@link FiktivPeriode}.
     *
     * @param periode underlagsperioda som den fiktive perioda skal kopiere annotasjonar og koblingar fr�
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
