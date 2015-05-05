package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;

import java.util.function.Predicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

public class YrkesskadefaktureringRegel implements BeregningsRegel<YrkesskadefaktureringStatus> {
    @Override
    public YrkesskadefaktureringStatus beregn(final Beregningsperiode<?> periode) {
        final Medlemsavtalar avtalar = periode.annotasjonFor(Medlemsavtalar.class);
        final Predicate<AktivStilling> harYrkesskade = s -> avtalar.betalarTilSPKFor(s.stillingsforhold(), YSK);
        final Predicate<AktivStilling> permisjonUtanLoenn = s -> s
                .aksjonskode()
                .filter(PERMISJON_UTAN_LOENN::equals)
                .isPresent();

        final StillingsforholdId stilling = periode.annotasjonFor(StillingsforholdId.class);
        return new YrkesskadefaktureringStatus(
                stilling,
                periode.annotasjonFor(AktiveStillingar.class)
                        .stillingar()
                        .filter(harYrkesskade)
                        .filter(permisjonUtanLoenn.negate())
                        .reduce(
                                new Stillingsfordeling(),
                                Stillingsfordeling::leggTil,
                                Stillingsfordeling::kombiner
                        )
                        .andelFor(stilling)
                        .orElse(ZERO) // Medregning manglar stillingsprosent og skal heller aldri belastast for YSK
        );
    }

}
