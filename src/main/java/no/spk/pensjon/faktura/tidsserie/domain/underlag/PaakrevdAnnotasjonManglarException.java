package no.spk.pensjon.faktura.tidsserie.domain.underlag;

/**
 * Kastast viss oppslag av ein p�krevd annotasjon fr� ei underlagsperiode ikkje er mulig
 * fordi underlagsperioda ikkje har blitt annotert med ein verdi for den p�krevde annotasjonstypen.
 *
 * @author Tarjei Skorgenes
 */
public class PaakrevdAnnotasjonManglarException extends RuntimeException {
    private static final long serialVersionUID = 5240556663220387894L;

    private final Underlagsperiode periode;

    private final Class<?> type;

    /**
     * Konstruerer ein ny feil for periode som ikkje er annotert med den angitte typen.
     *
     * @param periode underlagsperioda som manglar den p�krevde annotasjonen
     * @param type    typen til den p�krevde annotasjonen
     */
    public PaakrevdAnnotasjonManglarException(final Underlagsperiode periode, final Class<?> type) {
        this.periode = periode;
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Underlagsperioda fr� " + periode.fraOgMed() + " til " +
                periode.tilOgMed().get() +
                " manglar p�krevd annotasjon av type " + type;
    }
}
