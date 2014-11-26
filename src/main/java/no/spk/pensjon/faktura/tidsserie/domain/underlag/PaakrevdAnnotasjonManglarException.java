package no.spk.pensjon.faktura.tidsserie.domain.underlag;

/**
 * Blir kasta viss oppslag av ein p�krevd annotasjon fr� ei {@link Annoterbar annoterbart} objekt ikkje er mulig
 * fordi objektet ikkje har blitt annotert med ein verdi av den p�krevde typen.
 *
 * @author Tarjei Skorgenes
 */
public class PaakrevdAnnotasjonManglarException extends RuntimeException {
    private static final long serialVersionUID = 5240556663220387894L;

    private final Annoterbar<?> kilde;

    private final Class<?> type;

    /**
     * Konstruerer ein ny feil for eit annoterbart objekt som ikkje er annotert med den p�krevde typen.
     *
     * @param kilde objektet som manglar den p�krevde annotasjonen
     * @param type  typen til den p�krevde annotasjonen
     */
    public PaakrevdAnnotasjonManglarException(final Annoterbar<?> kilde, final Class<?> type) {
        this.kilde = kilde;
        this.type = type;
    }

    @Override
    public String getMessage() {
        return kilde + " manglar ein p�krevd annotasjon av type " + type.getSimpleName();
    }
}
