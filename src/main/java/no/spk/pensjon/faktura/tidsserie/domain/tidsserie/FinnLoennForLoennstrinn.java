package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Feilmeldingar.meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid;

/**
 * {@link FinnLoennForLoennstrinn} representerer algoritma som konverterer l�nnstrinn til l�nn i 100% stilling
 * basert p� ei underlagsperiodes {@link Loennstrinnperioder grupperte l�nnstrinn}-koblingar.
 * <p>
 * Algoritma forventar � finne maksimalt 1 gruppering som inneheld gjeldande l�nn for l�nnstrinnet i underlagsperiodas
 * tidsperiode.
 * <p>
 * Dersom det eksisterer meir enn ei gruppering som inneheld l�nn for l�nnstrinnet, indikerer det at ein har d�rlig
 * datakvalitet og algoritma feilar derfor for � gjere klientane oppmerksome p� problemet.
 * <p>
 * Dersom det ikkje eksisterer ei l�nnstrinnperiode som tilh�yrer l�nnstrinnet vil algoritma avslutte utan � feile.
 *
 * @author Tarjei Skorgenes
 */
public class FinnLoennForLoennstrinn {
    private final Set<Loennstrinnperioder> perioderForLoennstrinn = new HashSet<>(1);

    private final Underlagsperiode periode;

    private final Ordning ordning;

    private final Loennstrinn loennstrinn;

    private final Optional<Stillingskode> stillingskode;

    /**
     * Konstruerer ei ny algoritme som vil sl� opp l�nnstrinnets l�nn
     * fr� underlagsperiodas overlappande l�nnstrinnperioder.
     *
     * @param periode underlagsperioda som inneheld annotasjonane og koblingane som l�nna skal hentast ut basert p�
     */
    public FinnLoennForLoennstrinn(final Underlagsperiode periode) {
        this.periode = periode;
        this.ordning = periode.annotasjonFor(Ordning.class);
        this.loennstrinn = periode.annotasjonFor(Loennstrinn.class);
        this.stillingskode = periode.valgfriAnnotasjonFor(Stillingskode.class);
    }

    /**
     * Sl�r opp kva som er gjeldande l�nn for l�nnstrinnet kvar einaste dag i underlagsperioda.
     *
     * @return gjeldande l�nn for l�nnstrinnet, eller ingenting dersom underlagsperiode ikkje overlappar nokon
     * l�nnstrinnperioder som inneheld l�nn for l�nnstrinnet
     * @throws IllegalStateException dersom underlagsperioda er tilkobla fleire l�nnstrinnperioder som alle indikerer
     *                               at dei inneheld gjeldande l�nn for l�nnstrinnet
     */
    public Optional<LoennstrinnBeloep> loennForLoennstrinn() {
        return periode.koblingarAvType(Loennstrinnperioder.class)
                .filter(p -> p.tilhoeyrer(ordning))
                .filter(this::harLoennFor)
                .reduce(this::avbrytVissMeirEnnEinKandidat)
                .map((Loennstrinnperioder gruppering) -> gruppering.loennFor(loennstrinn, stillingskode))
                .flatMap((Optional<LoennstrinnBeloep> loenn) -> loenn);
    }

    /**
     * Sjekkar om grupperinga inneheld ei l�nnstrinnperiode som tilh�yrer l�nnstrinnet vi skal finne l�nn for.
     *
     * @param gruppering l�nnstrinngrupperinga som ein skal sjekke om inneheld l�nn for l�nnstrinnet
     * @return <code>true</code> dersom grupperinga inneheld l�nn for l�nnstrinnet, <code>false</code> ellers
     */
    private boolean harLoennFor(final Loennstrinnperioder gruppering) {
        return gruppering.harLoennFor(loennstrinn, stillingskode);
    }

    /**
     * Tar vare p� den den eller dei av dei to periodene som inneheld gjeldande l�nn for l�nnstrinnet i tidsperioda
     * underlagsperioda strekker seg over.
     *
     * @param current ei l�nnstrinnperiode som har l�nn for l�nnstrinnet
     * @param next    ei anna l�nnstrinnperiode som har l�nn for l�nnstrinnet
     * @return <code>next</code>
     */
    private Loennstrinnperioder avbrytVissMeirEnnEinKandidat(final Loennstrinnperioder current, final Loennstrinnperioder next) {
        add(current);
        add(next);
        return next;
    }

    /**
     * Tar vare p� grupperinga viss den inneheld gjeldande l�nn for l�nnstrinnet.
     *
     * @param gruppering ei samling l�nnstrinnperioder som ein skal sjekke om inneheld gjeldande l�nn for l�nnstrinnet
     */
    private void add(final Loennstrinnperioder gruppering) {
        perioderForLoennstrinn.add(gruppering);
        if (harFunneFleireKandidatarForLoennstrinn()) {
            throw new IllegalStateException(
                    meirEnnEiGjeldandeLoennstrinnPeriodeForSammeLoennstrinnPaaSammeTid(
                            periode,
                            ordning, loennstrinn, stillingskode,
                            perioderForLoennstrinn
                                    .stream()
                                    .toArray(Loennstrinnperioder[]::new)
                    )
            );
        }
    }

    private boolean harFunneFleireKandidatarForLoennstrinn() {
        return perioderForLoennstrinn.size() > 1;
    }
}
