package no.spk.pensjon.faktura.tidsserie.domain.internal;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link LoennstilleggRegel} representerer
 * algoritma som reknar ut kor stort beløp som blir utbetalt i lønnstillegg for ei bestemt underlagsperiode.
 * <p>
 * Det totale lønnstillegget er utregna basert på summen av faste-, variable- og funksjonstillegg for perioda.
 * Det blir ikkje foretatt noka form for avkortning av lønnstillegga dersom stillinga er under minstegrensa eller
 * over øvre grense for pensjonsgivande lønn (10G/12G pr 2014).
 * <p>
 * Ingen av tillegga blir justert i henhold til stillingas stillingsprosent i perioda ettersom dei to
 * tilleggstypene som skal deltidjusterast, skal ha blitt det av arbeidsgivar før innrapportering. Den resterande
 * tilleggstypen, funksjonstillegg, skal aldri deltidsjusterast og blir innrapportert av arbeidsgivar i henhold til
 * dette.
 * <p>
 * Ettersom dei innrapporterte tillegga representerer årlig lønnstillegg blir beløpa avkorta i henhold til periodas
 * årsfaktor dersom tidsperioda ikkje strekker seg over heile premieåret.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstilleggRegel implements BeregningsRegel<Kroner> {
    private static final Fastetillegg INGEN_FASTE_TILLEGG = new Fastetillegg(new Kroner(0));

    private static final Variabletillegg INGEN_VARIABLE_TILLEGG = new Variabletillegg(new Kroner(0));

    private static final Funksjonstillegg INGEN_FUNKSJONSTILLEGG = new Funksjonstillegg(new Kroner(0));

    /**
     * Reknar ut den totale summen av dei tre lønnstillegga som stillinga kan ha i den angitte perioda, kun avkorta
     * i henhold til periodas årsfaktor.
     *
     * @param periode underlagsperioda som totalt lønnstillegg skal beregnast for
     * @return underlagsperiodas bidrag til premieårets totale lønnstillegg
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        return periode.beregn(AarsfaktorRegel.class).multiply(
                fastetillegg(periode)
                        .plus(variabletillegg(periode))
                        .plus(funksjonstillegg(periode))
        );
    }

    private Kroner funksjonstillegg(final Underlagsperiode periode) {
        return periode.valgfriAnnotasjonFor(Funksjonstillegg.class).orElse(INGEN_FUNKSJONSTILLEGG).beloep();
    }

    private Kroner variabletillegg(final Underlagsperiode periode) {
        return periode.valgfriAnnotasjonFor(Variabletillegg.class).orElse(INGEN_VARIABLE_TILLEGG).beloep();
    }

    private Kroner fastetillegg(final Underlagsperiode periode) {
        return periode.valgfriAnnotasjonFor(Fastetillegg.class).orElse(INGEN_FASTE_TILLEGG).beloep();
    }
}
