package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;

/**
 * Dekoratorklasse som holder på {@link Fordelingsaarsak} som gjelder for en {@link AktivStilling}.
 * @author Snorre E. Brekke - Computas
 */
class FakturerbarStilling {

    private AktivStilling aktivStilling;
    private Fordelingsaarsak status;

    FakturerbarStilling(AktivStilling aktivStilling, Fordelingsaarsak status) {
        this.aktivStilling = aktivStilling;
        this.status = status;
    }

    Fordelingsaarsak status() {
        return status;
    }

    AktivStilling aktivStilling() {
        return aktivStilling;
    }
}
