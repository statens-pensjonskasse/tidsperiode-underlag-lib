package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * Beregningsregel som reknar ut {@link no.spk.pensjon.faktura.tidsserie.domain.internal.Aarsfaktor}en til ei
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class AarsfaktorRegel implements BeregningsRegel<Aarsfaktor> {
    /**
     * Beregnar �rsfaktoren ut fr� lengda p� tidsperioda underlagsperioda strekker seg over
     * og lengda p� �ret underlagsperioda ligg innanfor.
     * <br>
     * �ret lengde blir beregna vha. {@link no.spk.pensjon.faktura.tidsserie.domain.internal.AarsLengdeRegel}
     * og lengda p� perioda blir beregna vha. {@link no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagarRegel}.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av
     *                beregningsregelen
     * @return �rsfaktoren for underlagsperioda
     * @throws PaakrevdAnnotasjonManglarException dersom nokon av reglane brukt ved beregning av antall dagar i perioda
     *                                            eller �ret, ikkje er annotert p� perioda
     * @see Aarsfaktor
     * @see AarsLengdeRegel
     */
    @Override
    public Aarsfaktor beregn(final Underlagsperiode periode) throws PaakrevdAnnotasjonManglarException {
        final AntallDagar dagarIPeriode = periode.beregn(AntallDagarRegel.class);
        final AntallDagar antallDagarIAaret = periode.beregn(AarsLengdeRegel.class);
        final double verdi = dagarIPeriode.verdi() / (double) antallDagarIAaret.verdi();
        if (verdi > 1d) {
            throw new IllegalArgumentException(
                    "�rsfaktor kan kun beregnast for perioder p� 1 �r eller kortare, men perioda var "
                            + dagarIPeriode + " lang, perioda det gjaldt er " + periode
            );
        }
        return new Aarsfaktor(verdi);
    }
}
