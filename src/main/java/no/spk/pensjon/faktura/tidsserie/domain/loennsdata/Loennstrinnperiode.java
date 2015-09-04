package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.util.Optional;

/**
 * {@link Loennstrinnperiode} representerer ei lønnstrinnperiode som inneheld kva som er gjeldande lønn i 100%
 * stilling for stillingar tilknytta eit bestemt lønnstrinn.
 * <p>
 * Ettersom definisjonane av kva som er gjeldande lønn i 100% stilling varierer frå ordning til ordning, og
 * apotekordninga har forskjellig lønn for forskjellige stillingskoder, er dette grensesnittet trekt ut for å illustrere
 * den felles kontrakta som alle dei forskjellige lønnstrinndefinisjonane må oppfylle.
 *
 * @param <T> type-referanse til klassa som implementerer grensesnittet
 * @author Tarjei Skorgenes
 */
public interface Loennstrinnperiode<T extends Loennstrinnperiode<T>> extends Tidsperiode<T> {
    /**
     * Er lønnstrinnperioda gjeldande for stillingar som jobbar på avtalar tilknytta den aktuelle ordninga?
     *
     * @param ordning pensjonsordninga lønnstrinnperioda skal sjekkast mot
     * @return <code>true</code> dersom lønnstrinnet tilhøyrer den angitte ordninga, <code>false</code> ellers
     */
    boolean tilhoeyrer(Ordning ordning);

    /**
     * Inneheld perioda gjeldande lønn for lønnstrinnet?
     *
     * @param loennstrinn   lønnstrinnet som det skal sjekkast mot
     * @param stillingskode kun påkrevd for ordningar som har forskjellige lønnstrinntabellar for forskjellige
     *                      stillingskoder
     * @return <code>true</code> dersom perioda inneheld gjeldande lønn for lønnstrinnet (og stillingskoda viss den er
     * påkrevd av ordningas lønnstrinntabell), <code>false</code> ellers
     */
    boolean harLoennFor(Loennstrinn loennstrinn, Optional<Stillingskode> stillingskode);

    /**
     * Gjeldande lønnsbeløp for ei 100% stilling på det aktuelle lønnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @return lønna i 100% stilling tilknytta det aktuelle lønnstrinnet
     */
    LoennstrinnBeloep beloep();

    /**
     * Lønnstrinnet som perioda inneheld gjeldande lønn i 100% stilling for.
     *
     * @return lønnstrinner perioda representerer gjeldande lønn for
     * @since 1.1.2
     */
    Loennstrinn trinn();
}
