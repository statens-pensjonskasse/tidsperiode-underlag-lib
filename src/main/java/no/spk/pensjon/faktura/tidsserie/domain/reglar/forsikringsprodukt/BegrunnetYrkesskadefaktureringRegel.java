package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSFORHOLDID;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSPROSENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Stillingsfordeling;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;

/**
 * {@link BegrunnetYrkesskadefaktureringRegel} benytter samme strategi som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel} for å beregne faktureringsandel
 * for yrkesskadeproduktet.
 * <br>
 * BegrunnetYrkesskadefaktureringRegel gir tillegg en {@link Fordelingsaarsak} til at faktureringsandelen blir som den blir.
 *
 * @author Snorre E. Brekke - Computas
 * @since 2.3.0
 */
public class BegrunnetYrkesskadefaktureringRegel implements BeregningsRegel<BegrunnetFaktureringsandel> {
    @Override
    public BegrunnetFaktureringsandel beregn(final Beregningsperiode<?> periode) throws PaakrevdAnnotasjonManglarException {
        final StillingsforholdId stillingsforhold = periode.annotasjonFor(StillingsforholdId.class);
        final ProduktavhengigFordelingsStrategi fordelingsstrategi = new ProduktavhengigFordelingsStrategi(YSK, periode.annotasjonFor(Medlemsavtalar.class));

        return periode.annotasjonFor(AktiveStillingar.class)
                .stillingar()
                .sorted(SAMMENLIGN_STILLINGSPROSENT.reversed().thenComparing(SAMMENLIGN_STILLINGSFORHOLDID))
                .reduce(
                        new Stillingsfordeling(fordelingsstrategi),
                        Stillingsfordeling::leggTil,
                        Stillingsfordeling::kombinerIkkeStoettet
                )
                .begrunnetAndelFor(stillingsforhold)
                .orElseThrow(() -> new IllegalStateException("Ingen begrunnet YSK-faktureringsandel finnes for stilling: " + stillingsforhold));

    }
}
