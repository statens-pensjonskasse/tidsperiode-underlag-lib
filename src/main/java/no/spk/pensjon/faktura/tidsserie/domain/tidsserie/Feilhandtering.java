package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link Feilhandtering} representerer strategien for � handtere feil som f�rer til at
 * tidsseriegenereringa til eit stillingsforhold blir avbrutt.
 *
 * @author Tarjei Skorgenes
 */
public interface Feilhandtering {
    /**
     * Notifiserer strategien om at tidsserie-genereringa for det aktuelle stillingsforholdet, har feila.
     *
     * @param stillingsforhold stillingsforholdet som tidsseriegenereringa har feila p�
     * @param underlag         stillingsforholdunderlaget
     * @param t                feilen som har f�rt til at prosesseringa av stillingsforholdet har blitt avbrutt
     */
    public void handterFeil(StillingsforholdId stillingsforhold, Underlag underlag, Throwable t);
}
