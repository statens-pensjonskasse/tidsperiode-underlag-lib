package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * Strategi som benyttes av {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.Stillingsfordeling} for
 * å lage {@link BegrunnetFaktureringsandel} for en {@link AktivStilling}.
 * @author Snorre E. Brekke - Computas
 */
@FunctionalInterface
public interface FordelingsStrategi {
    /**
     * Lager {@link BegrunnetFaktureringsandel} for en {@link AktivStilling}. {@link BegrunnetFaktureringsandel#andel()}
     * skal ikke kunne overstige <code>maksimalAndel</code>.
     * @param stilling som skal får begrunnet faktureringsandel
     * @param maksimalAndel maksimal andel fordelingen skal tillate for stillingen.
     * @return fordelingsårsak for stillingen
     */
    BegrunnetFaktureringsandel begrunnetAndelFor(AktivStilling stilling, Prosent maksimalAndel);
}
