package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.util.Optional;

/**
 * {@link Loennstrinnperiode} representerer ei l�nnstrinnperiode som inneheld kva som er gjeldande l�nn i 100%
 * stilling for stillingar tilknytta eit bestemt l�nnstrinn.
 * <p>
 * Ettersom definisjonane av kva som er gjeldande l�nn i 100% stilling varierer fr� ordning til ordning, og
 * apotekordninga har forskjellig l�nn for forskjellige stillingskoder, er dette grensesnittet trekt ut for � illustrere
 * den felles kontrakta som alle dei forskjellige l�nnstrinndefinisjonane m� oppfylle.
 *
 * @param <T> type-referanse til klassa som implementerer grensesnittet
 * @author Tarjei Skorgenes
 */
public interface Loennstrinnperiode<T extends Loennstrinnperiode<T>> extends Tidsperiode<T> {
    /**
     * Er l�nnstrinnperioda gjeldande for stillingar som jobbar p� avtalar tilknytta den aktuelle ordninga?
     *
     * @param ordning pensjonsordninga l�nnstrinnperioda skal sjekkast mot
     * @return <code>true</code> dersom l�nnstrinnet tilh�yrer den angitte ordninga, <code>false</code> ellers
     */
    boolean tilhoeyrer(Ordning ordning);

    /**
     * Inneheld perioda gjeldande l�nn for l�nnstrinnet?
     *
     * @param loennstrinn   l�nnstrinnet som det skal sjekkast mot
     * @param stillingskode kun p�krevd for ordningar som har forskjellige l�nnstrinntabellar for forskjellige
     *                      stillingskoder
     * @return <code>true</code> dersom perioda inneheld gjeldande l�nn for l�nnstrinnet (og stillingskoda viss den er
     * p�krevd av ordningas l�nnstrinntabell), <code>false</code> ellers
     */
    boolean harLoennFor(Loennstrinn loennstrinn, Optional<Stillingskode> stillingskode);

    /**
     * Gjeldande l�nnsbel�p for ei 100% stilling p� det aktuelle l�nnstrinnet innanfor den aktuelle tidsperioda.
     *
     * @return l�nna i 100% stilling tilknytta det aktuelle l�nnstrinnet
     */
    LoennstrinnBeloep beloep();

    /**
     * L�nnstrinnet som perioda inneheld gjeldande l�nn i 100% stilling for.
     *
     * @return l�nnstrinner perioda representerer gjeldande l�nn for
     * @since 1.1.2
     */
    Loennstrinn trinn();
}
