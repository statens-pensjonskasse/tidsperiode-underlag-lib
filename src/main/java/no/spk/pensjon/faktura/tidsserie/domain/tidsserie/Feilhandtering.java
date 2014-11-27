package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link Feilhandtering} representerer strategien for å handtere feil som fører til at
 * tidsseriegenereringa til eit stillingsforhold blir avbrutt.
 *
 * @author Tarjei Skorgenes
 */
public interface Feilhandtering {
    /**
     * Notifiserer strategien om at tidsserie-genereringa for det aktuelle stillingsforholdet, har feila.
     *
     * @param stillingsforhold stillingsforholdet som tidsseriegenereringa har feila på
     * @param underlag         stillingsforholdunderlaget
     * @param t                feilen som har ført til at prosesseringa av stillingsforholdet har blitt avbrutt
     */
    public void handterFeil(StillingsforholdId stillingsforhold, Underlag underlag, Throwable t);
}
