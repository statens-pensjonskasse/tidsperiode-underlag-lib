package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * Feilmeldingar generert av tidsserie-genereringa.
 *
 * @author Tarjei Skorgenes
 */
public class Feilmeldingar {

    /**
     * Opprettar ein ny reducer som feilar umiddelbart viss den blir kalla ein eller fleire gangar.
     * <p>
     * Feilen den genererer oppstår dersom det eksisterer meir enn ein avtaleversjon tilknytta <code>avtale</code>
     * innanfor tidsperioda angitt av <code>periode</code>. Det blir då umulig å avgjere kva for ein av dei to
     * avtaleversjonane som skal benyttast ved vidare annotering av underlagsperioda.
     *
     * @param <T>     periodetypa som det eksisterer meir enn 1 av
     * @param type    periodetypa som det eksisterer meir enn 1 av
     * @param avtale  avtalen
     * @param periode underlagsperioda som det eksisterer meir enn ein gjeldande avtaleversjon for
     *                og der alle versjonane er tilknytta <code>avtale</code>
     * @return ein ny reducer som feilar dersom den blir kalla ein eller fleire gangar
     */
    public static <T> BinaryOperator<T> feilDersomPeriodaOverlapparMeirEnnEin(
            final Class<T> type, final AvtaleId avtale, final Tidsperiode<?> periode) {
        return (a, b) -> {
            final StringBuilder builder = new StringBuilder();
            builder.append("Klarer ikkje å entydig avgjere kva som er gjeldande " + type.getSimpleName() + " for ")
                    .append(avtale)
                    .append(" i perioda ")
                    .append(periode)
                    .append(".\n" + type + " som overlappar perioda:\n");
            Stream.of(a, b).forEach(versjon -> {
                builder.append("- ");
                builder.append(versjon.toString());
                builder.append('\n');
            });
            throw new IllegalStateException(builder.toString());
        };
    }
}