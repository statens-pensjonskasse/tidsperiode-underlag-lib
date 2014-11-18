package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.internal.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

class Feilmeldingar {
    static String feilmeldingForMeirEnnEiKobling(final Class<?> type, final Set<?> koblingar) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Underlagsperioda er kobla til meir enn ei tidsperiode av type ");
        builder.append(type.getSimpleName());
        builder.append(", vi forventa berre 1 kobling av denne typen.\n");
        builder.append("Koblingar:\n");
        koblingar.forEach(k -> builder.append("- ").append(k).append('\n'));
        return builder.toString();
    }

    /**
     * Genererer feilmeldinga som skal bli brukt når underlaget oppdagar at det er eit eller fleire tidsgap mellom
     * underlagsperiodene sine.
     *
     * @param message første linje i feilmeldinga som blir generert
     * @param tidsgap validatoren som inneheld informasjon om tidsperiodene som det er detektert gap mellom
     * @return ei feilmelding som beskriv feilen som har oppstått
     */
    static String feilmeldingVedTidsgapIUnderlaget(final String message, final DetekterTidsgapMellomPerioder tidsgap) {
        final StringBuilder builder = new StringBuilder();
        builder.append(message);
        builder.append(":\n");

        final Stream<Underlagsperiode[]> stream = tidsgap.stream();
        stream.forEach(array -> {
            final Underlagsperiode a = array[0];
            final Underlagsperiode b = array[1];
            builder
                    // Vi trekker frå 2 dagar her fordi til og med-dato alltid er 1 dag mellom til og med- og neste
                    // fra og med-dato. I tillegg må vi trekke frå 1 ekstra dag på grunn av måten vi sjekkar etter
                    // gap på i apply-metoda
                    .append(AntallDagar.antallDagarMellom(a.tilOgMed().orElse(LocalDate.MAX), b.fraOgMed().minusDays(2)))
                    .append(" tidsgap mellom ")
                    .append(a)
                    .append(" og ")
                    .append(b)
                    .append('\n');
        });
        return builder.toString();
    }
}
