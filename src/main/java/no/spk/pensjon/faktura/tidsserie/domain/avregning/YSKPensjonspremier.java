package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

class YSKPensjonspremier {

    /**
     * Beregner premier for angitt produkt basert på premiesatser for perioden multiplisert med angitt premiefaktor.
     * Premiene avrundes til to desimaler.
     * Beregningsmetoden benyttes for produktene YSK og GRU.
     * @param periode perioden det skal beregnes pemier for
     * @param grunnlag grunnlaget for YSK-premier
     * @return premier for YSK, gjeldene for angitt periode
     */
    Premier beregn(final Beregningsperiode<?> periode, GrunnlagForYSK grunnlag) {
        return periode
                .annotasjonFor(Avtale.class)
                .premiesatsFor(YSK)
                .flatMap(Premiesats::beloepsatsar)
                .map(satser -> premier()
                        .arbeidsgiver(premiebeloep(grunnlag, satser.arbeidsgiverpremie()))
                        .medlem(premiebeloep(grunnlag, satser.medlemspremie()))
                        .administrasjonsgebyr(premiebeloep(grunnlag, satser.administrasjonsgebyr())))
                .orElse(premier())
                .bygg();
    }
}
