package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * {@link Aarsverk} representerer 1 års arbeidsinnsats for eit stillingsforhold som har 100% stillingsprosent
 * gjennom heile året.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsverk {
    /**
     * Hentar ut antall årsverk som ein prosentverdi sett i forhold til 1 årsverk.
     *
     * @return antall årsverk i prosent
     */
    public Prosent tilProsent() {
        return new Prosent(0d);
    }
}
