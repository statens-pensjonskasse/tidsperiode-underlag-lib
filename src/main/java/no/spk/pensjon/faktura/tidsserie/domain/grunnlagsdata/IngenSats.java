package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * Noop implementasjon av sats, som brukes dersom et avtaleprodukt ikke har verken krone- eller prosent-satser.
 * @author Snorre E. Brekke - Computas
 */
public final class IngenSats implements Sats{
    private static final IngenSats INGEN_SATS = new IngenSats();

    private IngenSats(){}

    /**
     * @return Sats som representerer at ingen sats er satt.
     */
    public static final Sats sats() {
        return INGEN_SATS;
    }
}
