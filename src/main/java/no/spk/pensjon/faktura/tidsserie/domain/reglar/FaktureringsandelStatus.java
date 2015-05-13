package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Objects.requireNonNull;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

/**
 * {@link FaktureringsandelStatus} indikerer korvidt eit stillingsforhold skal bli fakturert for gruppeliv.
 *
 * @author Tarjei Skorgenes
 * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel
 */
public class FaktureringsandelStatus {
    private final StillingsforholdId id;
    private final Prosent andel;

    /**
     * Konstruerer ein ny status som skal indikere korvidt stillingsforholdet kan fakturerast for gruppelivsproduktet.
     *
     * @param stillingsforhold stillingsforholdet statusen gjeld for
     * @param andel            prosentandelen av gruppelivspremien som stillingsforholdet sin avtale skal betale
     */
    public FaktureringsandelStatus(final StillingsforholdId stillingsforhold, final Prosent andel) {
        this.id = requireNonNull(stillingsforhold, "stillingsforhold var null, men er påkrevd");
        this.andel = requireNonNull(andel, "andel var null, men er påkrevd");
    }

    /**
     * Indikerer korvidt stillingsforholdet skal bli fakturert for gruppelivsproduktet.
     *
     * @return <code>true</code> viss stillingsforholdet skal bli fakturert for gruppeliv, <code>false</code> ellers
     */
    public boolean erFakturerbar() {
        return andel.isGreaterThan(Prosent.ZERO);
    }

    /**
     * Stillingsforholdet statusen gjeld for.
     *
     * @return stillingsforholdet statusen gjeld for.
     */
    public StillingsforholdId stillingsforhold() {
        return id;
    }

    public Prosent andel() {
        return andel;
    }

    @Override
    public String toString() {
        return stillingsforhold()
                + (erFakturerbar() ? " skal " : " skal ikkje ")
                + "betale premie";
    }
}
