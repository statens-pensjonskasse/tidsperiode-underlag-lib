package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

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

    <T extends Tidsperiode<?>> Stream<T> get(final Class<T> type) {
        return ofNullable(koblingar.get(type))
                .orElse(emptySet())
                .stream()
                .map(p -> (T) p);
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
}
