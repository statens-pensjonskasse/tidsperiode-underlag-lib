package no.spk.pensjon.faktura.tidsserie.domain.at;

import java.util.stream.Stream;

/**
 * Hjelpeklasse for å kunne koble stillinger til avtaler som er definiert for en spesifikasjon.
 * @author Snorre E. Brekke - Computas
 */
@FunctionalInterface
public interface StillingAvtaler {
    Stream<StillingAvtale> stillinger();
}
