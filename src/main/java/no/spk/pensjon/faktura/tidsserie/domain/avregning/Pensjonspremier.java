package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

class Pensjonspremier {
    Premier beregn(final Beregningsperiode<?> periode, final Produkt produkt) {
        final Premiebeloep grunnlag = premiebeloep(
                periode.beregn(MaskineltGrunnlagRegel.class)
        );
        return periode
                .annotasjonFor(Avtale.class)
                .premiesatsFor(produkt)
                .flatMap(Premiesats::prosentsatser)
                .map(satser -> premier()
                                .arbeidsgiver(grunnlag.multiply(satser.arbeidsgiverpremie()))
                                .medlem(grunnlag.multiply(satser.medlemspremie()))
                                .administrasjonsgebyr(grunnlag.multiply(satser.administrasjonsgebyr()))
                )
                .orElse(premier())
                .bygg()
                ;
    }

}
