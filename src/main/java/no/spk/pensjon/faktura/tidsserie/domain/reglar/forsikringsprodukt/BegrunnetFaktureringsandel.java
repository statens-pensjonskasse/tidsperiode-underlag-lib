package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.util.Objects.requireNonNull;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;

/**
 * {@link BegrunnetFaktureringsandel} indikerer korvidt eit stillingsforhold skal bli fakturert for gruppeliv eller yrkesskade.
 * Denne klassen er en dekorator av {@link FaktureringsandelStatus}, som ikke inneholder årsak til fordeling.
 *
 * @author Snorre E. Brekke
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.BegrunnetGruppelivsfaktureringRegel
 * @since 2.3.0
 */
public class BegrunnetFaktureringsandel extends FaktureringsandelStatus {
    private final Fordelingsaarsak fordelingsaarsak;

    /**
     * Konstruerer ein ny status som skal indikere korvidt stillingsforholdet kan fakturerast for et produkt.
     *
     * @param stillingsforhold stillingsforholdet statusen gjeld for
     * @param andel            prosentandelen av premien som stillingsforholdet sin avtale skal betale
     * @param fordelingsaarsak årsaken til at stillingen har fått den faktureringsandelen den har
     */
    public BegrunnetFaktureringsandel(final StillingsforholdId stillingsforhold, final Prosent andel,
            final Fordelingsaarsak fordelingsaarsak) {
        super(stillingsforhold, andel);
        this.fordelingsaarsak = requireNonNull(fordelingsaarsak, "status var null, men er påkrevd");
    }

    /**
     * Indikerer korvidt stillingsforholdet skal bli fakturert for produktet og andel er større enn 0%.
     *
     * @return <code>true</code> viss stillingsforholdet skal bli fakturert for produktet og andel er større enn 0%, <code>false</code> ellers
     */
    @Override
    public boolean erFakturerbar() {
        return this.fordelingsaarsak().fakturerbar() && super.erFakturerbar();
    }

    /**
     * Angir årsaken til at stillingen har fått den faktureringsandelen den har.
     * @return fordelingsårsak
     */
    public Fordelingsaarsak fordelingsaarsak() {
        return fordelingsaarsak;
    }

    @Override
    public String toString() {
        return stillingsforhold()
                + " med faktureringsandelkode " + fordelingsaarsak  +
                " og andel " + andel();
    }
}
