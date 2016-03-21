package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSFORHOLDID;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSPROSENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVKORTET;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.UKJENT;

import java.util.Optional;

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
        final FakturerbareStillingerForPeriodeFactory fakturebareStillinger = new FakturerbareStillingerForPeriodeFactory(GRU, periode);

        final StillingsforholdId stillingsid = periode.annotasjonFor(StillingsforholdId.class);
        final Optional<FakturerbarStilling> stilling = fakturebareStillinger.stilling(stillingsid);

        final Prosent faktureringsandel = fakturebareStillinger
                .stillinger()
                .filter(s -> s.status().fakturerbar())
                .map(FakturerbarStilling::aktivStilling)
                .sorted(SAMMENLIGN_STILLINGSPROSENT.reversed().thenComparing(SAMMENLIGN_STILLINGSFORHOLDID))
                .map(this::oppjusterTilFulltid)
                .reduce(
                        new Stillingsfordeling(),
                        Stillingsfordeling::leggTil,
                        Stillingsfordeling::kombinerIkkeStoettet
                )
                .andelFor(stillingsid)
                .orElse(ZERO);

        final Fordelingsaarsak forelingsgrunn = stilling
                .map(e -> forelingsgrunn(e, faktureringsandel))
                .orElse(UKJENT);

        return new BegrunnetFaktureringsandel(
                stillingsid,
                faktureringsandel,
                forelingsgrunn
        );
    }

    private Fordelingsaarsak forelingsgrunn(FakturerbarStilling stilling, Prosent faktureringsandel) {
        if (stilling.status() == ORDINAER) {
            final Prosent oppjustertTilFulltid = oppjusterTilFulltid(stilling.aktivStilling()).stillingsprosent().orElse(Prosent.ZERO);
            return oppjustertTilFulltid.equals(faktureringsandel, 3) ? ORDINAER : AVKORTET;
        }
        return stilling.status();
    }

    private AktivStilling oppjusterTilFulltid(final AktivStilling stilling) {
        return stilling.juster(FULLTID);
    }
}
