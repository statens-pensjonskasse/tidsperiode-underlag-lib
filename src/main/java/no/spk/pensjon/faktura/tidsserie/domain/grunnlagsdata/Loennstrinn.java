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
    /**
     * Konstruerer et nytt lønnstrinn.
     *
     * @param trinn tallet som representerer hvilket lønnstrinn som skal konstrueres
     */
    public Loennstrinn(final int trinn) {
    }
}
