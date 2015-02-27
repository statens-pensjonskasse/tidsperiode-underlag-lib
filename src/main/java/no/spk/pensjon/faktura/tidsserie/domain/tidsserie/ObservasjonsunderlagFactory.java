package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.LocalDate;
import java.time.Month;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Observasjonsdato.forSisteDag;

/**
 * {@link ObservasjonsunderlagFactory} representerer
 * algoritma for å generere eit nytt underlag som kan benyttast for å utføre ein observasjon for heile året basert
 * på kun dei endringane som har aksjonsdato fram til og med siste dag i måneden ein utfører observasjonen.
 * <p>
 * Ettersom observasjonsunderlaget vil kunne endre seg basert på kva for ein månad ein ønskjer å utføre ein observasjon
 * av underlaget på, blir det generert opp eit observasjonsunderlag pr måned ein skal kunne observere. Kvart og eit
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
        return asList(Month.values())
                .stream()
                .sorted(Month::compareTo)
                .map((Month m) -> nyttObservasjonsunderlag(aarsunderlag, m))
                .filter(isEmpty().negate());
    }

    /**
     * Genererer eit nytt observasjonsunderlag som inneheld alle perioder i årsunderlaget som er synlige pr siste
     * dag i den angitte månaden. Dersom årsunderlaget inneheld perioder som ikkje er synlige pr siste dag
     * i månaden blir det og generert ei ny, fiktiv periode basert på siste synlige periodes annotasjonar og koblingar.
     * Den einaste forskjellen mellom den fiktive perioda og siste synlige perioda blir tidsperioda den strekker seg
     * over, den fiktive perioda løper frå dagen etter siste synlige periodes til og med-dato, til og med siste dag i
     * året.
     *
     * @param aarsunderlag årsunderlaget som observasjonsunderlaget hentar synlige og bygger opp fiktive perioder frå
     * @param month        månaden som regulerer kva perioder i årsunderlaget som er synlige
     * @return eit nytt observasjonsunderlag
     */
    private Underlag nyttObservasjonsunderlag(final Underlag aarsunderlag, final Month month) {
        if (inneholderSistePeriode(aarsunderlag, month)) {
            return aarsunderlag
                    .restrict(allePerioder())
                    .annoter(Observasjonsdato.class, forSisteDag(aarsunderlag.annotasjonFor(Aarstall.class), month));
        }
        return new Underlag(
                concat(
                        synligePerioderFramTilOgMed(aarsunderlag, month),
                        fiktivPeriodeUtAaret(aarsunderlag, month)
                )
        )
                .annoterFra(aarsunderlag)
                .annoter(Observasjonsdato.class, forSisteDag(aarsunderlag.annotasjonFor(Aarstall.class), month));
    }

    /**
     * Inneheld den delen av årsunderlaget som er synlig siste dag i den angitt måneden,
     * ei underlagsperiode annotert med siste periode?
     * <p>
     *
     * @param aarsunderlag årsunderlaget som synlige perioder blir henta frå
     * @param month        måneden som regulerer kva perioder i årsunderlaget som er synlige og som skal tas hensyn til
     *                     i sjekken på om siste synlige periode er annotert som siste periode for stillingsforholdet
     * @return <code>true</code> dersom observasjonsunderlaget inneheld ei perioder annotert med siste periode,
     * <code>false</code> ellers
     * @see SistePeriode
     */
    private boolean inneholderSistePeriode(final Underlag aarsunderlag, final Month month) {
        return synligePerioderFramTilOgMed(aarsunderlag, month)
                .filter((Underlagsperiode p) -> p.valgfriAnnotasjonFor(SistePeriode.class).isPresent())
                .findAny()
                .isPresent();
    }

    /**
     * Genererer ei ny underlagsperiode som strekker seg frå dagen etter den angitte månedens siste dag, til
     * siste dag i årsunderlagets årstall.
     * <p>
     * Den fiktive periodas tilstand er ein eksakt kopi av
     * {@link #synligePerioderFramTilOgMed(Underlag, Month) siste synlige periode} frå årsunderlaget, sett bort frå
     * frå og med- og til og med-datoane. I tillegg blir den fiktive perioda annotert med {@link FiktivPeriode} for å
     * tydelig markere at perioda er fiktiv.
     * <p>
     * <h2>Unntakssituasjonar</h2>
     * <p>
     * Det er to situasjonar der det ikkje vil bli generert ei fiktiv periode:
     * <ul>
     * <li>Månaden er desember.</li>
     * <li>Stillingsforholdet blir sluttmeldt siste dag i siste synlige underlagsperiode.</li>
     * </ul>
     * <h3>1. Desember månad</h3>
     * Det blir ikkje generert ei fiktiv periode viss <code>month</code> er lik desember måned ettersom årsunderlaget
     * då vil vere identisk med observasjonsunderlaget. Siste dag i desember er også siste dag i året, ergo blir dei
     * to underlaga alltid like for desember måned.
     * <p>
     * <h3>2. Stillingsforholdet blir sluttmeldt siste dag i siste synlige underlagsperiode</h3>
     * Dersom siste synlige underlagsperiode er annotert med {@link SistePeriode} vil det ikkje bli generert ei fiktiv
     * periode ettersom dette betyr at stillingsforholdet er sluttmeldt på underlagsperiodas til og med-dato. Ergo
     * vil vi frå og med denne månaden sjå at stillingsforholdet er sluttmeldt og dermed vite at det ikkje
     * skal prognostiserast fram i tid som om det framleis er aktivt resten av året.
     *
     * @param aarsunderlag underlaget som siste synlige periode på eller før den aktuelle månaden, skal hentast frå
     * @param month        månaden som avgrensar kva perioder som er synlige
     * @return ein straum som inneheld ei fiktiv periode som strekker seg frå dagen etter siste dag i <code>month</code>
     * til siste dag i året, eller ein {@link java.util.stream.Stream#empty() tom} straum dersom det er desember måned
     */
    private Stream<Underlagsperiode> fiktivPeriodeUtAaret(final Underlag aarsunderlag, final Month month) {
        if (Month.DECEMBER.equals(month)) {
            return Stream.empty();
        }
        return synligePerioderFramTilOgMed(aarsunderlag, month)
                .reduce((a, b) -> b)
                .filter((Underlagsperiode p) -> !p.valgfriAnnotasjonFor(SistePeriode.class).isPresent())
                .map(this::nyFiktivPeriodeUtAaret)
                .map(Stream::of)
                .orElse(Stream.empty());
    }

    /**
     * Returnerer alle underlagsperioder som er startar og sluttar før eller på siste dag i den angitte måneden.
     *
     * @param aarsunderlag årsunderlaget som underlagsperiodene skal hentast frå
     * @param month        måneden der ein skal observere maskinelt grunnlag for heile året, pr siste dag i måneden
     * @return ein straum med alle underlagsperioder synlige fram til og med siste dag i måneden
     */
    private Stream<Underlagsperiode> synligePerioderFramTilOgMed(final Underlag aarsunderlag, final Month month) {
        return aarsunderlag
                .stream()
                .filter(
                        (Underlagsperiode p) -> p.annotasjonFor(Month.class)
                                .compareTo(month) <= 0
                );
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

    private static Predicate<Underlagsperiode> allePerioder() {
        return (Underlagsperiode p) -> true;
    }
}
