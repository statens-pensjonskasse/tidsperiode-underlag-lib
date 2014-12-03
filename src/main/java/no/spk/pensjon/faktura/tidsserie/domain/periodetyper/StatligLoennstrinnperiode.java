package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link StatligLoennstrinnperiode} representerer kva som er gjeldande lønnsbeløp i statlig sektor, for eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} innanfor ei bestemt tidsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class StatligLoennstrinnperiode extends AbstractTidsperiode<StatligLoennstrinnperiode> {
    private final Loennstrinn trinn;
    private final LoennstrinnBeloep beloep;

    /**
     * Konstruerer ei ny lønnstrinnperiode som styrer kva som er gjeldande lønnsbeløp
     * for det angitte lønnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @param fraOgMed første dag beløpet er gjeldande lønnsbeløp for det aktuelle lønnstrinnet
     * @param tilOgMed siste dag beløpet er gjeldande lønnsbeløo for det aktuelle lønnstrinner, kan vere tom
     *                 for den siste lønnstrinnperioda som er gjeldande fram til neste gang lønnstrinna blir endra
     * @param trinn    lønnstrinnet som perioda representerer gjeldande lønnsbeløp for
     * @param beloep   lønnsbeløpet for ei 100% stilling med det aktuelle lønnstrinnet innanfor tidsperioda
     * @throws NullPointerException viss ein eller fleire av parameterverdiane er <code>null</code>
     */
    public StatligLoennstrinnperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                     final Loennstrinn trinn, final Kroner beloep) {
        super(fraOgMed, tilOgMed);
        this.trinn = requireNonNull(trinn, () -> "Lønnstrinn er påkrevd, men manglar");
        this.beloep = new LoennstrinnBeloep(
                requireNonNull(beloep, () -> "Beløp for lønnstrinn " + trinn + " er påkrevd, men manglar")
        );
    }

    /**
     * Inneheld perioda gjeldande lønn for lønnstrinnet?
     *
     * @param loennstrinn lønnstrinnet som det skal sjekkast mot
     * @return <code>true</code> dersom perioda inneheld gjeldande lønn for lønnstrinnet,
     * <code>false</code> ellers
     */
    public boolean harLoennFor(final Loennstrinn loennstrinn) {
        return trinn.equals(loennstrinn);
    }

    /**
     * Lønnstrinnet som perioda representerer gjeldande lønnsbeløp for.
     *
     * @return lønnstrinnet perioda tilhøyrer
     */
    public Loennstrinn trinn() {
        return trinn;
    }

    /**
     * Gjeldande lønnsbeløp for ei 100% stilling på det aktuelle lønnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @return lønna i 100% stilling tilknytta det aktuelle lønnstrinnet
     */
    public LoennstrinnBeloep beloep() {
        return beloep;
    }

    /**
     * Genererer ein kompakt og tekstlig representasjon av periodas tilstand.
     *
     * @return ein kompakt og tekstlig representasjon av periodas tilstand.
     */
    @Override
    public String toString() {
        return "SPK_LTR[" + fraOgMed() + "->" + tilOgMed().map(LocalDate::toString).orElse("") + "," + trinn + "," + beloep + "]";
    }
}