package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.LocalDate.MAX;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.PeriodiserStillingshistorikk} representerer
 * algoritmen for og stillingsendringene som skal periodiseres og danne nye {@link StillingsforholdPeriode}.
 *
 * @author Tarjei Skorgenes
 */
public class PeriodiserStillingshistorikk {
    private final ArrayList<Stillingsendring> endringer = new ArrayList<>();

    /**
     * Legger til stillingsendringer som skal benyttes av {@link #periodiser()} når den periodiserer
     * og konstruerer nye {@link StillingsforholdPeriode perioder}.
     *
     * @param endringer stillingsendringer som skal periodiseres
     * @return <code>this</code>
     */
    public PeriodiserStillingshistorikk addEndring(final Stream<Stillingsendring> endringer) {
        endringer.forEach(e -> this.endringer.add(e));
        return this;
    }

    /**
     * @see #addEndring(java.util.stream.Stream)
     */
    public PeriodiserStillingshistorikk addEndring(final Iterable<Stillingsendring> endringer) {
        endringer.forEach(e -> this.endringer.add(e));
        return this;
    }

    /**
     * Bygger opp ein ny, ferdig periodisert representasjon av eit stillingsforhold.
     * <p>
     * Stillingsforholdet er bygd opp av stillingsforholdperioder som representerer ei tidsperiode der stillingsforholdet
     * ikkje endrar tilstand.
     * <p>
     * Nye perioder blir generert for kvar aksjonsdato der stillingsforholdet endrar tilstand, for eksempel ved
     * lønnsendringar, permisjon eller andre typer endringar som endar opp i stillingshistorikken tilknytta
     * stillingsforholdet.
     * <p>
     * Periodiseringa blir påvirka dersom datakvaliteten på innslaga i historikken er dårlige og det totale
     * bildet av stillingsforholdets historikk er inkonsistent. Av denne grunn tar periodiseringa veldig lite hensyn
     * til aksjonskodene til endringane, den ser kun på aksjonsdatoane som er registrert og dannar perioder som løper
     * frå ein aksjonsdato til dagen før neste aksjonsdato.
     * <p>
     * Det einaste unntaket frå denne regelen er aksjonsdatoen til sluttmeldingar som ligg inne med ein aksjonsdato
     * som er større enn eller lik siste aksjonsdato som er registrert på stillingsforholdet. I dette tilfellet, og kun
     * i dette tilfellet vil aksjonsdatoen bli brukt for å avslutte stillingsforholdets siste periode. I alle andre
     * tilfelle vil stillingsforholdet bli behandla som løpande. VErken 011 eller 031 blir tolka på noko anna vis enn
     * ei 021 linje.
     * <p>
     * <Første aksjonsdato i historikken til stillingsforholdet blir alltid behandla som startmelding ettersom vi ikkje
     * er i stand til å gi noko bedre estimat på når stillingsforholdet startar viss det ikkje er ei startmelding som
     * er registrert denne dagen.
     *
     * @return ein ny og ferdig periodisert representasjon av stillingsforholdets endringar så lenge det har vore aktivt
     */
    public Optional<List<StillingsforholdPeriode>> periodiser() {
        if (endringer.isEmpty()) {
            return empty();
        }

        final Map<LocalDate, List<Stillingsendring>> prAksjonsdato = grupperPaaAksjonsdato();
        final LocalDate startDato = finnFoersteAksjonsdato(prAksjonsdato);
        final LocalDate sisteEndringsdato = finnSisteAksjonsdato(prAksjonsdato);
        final Optional<LocalDate> tilOgMed = finnStillingsforholdetsSluttdato(prAksjonsdato, sisteEndringsdato);

        final List<StillingsforholdPeriode> perioder = byggStillingsforholdperioder(prAksjonsdato.keySet(), startDato, tilOgMed);
        leggTilOverlappandeStillingsendringar(perioder);
        return of(perioder);
    }

    /**
     * Går gjennom alle stillingsendringane og legger dei til på stillingsforholdperioda som dei blir overlappa av.
     * <p>
     * Hensikta med dette er å gjere det mulig for seinare annotering og kvalitetskontroll å finne ut nøyaktig kva
     * lønnsinformasjon og anna stillingsfinformasjon frå historikken, som er gjeldande innanfor kvar enkelt periode.
     * <p>
     * Dersom ei og samme periode får lagt til meir enn ei stillingsendring (sett bort frå siste periode for avslutta
     * stillingar), så kan det ofte vere ein indikasjon på inkonsistent historikk og dårlig datakvalitet.
     *
     * @param perioder alle periodene stillinga stillingsforholdet er splitta opp i
     */
    private void leggTilOverlappandeStillingsendringar(final Iterable<StillingsforholdPeriode> perioder) {
        for (final StillingsforholdPeriode periode : perioder) {
            periode.leggTilOverlappendeStillingsendringer(endringer);
        }
    }

    /**
     * Bygger opp ei kronologisk sortert liste med stillingsforholdperioder basert på aksjonsdatoane.
     *
     * @param alleAksjonsdatoer alle aksjonsdatoar tilknytta stillingas stillingsendringar
     * @param startDato         datoen for stillingas første dag
     * @param tilOgMed          ein valgfri dato for siste dag stillinga er aktiv før den blir avslutta
     * @return ei kronologisk sortert liste med stillingsforholdperioder
     */
    private ArrayList<StillingsforholdPeriode> byggStillingsforholdperioder(
            final Set<LocalDate> alleAksjonsdatoer, final LocalDate startDato, final Optional<LocalDate> tilOgMed) {
        LocalDate fraOgMed = startDato;
        final ArrayList<StillingsforholdPeriode> perioder = new ArrayList<>();
        for (final LocalDate nesteFraOgMed : alleAksjonsdatoerUtenomStartOgSluttdato(alleAksjonsdatoer, startDato, tilOgMed)) {
            perioder.add(new StillingsforholdPeriode(fraOgMed, of(nesteFraOgMed.minusDays(1))));
            fraOgMed = nesteFraOgMed;
        }
        perioder.add(new StillingsforholdPeriode(fraOgMed, tilOgMed));
        return perioder;
    }

    /**
     * Genererer ei liste som inneheld alle aksjonsdatoane til stillingsforholdet utenom
     * stillingsforholdet sin startdato, og viss det er avslutta, sluttdato.
     * <p>
     * Hensikta med å fjerne første og siste dato er å unngå at det blir generert ei -1 dager lang første periode
     * for stillingsforholdet ut frå stillingsforholdet sin startdato, og ei 1-dagar lang siste periode på
     * stillingsforholdet sin sluttdato.
     * <p>
     * Lista er sortert i kronologisk rekkefølge med eldste dato første og yngste dato sist.
     *
     * @param aksjonsdatoer alle aksjonsdatoane til stillingsendringar tilknytta stillingsforholdet
     * @param fraOgMed      første aksjonsdato
     * @param tilOgMed
     * @return ei kronologisk sortert liste som inneheld alle aksjonsdatoar utanom stillingsforholdets start- og sluttdato
     */
    private List<LocalDate> alleAksjonsdatoerUtenomStartOgSluttdato(
            final Set<LocalDate> aksjonsdatoer, final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return aksjonsdatoer
                .stream()
                .filter(alleAksjonsdatoerUtenomFoersteOgSiste(fraOgMed, tilOgMed))
                .sorted(LocalDate::compareTo)
                .collect(toList());
    }

    /**
     * Returnerer eit predikat som kan brukast for å sjekke om ein dato er lik <code>fraOgMed</code> eller
     * <code>tilOgMed</code> sin verdi viss den ikkje er {@link java.util.Optional#empty() tom}.
     *
     * @param fraOgMed frå og med-datoen som ein dato skal sjekkast opp mot
     * @param tilOgMed ein valgfri dato som datoen skal sjekkast opp mot med mindre <code>tilOgMed</code> manglar verdi
     * @return eit nytt predikat for å sjekke ein dato opp mot <code>fraOgMed</code> og <code>tilOgMed</code>
     */
    private static Predicate<LocalDate> alleAksjonsdatoerUtenomFoersteOgSiste(final LocalDate fraOgMed,
                                                                              final Optional<LocalDate> tilOgMed) {
        return d -> !(d.isEqual(fraOgMed) || (d.isEqual(tilOgMed.orElse(MAX))));
    }

    /**
     * Grupperer stillingsendringane basert på endringane sin aksjonsdato.
     *
     * @return ein map som inneheld ein mapping frå aksjonsdato til ei liste med alle endringar som har samme aksjonsdato
     */
    private Map<LocalDate, List<Stillingsendring>> grupperPaaAksjonsdato() {
        return endringer
                .stream()
                .collect(
                        groupingBy(e -> e.aksjonsdato())
                );
    }

    /**
     * Prøvar å finne ut om stillingsforholdet er avslutta og dermed har ein sluttdato.
     * <p>
     * Strategien som blir benytta vil kun behandle stillingsforholdet som avslutta viss det er registrert ei
     * sluttmelding som har ein aksjonsdato som er lik <code>sisteAksjonsdato</code>.
     * <p>
     * Ei sluttmelding skal kun kunne eksistere som siste endring i historikken for stillingsforholdet. Sluttmeldingar
     * som har ein tidligare aksjonsdato er derfor eit tegn på ein inkonsistent historikk. I desse tilfella vil ikkje
     * stillingsforholdet bli betrakta som avslutta med mindre det har ei ny sluttmelding på siste aksjonsdato.
     *
     * @param endringerPrAksjonsdato alle stillingsendringane, gruppert pr aksjonsdato
     * @param sisteAksjonsdato       siste aksjonsdato som det eksisterer ei stillingsendring på
     * @return <code>sisteAksjonsdato</code> viss det er registrert ei sluttmelding denne dagen,
     * {@link java.util.Optional#empty()} ellers
     */
    private Optional<LocalDate> finnStillingsforholdetsSluttdato(
            final Map<LocalDate, List<Stillingsendring>> endringerPrAksjonsdato, final LocalDate sisteAksjonsdato) {
        return endringerPrAksjonsdato
                .get(sisteAksjonsdato)
                .stream()
                .filter(Stillingsendring::erSluttmelding)
                .findAny()
                .map(Stillingsendring::aksjonsdato);
    }

    /**
     * Hentar ut aksjonsdatoen til den siste/yngste stillingsendringa.
     * <p>
     * Funksjonen forutsetter at det eksisterer minst ei stillingsendring.
     *
     * @param endringerPrAksjonsdato alle stillingsendringane, gruppert pr aksjonsdato
     * @return aksjonsdatoen for den den yngste stillingsendringa
     */
    private LocalDate finnSisteAksjonsdato(final Map<LocalDate, List<Stillingsendring>> endringerPrAksjonsdato) {
        return endringerPrAksjonsdato
                .keySet()
                .stream()
                .max(LocalDate::compareTo)
                .get();
    }

    /**
     * Hentar ut aksjonsdatoen til den første/eldste stillingsendringa.
     * <p>
     * Funksjonen forutsetter at det eksisterer minst ei stillingsendring.
     *
     * @param endringerPrAksjonsdato alle stillingsendringane, gruppert pr aksjonsdato
     * @return aksjonsdatoen for den den eldste stillingsendringa
     */
    private LocalDate finnFoersteAksjonsdato(final Map<LocalDate, List<Stillingsendring>> endringerPrAksjonsdato) {
        return endringerPrAksjonsdato
                .keySet()
                .stream()
                .min(LocalDate::compareTo)
                .get();
    }
}
