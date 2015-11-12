package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link LoennstilleggRegel} representerer
 * algoritma som reknar ut kor stort bel�p som blir utbetalt i l�nnstillegg for ei bestemt underlagsperiode.
 * <p>
 * Det totale l�nnstillegget er utregna basert p� summen av faste-, variable- og funksjonstillegg for perioda.
 * Det blir ikkje foretatt noka form for avkortning av l�nnstillegga dersom stillinga er under minstegrensa eller
 * over �vre grense for pensjonsgivande l�nn (10G/12G pr 2014).
 * <p>
 * Ingen av tillegga blir justert i henhold til stillingas stillingsprosent i perioda ettersom dei to
 * tilleggstypene som skal deltidjusterast, skal ha blitt det av arbeidsgivar f�r innrapportering. Den resterande
 * tilleggstypen, funksjonstillegg, skal aldri deltidsjusterast og blir innrapportert av arbeidsgivar i henhold til
 * dette.
 * <p>
 * Beregningsregelen returnerer alltid gjeldande �rlige tillegg uten � avkorte bel�pet i henhold til periodas
 * �rsfaktor. Avkorting i henhold til �rsfaktor er det derfor klienten som er ansvarlig for, dette er valgt
 * fordi ein antar at avkorting til �rsfaktor er eit felles ansvar som vil m�tte bli utf�rt fleire gangar pr periode
 * n�r ein skal gjere mange delberegningar. Ergo blir dette ansvaret plassert ute hos klienten for � hindre at ein skal
 * m�tte beregne �rsfaktor fleire gangar pr periode.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstilleggRegel implements BeregningsRegel<Kroner> {
    private static final Fastetillegg INGEN_FASTE_TILLEGG = new Fastetillegg(new Kroner(0));

    private static final Variabletillegg INGEN_VARIABLE_TILLEGG = new Variabletillegg(new Kroner(0));

    private static final Funksjonstillegg INGEN_FUNKSJONSTILLEGG = new Funksjonstillegg(new Kroner(0));

    /**
     * Reknar ut den totale summen av dei tre l�nnstillegga som stillinga har i den angitte perioda.
     *
     * @param periode underlagsperioda som totalt l�nnstillegg skal beregnast for
     * @return underlagsperiodas bidrag til premie�rets totale l�nnstillegg
     */
    @Override
    public Kroner beregn(final Beregningsperiode<?> periode) {
        if (periode.beregn(ErPermisjonUtanLoennRegel.class)) {
            return Kroner.ZERO;
        }
        return fastetillegg(periode)
                .plus(variabletillegg(periode))
                .plus(funksjonstillegg(periode));
    }

    private Kroner funksjonstillegg(final Beregningsperiode<?> periode) {
        return periode.valgfriAnnotasjonFor(Funksjonstillegg.class).orElse(INGEN_FUNKSJONSTILLEGG).beloep();
    }

    private Kroner variabletillegg(final Beregningsperiode<?> periode) {
        return periode.valgfriAnnotasjonFor(Variabletillegg.class).orElse(INGEN_VARIABLE_TILLEGG).beloep();
    }

    private Kroner fastetillegg(final Beregningsperiode<?> periode) {
        return periode.valgfriAnnotasjonFor(Fastetillegg.class).orElse(INGEN_FASTE_TILLEGG).beloep();
    }
}
