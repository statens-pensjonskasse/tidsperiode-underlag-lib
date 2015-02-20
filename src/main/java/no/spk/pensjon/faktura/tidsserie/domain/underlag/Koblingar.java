package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("unchecked")
class Koblingar {
    private final Map<Class<?>, Set<Tidsperiode<?>>> koblingar = new HashMap<>();

    void add(Tidsperiode<?> kobling) {
        koblingar.computeIfAbsent(
                kobling.getClass(),
                c -> new HashSet<>()
        )
                .add(kobling);
    }

    void addAll(final Koblingar koblingar) {
        koblingar.koblingar.forEach((key, values) -> this.koblingar.put(key, new HashSet<>(values)));
    }

    <T extends Tidsperiode<?>> Stream<T> koblingarAvType(final Class<T> type) {
        return ofNullable(koblingar.get(type))
                .orElse(emptySet())
                .stream()
                .map(p -> (T) p);
    }

    <T extends Tidsperiode<T>> Optional<T> koblingAvType(final Class<T> type) {
        return koblingarAvType(type).reduce((a, b) -> {
            // Dersom det eksisterer meir enn 1 kobling av samme type blir denne metoda kalla, ergo feilar vi alltid her
            // Dersom det kun eksisterer ei kobling, eller ingen koblingar, kjem vi aldri inn hit
            throw new IllegalStateException(
                    feilmeldingForMeirEnnEiKobling(
                            type,
                            koblingarAvType(type).collect(toSet())
                    )
            );
        });
    }

    /**
     * Populerer underlagsperioda med alle koblingane som er lagt inn tidligare via {@link #add}.
     *
     * @param periode underlagsperioda som koblingane skal leggast til på
     */
    void kobleTil(final Underlagsperiode periode) {
        koblingar
                .values()
                .stream()
                .flatMap(Set::stream)
                .forEach(periode::kobleTil);
    }

    private static String feilmeldingForMeirEnnEiKobling(final Class<?> type, final Set<?> koblingar) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Underlagsperioda er kobla til meir enn ei tidsperiode av type ");
        builder.append(type.getSimpleName());
        builder.append(", vi forventa berre 1 kobling av denne typen.\n");
        builder.append("Koblingar:\n");
        koblingar.forEach(k -> builder.append("- ").append(k).append('\n'));
        return builder.toString();
    }
}
