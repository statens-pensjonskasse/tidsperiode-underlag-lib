package no.spk.pensjon.faktura.tidsserie.domain.underlag;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel} representerer ein regel for beregning
 * av resultat som baserer seg p� grunnlagsdata som kan variere over tid og som normalt er tilgjengelig via
 * tidsperiodserte datatyper.
 * <p>
 * Beregningsregelens oppg�ve er � anta at underlagsperiodas tilstand inneheld alle verdiar som er p�krevd
 * for � utf�re ei bestemt type beregning og at den garanterer at desse vil vere konstante innanfor heile periodas
 * varigheit. Dermed slepp regelen � forholde seg til tidsdimensjonen og kan fokuserer p� � sl� opp p�krevde verdiar
 * fr� underlagsperioda, enten via annotasjonar eller ved � be andre beregningsreglar om � beregne p�krevde delresultat.
 *
 * @param <T> typen p� resultatet fr� beregninga regelen implementerer
 * @author Tarjei Skorgenes
 */
public interface BeregningsRegel<T> {
    /**
     * Beregnar ein verdi som ut fr� tidsperiodiserte data annotert eller utregna basert p� underlagsperiodas tilstand.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av beregningsregelen
     * @return resultatet som beregningsregelen har rekna ut basert p� underlagsperiodas tilstand
     */
    T beregn(final Underlagsperiode periode);
}
