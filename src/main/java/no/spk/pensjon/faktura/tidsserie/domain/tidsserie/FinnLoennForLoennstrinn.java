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
 * {@link FinnLoennForLoennstrinn} representerer algoritma som konverterer lønnstrinn til lønn i 100% stilling
 * basert på ei underlagsperiodes {@link Loennstrinnperioder grupperte lønnstrinn}-koblingar.
 * <p>
 * Algoritma forventar å finne maksimalt 1 gruppering som inneheld gjeldande lønn for lønnstrinnet i underlagsperiodas
 * tidsperiode.
 * <p>
 * Dersom det eksisterer meir enn ei gruppering som inneheld lønn for lønnstrinnet, indikerer det at ein har dårlig
 * datakvalitet og algoritma feilar derfor for å gjere klientane oppmerksome på problemet.
 * <p>
 * Dersom det ikkje eksisterer ei lønnstrinnperiode som tilhøyrer lønnstrinnet vil algoritma avslutte utan å feile.
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
     * Konstruerer ei ny algoritme som vil slå opp lønnstrinnets lønn
     * frå underlagsperiodas overlappande lønnstrinnperioder.
     *
     * @param periode underlagsperioda som inneheld annotasjonane og koblingane som lønna skal hentast ut basert på
     */
    public FinnLoennForLoennstrinn(final Underlagsperiode periode) {
        this.periode = periode;
        this.ordning = periode.annotasjonFor(Ordning.class);
        this.loennstrinn = periode.annotasjonFor(Loennstrinn.class);
        this.stillingskode = periode.valgfriAnnotasjonFor(Stillingskode.class);
    }

    /**
     * Slår opp kva som er gjeldande lønn for lønnstrinnet kvar einaste dag i underlagsperioda.
     *
     * @return gjeldande lønn for lønnstrinnet, eller ingenting dersom underlagsperiode ikkje overlappar nokon
     * lønnstrinnperioder som inneheld lønn for lønnstrinnet
     * @throws IllegalStateException dersom underlagsperioda er tilkobla fleire lønnstrinnperioder som alle indikerer
     *                               at dei inneheld gjeldande lønn for lønnstrinnet
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
     * Sjekkar om grupperinga inneheld ei lønnstrinnperiode som tilhøyrer lønnstrinnet vi skal finne lønn for.
     *
     * @param gruppering lønnstrinngrupperinga som ein skal sjekke om inneheld lønn for lønnstrinnet
     * @return <code>true</code> dersom grupperinga inneheld lønn for lønnstrinnet, <code>false</code> ellers
     */
    private boolean harLoennFor(final Loennstrinnperioder gruppering) {
        return gruppering.harLoennFor(loennstrinn, stillingskode);
    }

    /**
     * Tar vare på den den eller dei av dei to periodene som inneheld gjeldande lønn for lønnstrinnet i tidsperioda
     * underlagsperioda strekker seg over.
     *
     * @param current ei lønnstrinnperiode som har lønn for lønnstrinnet
     * @param next    ei anna lønnstrinnperiode som har lønn for lønnstrinnet
     * @return <code>next</code>
     */
    private Loennstrinnperioder avbrytVissMeirEnnEinKandidat(final Loennstrinnperioder current, final Loennstrinnperioder next) {
        add(current);
        add(next);
        return next;
    }

    /**
     * Tar vare på grupperinga viss den inneheld gjeldande lønn for lønnstrinnet.
     *
     * @param gruppering ei samling lønnstrinnperioder som ein skal sjekke om inneheld gjeldande lønn for lønnstrinnet
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
