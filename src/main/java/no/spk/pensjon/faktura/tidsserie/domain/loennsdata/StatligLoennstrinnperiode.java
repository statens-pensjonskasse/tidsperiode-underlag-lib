package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link StatligLoennstrinnperiode} representerer kva som er gjeldande l�nnsbel�p i statlig sektor, for eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} innanfor ei bestemt tidsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class StatligLoennstrinnperiode extends AbstractTidsperiode<StatligLoennstrinnperiode>
        implements Loennstrinnperiode<StatligLoennstrinnperiode> {
    private final Loennstrinn trinn;
    private final LoennstrinnBeloep beloep;

    /**
     * Konstruerer ei ny l�nnstrinnperiode som styrer kva som er gjeldande l�nnsbel�p
     * for det angitte l�nnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @param fraOgMed f�rste dag bel�pet er gjeldande l�nnsbel�p for det aktuelle l�nnstrinnet
     * @param tilOgMed siste dag bel�pet er gjeldande l�nnsbel�o for det aktuelle l�nnstrinner, kan vere tom
     *                 for den siste l�nnstrinnperioda som er gjeldande fram til neste gang l�nnstrinna blir endra
     * @param trinn    l�nnstrinnet som perioda representerer gjeldande l�nnsbel�p for
     * @param beloep   l�nnsbel�pet for ei 100% stilling med det aktuelle l�nnstrinnet innanfor tidsperioda
     * @throws NullPointerException viss ein eller fleire av parameterverdiane er <code>null</code>
     */
    public StatligLoennstrinnperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                     final Loennstrinn trinn, final Kroner beloep) {
        super(fraOgMed, tilOgMed);
        this.trinn = requireNonNull(trinn, () -> "L�nnstrinn er p�krevd, men manglar");
        this.beloep = new LoennstrinnBeloep(
                requireNonNull(beloep, () -> "Bel�p for " + trinn + " er p�krevd, men manglar")
        );
    }

    /**
     * Er l�nnstrinnperioda gjeldande for stillingar som jobbar p� avtalar tilknytta den aktuelle ordninga?
     * <p>
     * Statlige l�nnstrinnperioder er kun gjeldande for avtalar tilknytta SPK-ordninga.
     *
     * @param ordning pensjonsordninga l�nnstrinnperioda skal sjekkast mot
     * @return <code>true</code> dersom ordning er lik {@link Ordning#SPK}, <code>false</code> ellers
     */
    @Override
    public boolean tilhoeyrer(final Ordning ordning) {
        return Ordning.SPK.equals(ordning);
    }

    /**
     * Inneheld perioda gjeldande l�nn for l�nnstrinnet?
     *
     * @param loennstrinn   l�nnstrinnet som det skal sjekkast mot
     * @param stillingskode blir ignorert, statlige l�nnstrinn er definert uavhengig av stillingskode
     * @return <code>true</code> dersom perioda inneheld gjeldande l�nn for l�nnstrinnet,
     * <code>false</code> ellers
     */
    @Override
    public boolean harLoennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
        return trinn.equals(loennstrinn);
    }

    /**
     * L�nnstrinnet som perioda representerer gjeldande l�nnsbel�p for.
     *
     * @return l�nnstrinnet perioda tilh�yrer
     */
    public Loennstrinn trinn() {
        return trinn;
    }

    /**
     * Gjeldande l�nnsbel�p for ei 100% stilling p� det aktuelle l�nnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @return l�nna i 100% stilling tilknytta det aktuelle l�nnstrinnet
     */
    @Override
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