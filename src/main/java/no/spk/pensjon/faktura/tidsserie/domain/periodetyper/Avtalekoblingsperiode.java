package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode} representerer avtalen
 * eit stillingsforhold er tilknytta i ei bestemt tidsperiode.
 * <p>
 * Sjølv om det i PUMA i dag ikkje er vanlig å finne fleire avtalekoblingsperioder for samme stillingsforhold
 * for samme avtale så er det fysisk og logisk mulig, det er derfor fullt mulig å ha fleire avtalekoblingsperioder
 * for samme stillingsforhold og avtale under forutsetning av at tidsperiodene ikkje overlappar.
 *
 * @author Tarjei Skorgenes
 */
public class Avtalekoblingsperiode extends GenerellTidsperiode {
    private final StillingsforholdId stillingsforhold;
    private final AvtaleId avtale;

    /**
     * Konstruerer ei ny avtalekobling mellom eit stillingsforhold og ein avtale i ei bestemt tidsperiode.
     *
     * @param fraOgMed         frå og med-dato for perioda
     * @param tilOgMed         til og med-dato for perioda
     * @param stillingsforhold stillingsforholdet avtalekoblinga tilhøyrer
     * @param avtale           avtalen stillingsforholdet er tilknytta innanfor perioda
     * @throws NullPointerException dersom verdien på eit eller fleire parameter er <code>null</code>
     */
    public Avtalekoblingsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                 final StillingsforholdId stillingsforhold, final AvtaleId avtale) {
        super(fraOgMed, tilOgMed);
        requireNonNull(stillingsforhold, () -> "stillingsforhold er påkrevd, men var null");
        requireNonNull(avtale, () -> "avtale er påkrevd, men var null");
        this.stillingsforhold = stillingsforhold;
        this.avtale = avtale;
    }

    /**
     * Stillingsforholdet avtalekoblinga tilhøyrer.
     *
     * @return stillingsforholdet avtalekoblinga tilhøyrer
     */
    public StillingsforholdId stillingsforhold() {
        return stillingsforhold;
    }

    /**
     * Avtalen stillingsforholdet er tilknytta innanfor perioda.
     *
     * @return avtalen stillingsforholdet er tilknytta
     */
    public AvtaleId avtale() {
        return avtale;
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s->%s]", "A", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), stillingsforhold, avtale);
    }
}