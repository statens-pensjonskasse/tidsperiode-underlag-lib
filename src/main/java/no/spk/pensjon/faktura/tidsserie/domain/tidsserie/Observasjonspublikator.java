package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link Observasjonspublikator} representerer ein ut-port eller -adapter
 * for observasjonar generert av {@link TidsserieFacade} for kvar måned i observasjonsunderlaget
 * tilknytta eit bestemt stillingsforhold, avtale, år og måned.
 * <p>
 * Publikatoren blir forventa å ta seg av vidare prosessering eller persistering av observasjonen.
 * Det er _sterkt_ ønskelig at vidare prosessering av observasjonane blir utført asynkront på ein anna tråd
 * enn tråden som kallar {@link #publiser(TidsserieObservasjon)} slik at prosesseringa av tidsserien kan fortsette til neste
 * observasjon utan å vente på I/O eller anna tidkrevande prosessering utført som følge av publiseringa.
 * <p>
 * Publikatoren blir og forventa å kopiere ut tilstanden frå observasjonen slik at sjølve observasjonen og
 * objekta den peikar til, kan garbage collectast umiddelbart etter at {@link #publiser(TidsserieObservasjon)} returnerer.
 * <p>
 * Intensjonen med den siste delen av kontrakta er å hindre eller sterkt redusere omfanget av objekt som overlever
 * lenge nok til å bli overført til old generation. Dette fordi GC av old generation forventast å kunne få omfattande
 * ytelsemessige konsekvensar for sjølve prosesseringa.
 *
 * @author Tarjei Skorgenes
 */
public interface Observasjonspublikator {
    /**
     * Publiserer ein observasjon som skal inkluderast i ein tidsserie,
     * basert på ei eller fleire beregningar utført på eit observasjonsunderlag for ein
     * bestemt observasjonsdato.
     *
     * @param event observasjonen som skal inngå som ein del av tidsserien
     */
    void publiser(final TidsserieObservasjon event);
}
