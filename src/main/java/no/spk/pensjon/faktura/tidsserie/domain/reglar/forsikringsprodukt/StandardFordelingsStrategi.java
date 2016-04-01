package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVKORTET;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

/**
 * {@link FordelingsStrategi} som setter fordelingsårsak til {@link Fordelingsaarsak#ORDINAER} for alle stillinger
 * som ikke overstiger maksimal andel, {@link Fordelingsaarsak#AVKORTET} ellers.
 * @author Snorre E. Brekke - Computas
 */
public class StandardFordelingsStrategi implements FordelingsStrategi{
    @Override
    public BegrunnetFaktureringsandel begrunnetAndelFor(AktiveStillingar.AktivStilling stilling, Prosent maksimalAndel) {
        Fordelingsaarsak fordelingsaarsak = ORDINAER;

        Prosent nyAndel = stilling
                .stillingsprosent()
                .orElse(Prosent.ZERO);

        if (nyAndel.isGreaterThan(maksimalAndel)) {
            fordelingsaarsak = AVKORTET;
            nyAndel = maksimalAndel;
        }

        return new BegrunnetFaktureringsandel(
                stilling.stillingsforhold(),
                nyAndel,
                fordelingsaarsak
        );
    }
}
