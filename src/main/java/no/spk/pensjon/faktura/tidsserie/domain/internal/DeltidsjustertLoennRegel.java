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
 * Deltidsjustert lønn blir beregna på ein av tre mulig måtar:
 * <ol>
 * <l>Dersom stillinga er tilknytta medregning, blir beløpet alltid lik kr 0</l>
 * <li>Dersom underlagsperioda er annotert med lønnstrinn, blir lønnstrinnet slått opp og konvertert til lønn i
 * 100% stilling, for deretter å blir deltidsjustert ut frå periodas stillingsprosent</li>
 * <li>Dersom underlagsperioda ikkje er annotert med lønnstrinn, blir deltidsjustert lønn slått opp direkte frå
 * periodas annotasjonar</li>
 * <li>Dersom perioda ikkje er annotert med verken lønnstrinn eller deltidsjustert lønn, feilar beregninga</li>
 * </ol>
 *
 * @author Tarjei Skorgenes
 */
public class DeltidsjustertLoennRegel implements BeregningsRegel<Kroner> {
    /**
     * Beregnar den deltidsjusterte årslønna som er gjeldande innanfor underlagsperioda.
     * <br>
     * Lønna blir enten beregna ut frå kombinasjonen av gjeldande lønnstrinnbeløp for periodas annoterte lønnstrinn,
     * justert i henhold til stillingsprosent, eller direkte basert på periodas annoterte deltidsjusterte lønn.
     * <p>
     * For stillingar tilknytta medregning og som dermed ikkje har deltidsjustert lønn, blir kr 0 brukt som verdi for
     * å unngå at regelen skal feile.
     * <p>
     * Beregninga genererer alltid den deltidsjusterte årslønna, den avkortar ikkje det genererte beløpet i henhold til
     * periodas årsfaktor. Det er klienten sitt ansvar å foreta avkortinga til periodas årsfaktor.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av beregningsregelen
     * @return den deltidsjusterte årslønna som er gjeldande innanfor underlagsperioda
     * @throws PaakrevdAnnotasjonManglarException dersom {@link LoennstrinnBeloep} eller {@link Stillingsprosent}
     *                                            manglar når {@link Loennstrinn} er annotert på perioda,
     *                                            eller dersom annotasjon for {@link DeltidsjustertLoenn} manglar
     *                                            når perioda ikkje er annotert med lønnstrinn
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
