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
    /**
     * Konstruerer et nytt l�nnstrinn.
     *
     * @param trinn tallet som representerer hvilket l�nnstrinn som skal konstrueres
     */
    public Loennstrinn(final int trinn) {
    }
}
