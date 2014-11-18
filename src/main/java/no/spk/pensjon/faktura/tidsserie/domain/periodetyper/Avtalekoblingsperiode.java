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
 * Sj�lv om det i PUMA i dag ikkje er vanlig � finne fleire avtalekoblingsperioder for samme stillingsforhold
 * for samme avtale s� er det fysisk og logisk mulig, det er derfor fullt mulig � ha fleire avtalekoblingsperioder
 * for samme stillingsforhold og avtale under forutsetning av at tidsperiodene ikkje overlappar.
 *
 * @author Tarjei Skorgenes
 */
public class Avtalekoblingsperiode extends AbstractTidsperiode<Avtalekoblingsperiode> {
    private final StillingsforholdId stillingsforhold;
    private final AvtaleId avtale;

    /**
     * Konstruerer ei ny avtalekobling mellom eit stillingsforhold og ein avtale i ei bestemt tidsperiode.
     *
     * @param fraOgMed         fr� og med-dato for perioda
     * @param tilOgMed         til og med-dato for perioda
     * @param stillingsforhold stillingsforholdet avtalekoblinga tilh�yrer
     * @param avtale           avtalen stillingsforholdet er tilknytta innanfor perioda
     * @throws NullPointerException dersom verdien p� eit eller fleire parameter er <code>null</code>
     */
    public Avtalekoblingsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                 final StillingsforholdId stillingsforhold, final AvtaleId avtale) {
        super(fraOgMed, tilOgMed);
        requireNonNull(stillingsforhold, () -> "stillingsforhold er p�krevd, men var null");
        requireNonNull(avtale, () -> "avtale er p�krevd, men var null");
        this.stillingsforhold = stillingsforhold;
        this.avtale = avtale;
    }

    /**
     * Er avtalekoblinga tilknytta det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdnummeret avtalekoblinga skal sjekkast mot
     * @return <code>true</code> dersom avtalekoblinga er tilknytta det angitte stillingsforholdet,
     * <code>false</code> ellers
     */
    public boolean tilhoeyrer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Stillingsforholdet avtalekoblinga tilh�yrer.
     *
     * @return stillingsforholdet avtalekoblinga tilh�yrer
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