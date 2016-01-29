package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

class PensjonsproduktPensjonspremier {
    /**
     * Beregner premier for angitt produkt basert på grunnlag multiplisert med premiesatser for perioden.
     * Beregningsmetoden benyttes for produktene PEN, AFK og TIP.
     * @param periode perioden det skal beregnes pemier for
     * @param produkt produktet det skal beregnes premier for. Produktet må ha premiesatser angitt i {@link Prosent}.
     * @return premier for angitt produkt, gjeldene for angitt periode
     */
    Premier beregn(final Beregningsperiode<?> periode, final Produkt produkt) {
        final GrunnlagForPensjonsprodukt grunnlag = new GrunnlagForPensjonsprodukt(
                periode.beregn(MaskineltGrunnlagRegel.class)
        );
        return periode
                .annotasjonFor(Avtale.class)
                .premiesatsFor(produkt)
                .flatMap(Premiesats::prosentsatser)
                .map(satser -> premier()
                                .arbeidsgiver(premiebeloep(grunnlag, satser.arbeidsgiverpremie()))
                                .medlem(premiebeloep(grunnlag, satser.medlemspremie()))
                                .administrasjonsgebyr(premiebeloep(grunnlag, satser.administrasjonsgebyr()))
                )
                .orElse(premier())
                .bygg()
                ;
    }
}
