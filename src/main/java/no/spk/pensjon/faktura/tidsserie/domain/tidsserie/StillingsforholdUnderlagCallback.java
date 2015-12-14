package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;

/**
 * {@link StillingsforholdUnderlagCallback} representerer
 * kontrakta mellom {@link StillingsforholdunderlagFactory}
 * og klientar av den som ønskjer å behandle/prosessere underlag avgrensa til eit og eit stillingsforhold
 * om gangen.
 * <p>
 * Designet brukar callbacks for å ein Stream-aktig prosessering så ein slepp å bygge underlag for alle
 * stillingsforholda tilknytta eit medlem før prosessering av desse kan starte.
 *
 * @author Tarjei Skorgenes
 */
public interface StillingsforholdUnderlagCallback {
    /**
     * Notifiserer om at eit nytt underlag har blitt generert basert på grunnlagsdata tilknytta
     * <code>stillingsforhold</code> i tillegg til andre tidsperiodiserte referansedata og regelsett.
     * <p>
     * Underlaga som blir generert blir automatisk avgrensa til å inneholde kun underlagsperioder
     * i perioda mellom stillingsforholdets første stillingsendring og stillingsforholdets sluttdato, eller
     * observasjonsperiodas sluttdato viss stillingsforholdet er aktivt ut forbi observasjonsperiodas sluttdato.
     *
     * @param stillingsforhold stillingsforholdet som har eit nytt underlag har blitt generert for
     * @param underlag         eit nytt underlag der alle underlagsperiodene er tilknytta <code>stillingsforhold</code>
     * @throws RuntimeException viss callbacken kastar ein runtimexception og ikkje handterer sine egne
     *                          feil som forventa vil slike feil bli ignorert av fasaden
     * @throws Error            viss callbacken kastar ein error vil fasaden avbryte vidare prosessering
     *                          umiddelbart
     */
    void prosesser(final StillingsforholdId stillingsforhold, final Underlag underlag);
}
