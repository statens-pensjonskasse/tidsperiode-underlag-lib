package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.time.LocalDate.MAX;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * {@link StillingsforholdPeriode} representerer ei periode der det ikkje skjer nokon endringar p� eit bestemt
 * stillingsforhold.
 * <p>
 * Periodene kan bli bygd opp enten basert p� ei medregningsperiode, eller som ei periode mellom to endringar i
 * stillingshistorikken tilknytta stillingsforholdet.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPeriode extends AbstractTidsperiode<StillingsforholdPeriode> {
    private final ArrayList<Stillingsendring> gjeldendeVerdier = new ArrayList<>();

    private final Optional<Medregningsperiode> medregning;

    /**
     * Konstruerer ei ny periode for eit stillingsforhold.
     *
     * @param fraOgMed aksjonsdatoen stillingsforholdet endrar tilstand
     * @param tilOgMed dagen f�r neste endring i tilstanden til stillingsforholdet, eller stillingsforholdets sluttdato
     *                 viss perioda representerer siste periode stillingsforholdet er aktivt f�r det blir sluttmeldt
     * @throws NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public StillingsforholdPeriode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
        this.medregning = empty();
    }

    /**
     * Konstruerer ei ny periode basert p� medregning.
     *
     * @param medregning medregningsperioda som stillingsforholdperioda representerer
     * @throws NullPointerException viss <code>medregning</code> er <code>null</code>
     */
    public StillingsforholdPeriode(final Medregningsperiode medregning) {
        super(
                requireNonNull(medregning, () -> "medregning er p�krevd, men var null").fraOgMed(),
                medregning.tilOgMed()
        );
        this.medregning = of(medregning);
    }

    /**
     * Stillingsendringene som gjelder fra og med periodens f�rste dag.
     * <p>
     * For stillingsforholdets siste periode vil det og kunne ligge inne en eller flere stillingsendringer som
     * representerer sluttmeldingen eller sluttmeldinger som avslutter stillingsforholdet.
     * <p>
     * Antagelse: For stillingsforhold der datakvaliteten er som forventet skal alle perioder f�r stillingsforholdets
     * siste periode kun v�re tillknyttet 1 stillingsendring.
     * <p>
     * Og for stillingsforholdets siste periode for stillingsforhold som ikke er aktive, dvs stillingsforhold som har
     * blitt korrekt registrert som sluttmeldt, vil siste periode inneholde to stillingsendringer, en for siste endring
     * f�r sluttmeldingen og en for selve sluttmeldingen.
     *
     * @return alle stillingsendringer som har en aksjonsdato som perioden overlapper, er garantert � inneholde minst
     * en endring
     */
    Iterable<Stillingsendring> endringer() {
        return unmodifiableList(gjeldendeVerdier);
    }

    /**
     * @see #leggTilOverlappendeStillingsendringer(java.util.List)
     * @see java.util.Arrays#asList
     */
    public StillingsforholdPeriode leggTilOverlappendeStillingsendringer(final Stillingsendring... endringer) {
        leggTilOverlappendeStillingsendringer(asList(endringer));
        return this;
    }

    /**
     * Kobler sammen perioden med alle stillingsendringer som perioden overlapper.
     * <p>
     * Kun stillingsendringer som faktisk overlapper perioden vil bli lagt til, <code>endringer</code> kan godt inneholde
     * ikke overlappende perioder, de vil ikke bli koblet til perioden.
     *
     * @param endringer en liste som inneholder alle stillingsendringer som skal fors�kes tilkoblet perioden
     * @return <code>this</code>
     */
    public StillingsforholdPeriode leggTilOverlappendeStillingsendringer(final List<Stillingsendring> endringer) {
        for (final Stillingsendring endring : endringer) {
            if (overlapper(endring.aksjonsdato())) {
                gjeldendeVerdier.add(endring);
            }
        }
        return this;
    }

    /**
     * Returnerer gjeldende stillingsendring for stillingsforholdperioder som er basert p� stillingshistorikk.
     * <p>
     * I situasjoner der en har mer enn en endring vil sist registrerte endring som ikke er en sluttmelding, bli brukt
     * som gjeldende endring. Sluttmeldinger blir ignorert fordi det ikke vil v�re mulig � vite hva som er gjeldende
     * aksjonskode for perioden dersom man plukker sluttmeldingen som gjeldende endring.
     * <p>
     * I situasjoner der stillingsperioden kun har tilknyttet en endring vil denne alltid v�re gjeldende endring for
     * perioden, selv om endringen er en sluttmelding. Dette impliserer at dersom stillingsforholdet er registrert med
     * kun en stillingsendring og det er en sluttmelding s� vil den 1 dag lange perioden aldri kunne bli tolket som
     * permisjon uten l�nn eller andre spesielle typer perioder der aksjonskode regulerer hvordan perioden skal beregnes.
     * <p>
     * Det � bruke sist registrerte endring er for�vrig en forenkling som ikke n�dvendigvis er funksjonelt �nskelig.
     * Det gjennst�r � finne gode konflikth�ndteringsstrategier for � velge "rett" endring for slike situasjonar
     * ettersom det i 90% av tilfellene vil vere en indikasjon p� at stillingens historikk er inkonsistent eller
     * mangelfull.
     * <p>
     * Dersom stillingsforholdet er basert p� medregning har ikkje perioden noen gjeldende stillingsendring.
     *
     * @return stillingsendringen som er utvalgt til � representere gjeldende tilstand for stillingsforholde i perioden,
     * eller {@link Optional#empty()} dersom stillingsforholdet er basert p� medregning
     */
    Optional<Stillingsendring> gjeldendeEndring() {
        if (gjeldendeVerdier.size() == 1) {
            return of(gjeldendeVerdier.get(0));
        }
        final Comparator<Stillingsendring> comparator = Comparator
                .comparing((Stillingsendring e) -> ofNullable(e.registreringsdato()).orElse(MAX))
                .reversed();
        final Predicate<Stillingsendring> erSluttmelding = Stillingsendring::erSluttmelding;
        return gjeldendeVerdier.stream().filter(erSluttmelding.negate()).sorted(comparator).findFirst();
    }

    /**
     * Returnerer medregningsperioden som stillingsforholdperioden er basert p�.
     * <p>
     * For stillingsforhold basert p� historikk har ikkje perioden noen medregningsperiode.
     *
     * @return medregningsperioden som stillingsforholdperioden er basert p�, eller {@link Optional#empty()} dersom
     * stillingsforholdet er basert p� historikk
     */
    private Optional<Medregningsperiode> medregning() {
        return medregning;
    }

    /**
     * Annoterer underlagsperioda med grunnlagsdata fr� stillingsforholdet gjeldande endring eller medregning.
     *
     * @param periode underlagsperioda som skal annoterast med grunnlagsdata
     * @see Medregningsperiode#annoter(Annoterbar)
     * @see Stillingsendring#annoter(Annoterbar)
     */
    public void annoter(final Annoterbar<?> periode) {
        gjeldendeEndring().ifPresent(endring -> {
            endring.annoter(periode);
        });
        medregning().ifPresent(medregning -> {
            medregning.annoter(periode);
        });
    }

    /**
     * Tilh�yrer denne perioda stillingsforholdet identifisert av <code>id</code>?
     *
     * @param id stillingsforholdnummeret som perioda skal sjekkast mot
     * @return <code>true</code> dersom perioda tilh�yrer stillingsforholdet identifisert av <code>id</code>,
     * <code>false</code> ellers
     * @throws IllegalStateException dersom perioda er i ei ugyldig tilstand og verken har medregning eller stillingsendring(ar)
     */
    public boolean tilhoeyrer(final StillingsforholdId id) {
        if (medregning.isPresent()) {
            return medregning.get().tilhoerer(id);
        }
        return gjeldendeEndring()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Stillingsforholdperioda "
                                        + this + " er i ei ugyldig tilstand, "
                                        + "den har verken ei medregning eller ei stillingsendring"
                        )
                )
                .tilhoerer(id);
    }

    @Override
    public String toString() {
        return "SP[" + fraOgMed() + "," + tilOgMed().map(Object::toString).orElse("->") + "]";
    }
}
