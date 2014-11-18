package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} representerer et l�nnstrinn fra
 * Statens l�nnsregulativ.
 * <br>
 * Ettersom bruttol�nn i 100% stilling varierer fra �r til �r, representerer ikke l�nnstrinnet hva som er bruttol�nnen
 * som l�nnstrinnet er assosiert med p� et gitt tidspunkt, dette h�ndteres via
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode underlagsperiodens} annotasjon for
 * {@link LoennstrinnBeloep}.
 *
 * @author Tarjei Skorgenes
 */
public class Loennstrinn {
    private final int trinn;

    /**
     * Konstruerer et nytt l�nnstrinn.
     *
     * @param trinn tallet som representerer hvilket l�nnstrinn som skal konstrueres
     */
    public Loennstrinn(final int trinn) {
        this.trinn = trinn;
    }

    /**
     * Konstruerer et nytt l�nnstrinn.
     *
     * @param text ein tekstlig representasjon av tallet som representerer l�nnstrinnet
     */
    public Loennstrinn(final String text) {
        this(Integer.parseInt(text));
    }

    @Override
    public int hashCode() {
        return trinn;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Loennstrinn other = (Loennstrinn) obj;
        return trinn == other.trinn;
    }

    @Override
    public String toString() {
        return "l�nnstrinn " + trinn;
    }
}
