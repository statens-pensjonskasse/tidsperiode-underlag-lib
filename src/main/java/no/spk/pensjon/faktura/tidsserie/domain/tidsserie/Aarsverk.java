package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * {@link Aarsverk} representerer 1 �rs arbeidsinnsats for eit stillingsforhold som har 100% stillingsprosent
 * gjennom heile �ret.
 *
 * @author Tarjei Skorgenes
 */
public class Aarsverk {
    /**
     * Hentar ut antall �rsverk som ein prosentverdi sett i forhold til 1 �rsverk.
     *
     * @return antall �rsverk i prosent
     */
    public Prosent tilProsent() {
        return new Prosent(0d);
    }
}
