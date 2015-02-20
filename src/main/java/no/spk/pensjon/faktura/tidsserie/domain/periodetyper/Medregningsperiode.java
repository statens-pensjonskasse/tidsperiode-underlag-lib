package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link Medregningsperiode} representerer medregninga for eit bistilling eller ei stilling med tillegg fr� annan
 * arbeidsgivar.
 *
 * @author Tarjei Skorgenes
 */
public class Medregningsperiode extends AbstractTidsperiode<Medregningsperiode> {
    private final Medregning medregning;
    private final Medregningskode kode;
    private final StillingsforholdId stillingsforhold;

    /**
     * Konstruerer ei ny medregning som har ein fr� og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere l�pande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed         f�rste dag i tidsperioda
     * @param tilOgMed         viss {@link Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @param medregning       bel�pet som skal medregnast for stillingsforholdet
     * @param kode             medregningskoda som indikerer kva type medregning det er snakk om
     * @param stillingsforhold stillingsforholdnummeret som identifiserer stillingsforholdet medregningar tilh�yrer
     * @throws NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public Medregningsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                              final Medregning medregning, final Medregningskode kode,
                              final StillingsforholdId stillingsforhold) {
        super(fraOgMed, tilOgMed);
        this.medregning = requireNonNull(medregning, () -> "medregning er p�krevd, men var null");
        this.kode = requireNonNull(kode, () -> "medregningskode er p�krevd, men var null");
        this.stillingsforhold = requireNonNull(stillingsforhold, () -> "stillingsforhold er p�krevd, men var null");
    }

    /**
     * Tilh�yrer medregninge det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet som medregninga skal sjekkast om tilh�yrer
     * @return <code>true</code> dersom medreginga tilh�yrer stillingsforholdet,
     * <code>false</code> viss den tilh�yrer eit anna stillingsforhold
     */
    public boolean tilhoerer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Bel�pet som skal medregnast for stillingsforholdet.
     */
    public Medregning beloep() {
        return medregning;
    }

    /**
     * Kode som indikerer kva type medregning det er snakk om.
     * <p>
     * For fastsatsfaktureringa er det kun bistillingar og l�nn annen arbeidsgivar som skal medregnast.
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
