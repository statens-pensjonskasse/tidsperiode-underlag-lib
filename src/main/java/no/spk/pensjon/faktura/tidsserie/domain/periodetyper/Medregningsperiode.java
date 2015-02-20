package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link Medregningsperiode} representerer medregninga for eit bistilling eller ei stilling med tillegg frå annan
 * arbeidsgivar.
 *
 * @author Tarjei Skorgenes
 */
public class Medregningsperiode extends AbstractTidsperiode<Medregningsperiode> {
    private final Medregning medregning;
    private final Medregningskode kode;
    private final StillingsforholdId stillingsforhold;

    /**
     * Konstruerer ei ny medregning som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed         første dag i tidsperioda
     * @param tilOgMed         viss {@link Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @param medregning       beløpet som skal medregnast for stillingsforholdet
     * @param kode             medregningskoda som indikerer kva type medregning det er snakk om
     * @param stillingsforhold stillingsforholdnummeret som identifiserer stillingsforholdet medregningar tilhøyrer
     * @throws NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public Medregningsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                              final Medregning medregning, final Medregningskode kode,
                              final StillingsforholdId stillingsforhold) {
        super(fraOgMed, tilOgMed);
        this.medregning = requireNonNull(medregning, () -> "medregning er påkrevd, men var null");
        this.kode = requireNonNull(kode, () -> "medregningskode er påkrevd, men var null");
        this.stillingsforhold = requireNonNull(stillingsforhold, () -> "stillingsforhold er påkrevd, men var null");
    }

    /**
     * Tilhøyrer medregninge det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet som medregninga skal sjekkast om tilhøyrer
     * @return <code>true</code> dersom medreginga tilhøyrer stillingsforholdet,
     * <code>false</code> viss den tilhøyrer eit anna stillingsforhold
     */
    public boolean tilhoerer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Beløpet som skal medregnast for stillingsforholdet.
     */
    public Medregning beloep() {
        return medregning;
    }

    /**
     * Kode som indikerer kva type medregning det er snakk om.
     * <p>
     * For fastsatsfaktureringa er det kun bistillingar og lønn annen arbeidsgivar som skal medregnast.
     */
    public Medregningskode kode() {
        return kode;
    }

    /**
     * Stillingsforholdet medregninga er tilknytta.
     *
     * @return medregningas stillingsforhold
     */
    public StillingsforholdId stillingsforhold() {
        return stillingsforhold;
    }
}
