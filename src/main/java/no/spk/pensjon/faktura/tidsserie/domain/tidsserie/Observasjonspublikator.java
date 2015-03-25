package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link Observasjonspublikator} representerer ein ut-port eller -adapter
 * for observasjonsunderlag generert av {@link TidsserieFacade}.
 * <p>
 * Publikatoren blir forventa � ta seg av vidare prosessering og persistering av observasjonsunderlaget.
 * Det er _sterkt_ �nskelig at persistering eller tidkrevande prosessering av observasjonsunderlaga blir
 * utf�rt asynkront p� ein anna tr�d enn tr�den som kallar {@link #publiser(Stream)} slik at prosesseringa
 * av tidsserien kan fortsette til neste stillingsforhold utan � vente p� I/O eller anna tidkrevande prosessering
 * utf�rt som f�lge av publiseringa.
 * <p>
 * Publikatoren blir og forventa � kopiere ut tilstanden fr� observasjonen slik at sj�lve observasjonen og
 * objekta den peikar til, kan garbage collectast umiddelbart etter at {@link #publiser(Stream)} returnerer.
 * <p>
 * Intensjonen med den siste delen av kontrakta er � hindre eller sterkt redusere omfanget av objekt som overlever
 * lenge nok til � bli overf�rt til old generation. Dette fordi GC av old generation forventast � kunne f� omfattande
 * ytelsemessige konsekvensar for sj�lve prosesseringa.
 *
 * @author Tarjei Skorgenes
 */
public interface Observasjonspublikator {
    /**
     * Publiserer ein straum av observasjonsunderlag generert basert
     * p� stillingsforholdunderlaget for eit bestemt stillingsforhold.
     *
     * @param observasjonsunderlag alle observasjonsunderlaga som blir generert for eit stillingsforhold
     */
    void publiser(final Stream<Underlag> observasjonsunderlag);
}
