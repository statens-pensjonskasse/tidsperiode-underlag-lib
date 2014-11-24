package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

/**
 * {@link Observasjonspublikator} representerer ein ut-port eller -adapter
 * for observasjonar generert av {@link Tidsserie} for kvar måned i observasjonsunderlaget
 * tilknytta eit bestemt stillingsforhold, avtale, år og måned.
 * <p>
 * Publikatoren blir forventa å ta seg av vidare prosessering eller persistering av observasjonen.
 * Det er _sterkt_ ønskelig at vidare prosessering av observasjonane blir utført asynkront på ein anna tråd
 * enn tråden som kallar {@link #publiser(Object)} slik at prosesseringa av tidsserien kan fortsette til neste
 * observasjon utan å vente på I/O eller anna tidkrevande prosessering utført som følge av publiseringa.
 * <p>
 * Publikatoren blir og forventa å kopiere ut tilstanden frå observasjonen slik at sjølve observasjonen og
 * objekta den peikar til, kan garbage collectast umiddelbart etter at {@link #publiser(Object)} returnerer.
 * <p>
 * Intensjonen med den siste delen av kontrakta er å hindre eller sterkt redusere omfanget av objekt som overlever
 * lenge nok til å bli overført til old generation. Dette fordi GC av old generation forventast å kunne få omfattande
 * ytelsemessige konsekvensar for sjølve prosesseringa.
 *
 * @author Tarjei Skorgenes
 */
public interface Observasjonspublikator<T> {
    /**
     * Publiserer ein observasjon utført på eit observasjonsunderlag
     * av og som skal inkluderast i ein {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie}.
     *
     * @param event observasjonen som skal inngå som ein del av tidsserien
     */
    void publiser(final T event);
}
