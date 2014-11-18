package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn} representerer et lønnstrinn fra
 * Statens lønnsregulativ.
 * <br>
 * Ettersom bruttolønn i 100% stilling varierer fra år til år, representerer ikke lønnstrinnet hva som er bruttolønnen
 * som lønnstrinnet er assosiert med på et gitt tidspunkt, dette håndteres via
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode underlagsperiodens} annotasjon for
 * {@link LoennstrinnBeloep}.
 *
 * @author Tarjei Skorgenes
 */
public class Loennstrinn {
    private final int trinn;

    /**
     * Konstruerer et nytt lønnstrinn.
     *
     * @param trinn tallet som representerer hvilket lønnstrinn som skal konstrueres
     */
    public Loennstrinn(final int trinn) {
        this.trinn = trinn;
    }

    /**
     * Konstruerer et nytt lønnstrinn.
     *
     * @param text ein tekstlig representasjon av tallet som representerer lønnstrinnet
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
        return "lønnstrinn " + trinn;
    }
}
