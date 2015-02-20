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
 * {@link StatligLoennstrinnperiode} representerer kva som er gjeldande lønnsbeløp i statlig sektor, for eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} innanfor ei bestemt tidsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class StatligLoennstrinnperiode extends AbstractTidsperiode<StatligLoennstrinnperiode>
        implements Loennstrinnperiode<StatligLoennstrinnperiode> {
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
                requireNonNull(beloep, () -> "Beløp for " + trinn + " er påkrevd, men manglar")
        );
    }

    /**
     * Er lønnstrinnperioda gjeldande for stillingar som jobbar på avtalar tilknytta den aktuelle ordninga?
     * <p>
     * Statlige lønnstrinnperioder er kun gjeldande for avtalar tilknytta SPK-ordninga.
     *
     * @param ordning pensjonsordninga lønnstrinnperioda skal sjekkast mot
     * @return <code>true</code> dersom ordning er lik {@link Ordning#SPK}, <code>false</code> ellers
     */
    @Override
    public boolean tilhoeyrer(final Ordning ordning) {
        return Ordning.SPK.equals(ordning);
    }

    /**
     * Inneheld perioda gjeldande lønn for lønnstrinnet?
     *
     * @param loennstrinn   lønnstrinnet som det skal sjekkast mot
     * @param stillingskode blir ignorert, statlige lønnstrinn er definert uavhengig av stillingskode
     * @return <code>true</code> dersom perioda inneheld gjeldande lønn for lønnstrinnet,
     * <code>false</code> ellers
     */
    @Override
    public boolean harLoennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
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