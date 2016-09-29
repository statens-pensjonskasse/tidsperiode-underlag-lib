package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link Observasjonspublikator} representerer ein ut-port eller -adapter
 * for observasjonsunderlag generert av {@link TidsserieFacade}.
 * <p>
 * Publikatoren blir forventa å ta seg av vidare prosessering og persistering av observasjonsunderlaget.
 * Det er _sterkt_ ønskelig at persistering eller tidkrevande prosessering av observasjonsunderlaga blir
 * utført asynkront på ein anna tråd enn tråden som kallar {@link #publiser(Stream)} slik at prosesseringa
 * av tidsserien kan fortsette til neste stillingsforhold utan å vente på I/O eller anna tidkrevande prosessering
 * utført som følge av publiseringa.
 * <p>
 * Publikatoren blir og forventa å kopiere ut tilstanden frå observasjonen slik at sjølve observasjonen og
 * objekta den peikar til, kan garbage collectast umiddelbart etter at {@link #publiser(Stream)} returnerer.
 * <p>
 * Intensjonen med den siste delen av kontrakta er å hindre eller sterkt redusere omfanget av objekt som overlever
 * lenge nok til å bli overført til old generation. Dette fordi GC av old generation forventast å kunne få omfattande
 * ytelsemessige konsekvensar for sjølve prosesseringa.
 *
 * @author Tarjei Skorgenes
 */
public interface Observasjonspublikator {
    /**
     * Publiserer ein straum av observasjonsunderlag generert basert
     * på stillingsforholdunderlaget for eit bestemt stillingsforhold.
     *
     * @param observasjonsunderlag alle observasjonsunderlaga som blir generert for eit stillingsforhold
     */
    void publiser(final Stream<Underlag> observasjonsunderlag);
}
