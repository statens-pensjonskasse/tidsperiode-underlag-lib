package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.Optional;

/**
 * Beregningsregel for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn}.
 * <br>
 * Deltidsjustert l�nn blir beregna p� ein av tre mulig m�tar:
 * <ol>
 * <l>Dersom stillinga er tilknytta medregning, blir bel�pet alltid lik kr 0</l>
 * <li>Dersom underlagsperioda er annotert med l�nnstrinn, blir l�nnstrinnet sl�tt opp og konvertert til l�nn i
 * 100% stilling, for deretter � blir deltidsjustert ut fr� periodas stillingsprosent</li>
 * <li>Dersom underlagsperioda ikkje er annotert med l�nnstrinn, blir deltidsjustert l�nn sl�tt opp direkte fr�
 * periodas annotasjonar</li>
 * <li>Dersom perioda ikkje er annotert med verken l�nnstrinn eller deltidsjustert l�nn, feilar beregninga</li>
 * </ol>
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoennRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar den deltidsjusterte �rsl�nna som er gjeldande innanfor underlagsperioda.
     * <br>
     * L�nna blir enten beregna ut fr� kombinasjonen av gjeldande l�nnstrinnbel�p for periodas annoterte l�nnstrinn,
     * justert i henhold til stillingsprosent, eller direkte basert p� periodas annoterte deltidsjusterte l�nn.
     * <p>
     * For stillingar tilknytta medregning og som dermed ikkje har deltidsjustert l�nn, blir kr 0 brukt som verdi for
     * � unng� at regelen skal feile.
     * <p>
     * Beregninga genererer alltid den deltidsjusterte �rsl�nna, den avkortar ikkje det genererte bel�pet i henhold til
     * periodas �rsfaktor. Det er klienten sitt ansvar � foreta avkortinga til periodas �rsfaktor.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av beregningsregelen
     * @return den deltidsjusterte �rsl�nna som er gjeldande innanfor underlagsperioda
     * @throws PaakrevdAnnotasjonManglarException dersom {@link LoennstrinnBeloep} eller {@link Stillingsprosent}
     *                                            manglar n�r {@link Loennstrinn} er annotert p� perioda,
     *                                            eller dersom annotasjon for {@link DeltidsjustertLoenn} manglar
     *                                            n�r perioda ikkje er annotert med l�nnstrinn
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) throws PaakrevdAnnotasjonManglarException {
        if (periode.valgfriAnnotasjonFor(Medregning.class).isPresent()) {
            return Kroner.ZERO;
        }
        final Optional<Loennstrinn> loennstrinn = periode.valgfriAnnotasjonFor(Loennstrinn.class);
        if (loennstrinn.isPresent()) {
            return periode
                    .annotasjonFor(LoennstrinnBeloep.class)
                    .deltidsJuster(
                            periode.annotasjonFor(Stillingsprosent.class)
                    )
                    .beloep();
        }
        return periode.annotasjonFor(DeltidsjustertLoenn.class).beloep();
    }
}
