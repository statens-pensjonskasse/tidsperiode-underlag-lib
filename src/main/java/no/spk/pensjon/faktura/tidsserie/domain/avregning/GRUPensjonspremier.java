package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

class GRUPensjonspremier {

    /**
     * Beregner premier for GRU basert på premiesatser for perioden multiplisert med angitt grunnlag.
     * Premiene avrundes til to desimaler.
     * @param periode perioden det skal beregnes pemier for
     * @param grunnlag grunnlaget for GRU-premier
     * @return premier for GRU, gjeldene for angitt periode
     */
    Premier beregn(final Beregningsperiode<?> periode, GrunnlagForGRU grunnlag) {
        return periode
                .annotasjonFor(Avtale.class)
                .premiesatsFor(GRU)
                .flatMap(Premiesats::beloepsatsar)
                .map(satser -> premier()
                        .arbeidsgiver(premiebeloep(grunnlag, satser.arbeidsgiverpremie()))
                        .medlem(premiebeloep(grunnlag, satser.medlemspremie()))
                        .administrasjonsgebyr(premiebeloep(grunnlag, satser.administrasjonsgebyr())))
                .orElse(premier())
                .bygg();
    }
}
