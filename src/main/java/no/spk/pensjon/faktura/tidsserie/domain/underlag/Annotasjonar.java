package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@SuppressWarnings("unchecked")
class Annotasjonar {
    private final Map<Class<?>, Object> annotasjonar = new HashMap<>();

    <T> void registrer(final Class<? extends T> type, final T verdi) {
        if (Optional.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "Annotasjonar av type Optional er ikkje støtta, " +
                            "viss du vil legge til ein valgfri annotasjon må den registrerast under verdiens egen type"
            );
        }
        Optional<T> v = ofNullable(verdi);
        if (verdi instanceof Optional) {
            v = (Optional<T>) verdi;
        }
        if (v.isPresent()) {
            annotasjonar.put(type, v.get());
        } else {
            annotasjonar.remove(type);
        }
    }

    <T> Optional<T> lookup(final Class<T> type) {
        return ofNullable((T) annotasjonar.get(type));
    }

    void addAll(final Annotasjonar other) {
        this.annotasjonar.putAll(other.annotasjonar);
    }

    void annoter(final Underlagsperiode periode) {
        annotasjonar.forEach((key, value) -> periode.annoter(key, value));
    }

    void remove(final Class<?> type) {
        this.annotasjonar.remove(type);
    }
}
