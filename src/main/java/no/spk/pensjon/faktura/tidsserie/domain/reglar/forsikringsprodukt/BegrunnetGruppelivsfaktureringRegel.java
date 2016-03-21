package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSFORHOLDID;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSPROSENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Stillingsfordeling;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;

/**
 * {@link BegrunnetGruppelivsfaktureringRegel} benytter samme strategi som
 * {@link no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel} for å beregne faktureringsandel
 * for gruppelivsproduktet.
 * <br>
 * BegrunnetGruppelivsfaktureringRegel gir tillegg en {@link Fordelingsaarsak} til at faktureringsandelen blir som den blir.
 *
 * @author Snorre E. Brekke - Computas
 * @since 2.3.0
 */
public class BegrunnetGruppelivsfaktureringRegel implements BeregningsRegel<BegrunnetFaktureringsandel> {
    private static final Prosent FULLTID = new Prosent("100%");

    @Override
    public BegrunnetFaktureringsandel beregn(final Beregningsperiode<?> periode) throws PaakrevdAnnotasjonManglarException {
        final StillingsforholdId stillingsid = periode.annotasjonFor(StillingsforholdId.class);
        final StandardFordelingsStrategi fordelingsstrategi = new StandardFordelingsStrategi(GRU, periode);

        return periode.annotasjonFor(AktiveStillingar.class)
                .stillingar()
                .sorted(SAMMENLIGN_STILLINGSPROSENT.reversed().thenComparing(SAMMENLIGN_STILLINGSFORHOLDID))
                .map(this::oppjusterTilFulltid)
                .reduce(
                        new Stillingsfordeling(fordelingsstrategi),
                        Stillingsfordeling::leggTil,
                        Stillingsfordeling::kombinerIkkeStoettet
                )
                .begrunnetAndelFor(stillingsid)
                .orElseThrow(() -> new IllegalStateException("Ingen begrunnet GRU-faktureringsandel finnes for stilling: " + stillingsid));

    }

    private AktivStilling oppjusterTilFulltid(final AktivStilling stilling) {
        return stilling.juster(FULLTID);
    }
}
