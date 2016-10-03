package no.spk.felles.tidsperiode.underlag;

/**
 * {@link BeregningsRegel} representerer ein regel for beregning
 * av resultat som baserer seg på grunnlagsdata som kan variere over tid og som normalt er tilgjengelig via
 * tidsperiodserte datatyper.
 * <p>
 * Beregningsregelens oppgåve er å anta at beregningsperiodas tilstand inneheld alle verdiar som er påkrevd
 * for å utføre ei bestemt type beregning og at den garanterer at desse vil vere konstante innanfor heile periodas
 * varigheit. Dermed slepp regelen å forholde seg til tidsdimensjonen og kan fokuserer på å slå opp påkrevde verdiar
 * frå perioda, enten via annotasjonar eller ved å be andre beregningsreglar om å beregne påkrevde delresultat.
 *
 * @param <T> typen på resultatet frå beregninga regelen implementerer
 * @author Tarjei Skorgenes
 */
public interface BeregningsRegel<T> {
    /**
     * Beregnar ein verdi som ut frå tidsperiodiserte data annotert eller utregna basert på underlagsperiodas tilstand.
     *
     * @param periode beregningsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av beregningsregelen
     * @return resultatet som beregningsregelen har rekna ut basert på underlagsperiodas tilstand
     */
    T beregn(final Beregningsperiode<?> periode);
}
