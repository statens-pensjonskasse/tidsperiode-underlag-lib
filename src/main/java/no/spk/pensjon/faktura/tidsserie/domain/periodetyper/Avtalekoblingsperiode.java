package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

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
public class Avtalekoblingsperiode extends AbstractTidsperiode<Avtalekoblingsperiode> {
    private final StillingsforholdId stillingsforhold;
    private final AvtaleId avtale;
    private final Ordning ordning;

    /**
     * Konstruerer ei ny avtalekobling mellom eit stillingsforhold og ein avtale i ei bestemt tidsperiode.
     *
     * @param fraOgMed         frå og med-dato for perioda
     * @param tilOgMed         til og med-dato for perioda
     * @param stillingsforhold stillingsforholdet avtalekoblinga tilhøyrer
     * @param avtale           avtalen stillingsforholdet er tilknytta innanfor perioda
     * @param ordning          pensjonsordninga avtalen er tilknytta
     * @throws NullPointerException dersom verdien på eit eller fleire parameter er <code>null</code>
     */
    public Avtalekoblingsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                 final StillingsforholdId stillingsforhold, final AvtaleId avtale,
                                 final Ordning ordning) {
        super(fraOgMed, tilOgMed);
        this.stillingsforhold = requireNonNull(stillingsforhold, () -> "stillingsforhold er påkrevd, men var null");
        this.avtale = requireNonNull(avtale, () -> "avtale er påkrevd, men var null");
        this.ordning = requireNonNull(ordning, () -> "ordning er påkrevd, men var null");
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
     * Annoterer underlagsperioda med ordning og avtalenummer.
     *
     * @param periode underlagsperioda som skal annoterast
     */
    public void annoter(final Underlagsperiode periode) {
        periode.annoter(AvtaleId.class, avtale());
        periode.annoter(Ordning.class, ordning());
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

    /**
     * Pensjonsordninga avtalen er tilknytta.
     *
     * @return pensjonsordninga rdninga avtalen er tilknytta
     */
    private Ordning ordning() {
        return ordning;
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s->%s]", "A", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), stillingsforhold, avtale);
    }
}