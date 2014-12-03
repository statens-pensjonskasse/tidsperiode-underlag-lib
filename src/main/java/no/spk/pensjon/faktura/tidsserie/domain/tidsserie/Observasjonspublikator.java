package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

/**
 * {@link Observasjonspublikator} representerer ein ut-port eller -adapter
 * for observasjonar generert av {@link Tidsserie} for kvar m�ned i observasjonsunderlaget
 * tilknytta eit bestemt stillingsforhold, avtale, �r og m�ned.
 * <p>
 * Publikatoren blir forventa � ta seg av vidare prosessering eller persistering av observasjonen.
 * Det er _sterkt_ �nskelig at vidare prosessering av observasjonane blir utf�rt asynkront p� ein anna tr�d
 * enn tr�den som kallar {@link #publiser(Object)} slik at prosesseringa av tidsserien kan fortsette til neste
 * observasjon utan � vente p� I/O eller anna tidkrevande prosessering utf�rt som f�lge av publiseringa.
 * <p>
 * Publikatoren blir og forventa � kopiere ut tilstanden fr� observasjonen slik at sj�lve observasjonen og
 * objekta den peikar til, kan garbage collectast umiddelbart etter at {@link #publiser(Object)} returnerer.
 * <p>
 * Intensjonen med den siste delen av kontrakta er � hindre eller sterkt redusere omfanget av objekt som overlever
 * lenge nok til � bli overf�rt til old generation. Dette fordi GC av old generation forventast � kunne f� omfattande
 * ytelsemessige konsekvensar for sj�lve prosesseringa.
 *
 * @author Tarjei Skorgenes
 */
public interface Observasjonspublikator<T> {
    /**
     * Publiserer ein observasjon utf�rt p� eit observasjonsunderlag
     * av og som skal inkluderast i ein {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}.
     *
     * @param event observasjonen som skal inng� som ein del av tidsserien
     */
    void publiser(final T event);
}
