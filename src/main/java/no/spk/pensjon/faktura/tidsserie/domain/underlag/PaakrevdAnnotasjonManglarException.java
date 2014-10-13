package no.spk.pensjon.faktura.tidsserie.domain.underlag;

/**
 * Kastast viss oppslag av ein påkrevd annotasjon frå ei underlagsperiode ikkje er mulig
 * fordi underlagsperioda ikkje har blitt annotert med ein verdi for den påkrevde annotasjonstypen.
 *
 * @author Tarjei Skorgenes
 */
public class PaakrevdAnnotasjonManglarException extends RuntimeException {
    private final Underlagsperiode periode;

    private final Class<?> type;

    /**
     * Konstruerer ein ny feil for periode som ikkje er annotert med den angitte typen.
     *
     * @param periode underlagsperioda som manglar den påkrevde annotasjonen
     * @param type    typen til den påkrevde annotasjonen
     */
    public PaakrevdAnnotasjonManglarException(final Underlagsperiode periode, final Class<?> type) {
        this.periode = periode;
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Underlagsperioda frå " + periode.fraOgMed() + " til " +
                periode.tilOgMed().get() +
                " manglar påkrevd annotasjon av type " + type;
    }
}
