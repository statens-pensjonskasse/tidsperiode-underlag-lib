package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Aarsfaktor;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * Konvertering fra tekst til datatyper som en underlagsperiode kan annoteres med.
 */
class KonverterFraTekst {
    static Kroner beloep(final String verdi) {
        return kroner(parseInt(verdi.replaceAll("kr", "").replaceAll(" ", "")));
    }

    static AarsfaktorRegel aarsfaktorRegel(final String verdi) {
        return new AarsfaktorRegel() {
            @Override
            public Aarsfaktor beregn(final Beregningsperiode<?> periode) {
                return new Aarsfaktor(parseDouble(verdi));
            }
        };
    }

    static AarsverkRegel aarsverkRegel(final String verdi) {
        return new AarsverkRegel() {
            @Override
            public Aarsverk beregn(final Beregningsperiode<?> periode) {
                return Aarsverk.aarsverk(prosent(verdi));
            }
        };
    }

    static MaskineltGrunnlagRegel pensjonsgivendeLoenn(final String verdi) {
        return new MaskineltGrunnlagRegel() {
            @Override
            public Kroner beregn(final Beregningsperiode<?> periode) {
                return beloep(verdi);
            }
        };
    }
}
