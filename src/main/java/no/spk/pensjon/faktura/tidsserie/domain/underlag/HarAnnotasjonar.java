package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.Optional;

import no.spk.felles.tidsperiode.underlag.PaakrevdAnnotasjonManglarException;

/**
 * Rolle-interface som gir tilgang til annotasjonar som objektet har blitt annotert med.
 *
 * @author Tarjei Skorgenes
 */
public interface HarAnnotasjonar {
    /**
     * Slår opp verdien av den påkrevde annotasjonen med den angitte typen.
     * <p>
     * Dersom objektet ikkje har ein annotasjon av den angitte typen blir det kasta ein feil sidan annotasjonen blir
     * behandla som påkrevd. og dermed skulle ha vore tilgjengelig.
     *
     * @param <T>  annotasjonens type
     * @param type annotasjonens type
     * @return verdien av den angitte annotasjonstypen
     * @throws PaakrevdAnnotasjonManglarException viss obikkje har ein verdi for den angitte annotasjonstypen
     */
    <T> T annotasjonFor(Class<T> type) throws PaakrevdAnnotasjonManglarException;

    /**
     * Slår opp verdien av den valgfrie annotasjonen med den angitte typen.
     * <p>
     * Dersom objektet ikkje har ein annotasjon av den angitte typen blir det returnert ein {@link java.util.Optional#empty() tom}
     * verdi, det blir ikkje kasta nokon feil.
     *
     * @param <T>  annotasjonens type
     * @param type annotasjonens type
     * @return verdien av den angitte annotasjonstypen, eller {@link java.util.Optional#empty()} viss objektet ikkje har den
     * angitte annotasjonen
     */
    <T> Optional<T> valgfriAnnotasjonFor(Class<T> type);
}
