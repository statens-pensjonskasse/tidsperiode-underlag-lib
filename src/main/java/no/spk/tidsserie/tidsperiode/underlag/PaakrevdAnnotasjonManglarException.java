package no.spk.tidsserie.tidsperiode.underlag;

/**
 * Blir kasta viss oppslag av ein påkrevd annotasjon frå ei {@link Annoterbar annoterbart} objekt ikkje er mulig
 * fordi objektet ikkje har blitt annotert med ein verdi av den påkrevde typen.
 *
 * @author Tarjei Skorgenes
 */
public class PaakrevdAnnotasjonManglarException extends RuntimeException {
    private static final long serialVersionUID = 5240556663220387894L;

    private final Object kilde;

    private final Class<?> type;

    /**
     * Konstruerer ein ny feil for eit annoterbart objekt som ikkje er annotert med den påkrevde typen.
     *
     * @param kilde objektet som manglar den påkrevde annotasjonen
     * @param type  typen til den påkrevde annotasjonen
     */
    public PaakrevdAnnotasjonManglarException(final Object kilde, final Class<?> type) {
        this.kilde = kilde;
        this.type = type;
    }

    @Override
    public String getMessage() {
        return kilde + " manglar ein påkrevd annotasjon av type " + type.getSimpleName();
    }
}
