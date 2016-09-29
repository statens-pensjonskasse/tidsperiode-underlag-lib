package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;

import static java.util.Objects.requireNonNull;

/**
 * {@link Minstegrense} representerer grenseverdien som regulerer korvidt ei stilling skal kunne betale premie
 * basert på om stillingsprosenten er over eller under minstegrensa sin grenseverdi.
 * <p>
 * Avtalen som stilling tilhøyrer skal ikkje betale premie for stillingars underlagsperioder der stillingsprosenten er
 * lavare enn grenseverdien til minstegrensa som er gjeldande for stillinga innanfor perioda.
 *
 * @author Tarjei Skorgenes
 */
public class Minstegrense {
    private final Prosent grense;

    /**
     * Konstruerer ei ny minstegrense.
     *
     * @param grense grenseverdien som regulerer minste stillingsprosent som avtalen skal betale premie for
     * @throws NullPointerException dersom <code>grense</code> er <code>null</code>
     */
    public Minstegrense(final Prosent grense) {
        this.grense = requireNonNull(grense, () -> "minstegrense er påkrevd, men var null");
    }

    /**
     * Er den angitte stillingsstørrelsen under minstegrensa?
     *
     * @param stillingsprosent stillingsstørrelsen som skal samanliknast med minstegrensa
     * @return <code>true</code> dersom stillingsprosenten er lavare enn minstegrensa sin grenseverdi,
     * <code>false</code> om den er lik eller større enn grenseverdien
     */
    public boolean erUnderMinstegrensa(final Stillingsprosent stillingsprosent) {
        return grense.toDouble() > stillingsprosent.prosent().toDouble();
    }

    @Override
    public int hashCode() {
        return grense.toString().hashCode();
    }

    /**
     * Er <code>obj</code> ei minstegrense med lik verdi som gjeldande minstegrense?
     * <p>
     * Sidan minstegrensene kan inneholde desimalar, blir likheita evaluert med opp til 2 desimalar, om verdien etter
     * å ha blitt avrunda til 2 desimalar er lik, blir minstegrensene betrakta som like.
     *
     * @param obj objektet som minstegrensa skal samanliknast med
     * @return <code>true</code> dersom <code>obj</code> er ei minstegrense med lik prosentverdi,
     * <code>false</code> ellers
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final Minstegrense other = (Minstegrense) obj;
        return other.grense.toString().equals(grense.toString());
    }

    @Override
    public String toString() {
        return "minstegrense " + grense;
    }

    public Prosent grense() {
        return grense;
    }
}
