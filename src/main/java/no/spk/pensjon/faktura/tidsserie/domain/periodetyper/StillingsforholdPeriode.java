package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.MAX;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

/**
 * {@link StillingsforholdPeriode} representerer ei periode der det ikkje skjer nokon endringar på eit bestemt
 * stillingsforhold.
 * <p>
 * Periodene kan bli bygd opp enten basert på ei medregningsperiode, eller som ei periode mellom to endringar i
 * stillingshistorikken tilknytta stillingsforholdet.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdPeriode extends AbstractTidsperiode<StillingsforholdPeriode> {
    private final ArrayList<Stillingsendring> gjeldendeVerdier = new ArrayList<>();

    /**
     * Konstruerer ei ny periode for eit stillingsforhold.
     *
     * @param fraOgMed aksjonsdatoen stillingsforholdet endrar tilstand
     * @param tilOgMed dagen før neste endring i tilstanden til stillingsforholdet, eller stillingsforholdets sluttdato
     *                 viss perioda representerer siste periode stillingsforholdet er aktivt før det blir sluttmeldt
     */
    public StillingsforholdPeriode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        super(fraOgMed, tilOgMed);
    }

    /**
     * Stillingsendringene som gjelder fra og med periodens første dag.
     * <p>
     * For stillingsforholdets siste periode vil det og kunne ligge inne en eller flere stillingsendringer som
     * representerer sluttmeldingen eller sluttmeldinger som avslutter stillingsforholdet.
     * <p>
     * Antagelse: For stillingsforhold der datakvaliteten er som forventet skal alle perioder før stillingsforholdets
     * siste periode kun være tillknyttet 1 stillingsendring.
     * <p>
     * Og for stillingsforholdets siste periode for stillingsforhold som ikke er aktive, dvs stillingsforhold som har
     * blitt korrekt registrert som sluttmeldt, vil siste periode inneholde to stillingsendringer, en for siste endring
     * før sluttmeldingen og en for selve sluttmeldingen.
     *
     * @return alle stillingsendringer som har en aksjonsdato som perioden overlapper, er garantert å inneholde minst
     * en endring
     */
    public Iterable<Stillingsendring> endringer() {
        return unmodifiableList(gjeldendeVerdier);
    }

    /**
     * Kobler sammen perioden med alle stillingsendringer som perioden overlapper.
     * <p>
     * Kun stillingsendringer som faktisk overlapper perioden vil bli lagt til, <code>endringer</code> kan godt inneholde
     * ikke overlappende perioder, de vil ikke bli koblet til perioden.
     *
     * @param endringer en liste som inneholder alle stillingsendringer som skal forsøkes tilkoblet perioden
     */
    public void leggTilOverlappendeStillingsendringer(final List<Stillingsendring> endringer) {
        for (final Stillingsendring endring : endringer) {
            if (overlapper(endring.aksjonsdato())) {
                gjeldendeVerdier.add(endring);
            }
        }
    }

    /**
     * Returnerer gjeldende stillingsendring.
     * <p>
     * I situasjoner der stillingsperioden kun har tilknyttet en endring vil denne alltid være gjeldende endring for perioden.
     * <p>
     * I situasjoner der en har mer enn en endring vil sist registrerte endring bli brukt som gjeldende endring. Dette er
     * en forenkling som ikke nødvendigvis er funksjonelt ønskelig. Det gjennstår å finne gode konflikthåndteringsstrategier
     * for å velge rett endring for desse situasjonene.
     *
     * @return stillingsendringen som er utvalgt til å representere gjeldende tilstand for stillingsforholde i perioden
     */
    public Stillingsendring gjeldende() {
        final Comparator<Stillingsendring> comparator = Comparator
                .comparing((Stillingsendring e) -> ofNullable(e.registreringsdato()).orElse(MAX))
                .reversed();
        return gjeldendeVerdier.stream().sorted(comparator).findFirst().get();
    }
}
