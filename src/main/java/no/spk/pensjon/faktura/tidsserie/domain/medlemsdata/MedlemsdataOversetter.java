package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import java.util.List;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.MedlemsdataOversetter} representerer
 * oversettingsalgoritma som genererer ein sterkt typa representasjon av eit medlemsspesifikk datasett av
 * ein bestemt type.
 *
 * @author Tarjei Skorgenes
 */
public interface MedlemsdataOversetter<T> {
    /**
     * Oversetter innholdet i <code>rad</code> til ei nytt medlemsspesifikt datatype.
     *
     * @param rad ei rad som inneheld medlemsspesifikk informasjon
     * @return ein ny instans av datatypen som informasjonen representerer
     */
    T oversett(List<String> rad);

    /**
     * Støttar oversettaren oversetting av den medlemsspesifikke informasjonen i <code>rad</code>
     * til ein sterkt typa datatype?
     *
     * @param rad ei rad som inneheld medlemsspesifikk informasjon
     * @return <code>true</code> dersom oversettaren støttar datatypen som <code>rad</code> representerer,
     * <code>false</code> viss oversettaren ikkje kan brukast for å oversette denne datatypen
     */
    boolean supports(List<String> rad);
}
