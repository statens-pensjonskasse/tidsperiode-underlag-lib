package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;

/**
 * Strategi som benyttes av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.Stillingsfordeling} for
 * å angi {@link Fordelingsaarsak} for en {@link AktivStilling}.
 * @author Snorre E. Brekke - Computas
 */
@FunctionalInterface
public interface FordelingsStrategi {
    /**
     * Angir {@link Fordelingsaarsak} for en {@link AktivStilling}.
     * @param stilling som skal klassifiseres
     * @return fordelingsårsak for stillingen
     */
    Fordelingsaarsak klassifiser(AktivStilling stilling);
}
