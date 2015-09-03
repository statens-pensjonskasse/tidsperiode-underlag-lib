package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar.antallDagarMellom;

/**
 * Beregningsregel som reknar ut {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar} som ei
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode} strekker seg over.
 *
 * @author Tarjei Skorgenes
 */
public class AntallDagarRegel implements BeregningsRegel<AntallDagar> {
    /**
     * Beregnar lengda på tidsperioda underlagsperioda strekker seg over.
     * <br>
     * Beregninga teller med både frå og med- og til og med-datoane i tellinga av antall dagar.
     * <br>
     * Eksempel:
     * <br>
     * Ei underlagsperioda frå og med 2007.01.01 til og med 2007.01.30 skal resultere i eit resultat på
     * 30 antall dagar.
     *
     * @param periode underlagsperioda som beregningsregelen skal beregne lengda på
     * @return antall dagar underlagsperioda strekker seg over
     * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor
     * @see no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel
     */
    @Override
    public AntallDagar beregn(final Beregningsperiode<?> periode) {
        return periode.lengde();
    }
}
