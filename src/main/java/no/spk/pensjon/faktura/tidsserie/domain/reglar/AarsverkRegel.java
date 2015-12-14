package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link AarsverkRegel} beregnar årsverk for ei underlagsperiode.
 *
 * @author Tarjei Skorgenes
 */
public class AarsverkRegel implements BeregningsRegel<Aarsverk> {
    /**
     * Beregnar periodas årsverk basert på gjeldande stillingsprosent og periodas årsfaktor.
     * <p>
     * For perioder der vi ikkje har informasjon om gjeldande stillingsprosent, noko som skjer når perioda
     * er tilknytta medregning, blir resultatet av beregninga alltid lik 0 årsverk.
     * <p>
     * Merk at regelen kun implementerer beregning av årsverk innanfor eit og samme år, beregninga vil feile
     * for perioder som strekker seg over meir enn eit år enten i lengde eller ved at periodas frå og med- og til og
     * med-dato har forskjellig årstall.
     * <p>
     * Eksempel 1:
     * <p>
     * Ei periode som strekker seg frå 1. januar til 31. desember og der gjeldande stillingsprosent
     * er 50% resulterer i eit halvt årsverk.
     * <p>
     * Eksempel 2:
     * <p>
     * Ei periode som strekker seg frå 1. januar til 30. juni 2005 med ein stillingsprosent på 20% resulterer i
     * (181/365 * 20%) årsverk.
     * <p>
     * Eksempel 3:
     * <p>
     * Ei periode som startar 1. mars 1999 og strekker seg til 1. mars 2000 vil beregninga feile på.
     *
     * @param periode underlagsperioda som er annotert med stillingsprosent og årsfaktorregel
     * @return antall årsverk for perioda
     * @see no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent
     * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel
     */
    @Override
    public Aarsverk beregn(final Beregningsperiode<?> periode) {
        if (periode.fraOgMed().getYear() != periode.tilOgMed().get().getYear()) {
            throw new IllegalStateException(
                    "årsverk kan kun beregnast for underlagsperioder som startar og sluttar innanfor samme årstall.\n" +
                            "Underlagsperiode: " + periode
            );
        }
        return periode.beregn(AarsfaktorRegel.class)
                .multiply(
                        periode.valgfriAnnotasjonFor(Stillingsprosent.class)
                                .map(Stillingsprosent::prosent).orElse(Prosent.ZERO)
                );
    }
}
