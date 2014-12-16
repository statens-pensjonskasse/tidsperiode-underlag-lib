package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Omregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.Month;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Feilmeldingar.feilDersomPeriodaOverlapparMeirEnnEinAvtaleversjon;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StandardTidsserieAnnotering} representerer den ordinære
 * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade.Annoteringsstrategi strategien}
 * som bør brukast når ein skal annotere underlagsperioder som blir brukt til å generere ein ny tidsserie.
 *
 * @author Tarjei Skorgenes
 */
public class StandardTidsserieAnnotering implements TidsserieUnderlagFacade.Annoteringsstrategi {
    /**
     * Annoterer underlagsperioda basert på gjeldande stillingsendring viss perioda
     * er tilknytta eit stillingsforhold
     *
     * @param underlag underlaget som perioda inngår i
     * @param periode  underlagsperioda som skal populerast med annotasjonar
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#koblingarAvType(Class)
     * @see no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode#gjeldendeEndring()
     */
    @Override
    public void annoter(final Underlag underlag, final Underlagsperiode periode) {
        periode.koblingAvType(Avtalekoblingsperiode.class).ifPresent(avtalekobling -> {
            avtalekobling.annoter(periode);

            periode.koblingarAvType(Avtaleversjon.class)
                    .filter(v -> v.tilhoeyrer(avtalekobling.avtale()))
                    .reduce(feilDersomPeriodaOverlapparMeirEnnEinAvtaleversjon(avtalekobling.avtale(), periode))
                    .ifPresent(versjon -> versjon.annoter(periode));
        });
        periode.koblingAvType(StillingsforholdPeriode.class).ifPresent(stillingsforhold -> {
            stillingsforhold.gjeldendeEndring().ifPresent(endring -> {
                periode.annoter(Stillingsprosent.class, endring.stillingsprosent());
                periode.annoter(Stillingskode.class, endring.stillingskode());

                periode.annoter(DeltidsjustertLoenn.class, endring.loenn());
                periode.annoter(Fastetillegg.class, endring.fastetillegg());
                periode.annoter(Variabletillegg.class, endring.variabletillegg());
                periode.annoter(Funksjonstillegg.class, endring.funksjonstillegg());
                endring.loennstrinn().ifPresent(loennstrinn -> annoterLoennForLoennstrinn(periode, loennstrinn));
            });
            stillingsforhold.medregning().ifPresent(medregning -> {
                periode.annoter(Medregning.class, medregning.beloep());
                periode.annoter(Medregningskode.class, medregning.kode());
            });
        });
        periode.koblingAvType(Aar.class).ifPresent((Aar aar) -> {
            periode.annoter(Aarstall.class, aar.aarstall());
        });
        periode.koblingAvType(Maaned.class).ifPresent((Maaned m) -> {
            periode.annoter(Month.class, m.toMonth());
        });
        periode.koblingarAvType(Regelperiode.class).forEach(regelperiode -> {
            regelperiode.annoter(periode);
        });
        periode.koblingAvType(Omregningsperiode.class).ifPresent(omregning -> {
            omregning.annoter(periode);
        });
    }

    private void annoterLoennForLoennstrinn(final Underlagsperiode periode, final Loennstrinn loennstrinn) {
        periode.annoter(Loennstrinn.class, loennstrinn);

        final FinnLoennForLoennstrinn oppslag = new FinnLoennForLoennstrinn(
                periode,
                periode.annotasjonFor(Ordning.class)
        );
        oppslag.loennForLoennstrinn().ifPresent((LoennstrinnBeloep beloep) -> {
                    periode.annoter(LoennstrinnBeloep.class, beloep);
                }
        );
    }

}
