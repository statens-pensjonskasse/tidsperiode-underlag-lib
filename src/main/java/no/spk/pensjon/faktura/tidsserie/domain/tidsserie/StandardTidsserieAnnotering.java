package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.Month;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StandardTidsserieAnnotering} representerer den ordin�re
 * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade.Annoteringsstrategi strategien}
 * som b�r brukast n�r ein skal annotere underlagsperioder som blir brukt til � generere ein ny tidsserie.
 *
 * @author Tarjei Skorgenes
 */
public class StandardTidsserieAnnotering implements TidsserieUnderlagFacade.Annoteringsstrategi {
    /**
     * Annoterer underlagsperioda basert p� gjeldande stillingsendring viss perioda
     * er tilknytta eit stillingsforhold
     *
     * @param underlag underlaget som perioda inng�r i
     * @param periode  underlagsperioda som skal populerast med annotasjonar
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#koblingarAvType(Class)
     * @see no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode#gjeldende()
     */
    @Override
    public void annoter(final Underlag underlag, final Underlagsperiode periode) {
        periode.koblingAvType(StillingsforholdPeriode.class).ifPresent(stillingsforhold -> {
            periode.annoter(Stillingsprosent.class, gjeldende(stillingsforhold).stillingsprosent());
            gjeldende(stillingsforhold).loennstrinn().ifPresent((Loennstrinn loennstrinn) -> {
                annoterLoennForLoennstrinn(periode, loennstrinn);
            });
            periode.annoter(DeltidsjustertLoenn.class, gjeldende(stillingsforhold).loenn());
        });
        periode.koblingAvType(Aar.class).ifPresent((Aar aar) -> {
            periode.annoter(Aarstall.class, aar.aarstall());
        });
        periode.koblingAvType(Maaned.class).ifPresent((Maaned m) -> {
            periode.annoter(Month.class, m.toMonth());
        });
        markerSisteUnderlagsperiodeSomSistePeriodeVissStillingsforholdetErSluttmeldt(underlag, periode);
    }

    private void annoterLoennForLoennstrinn(Underlagsperiode periode, Loennstrinn loennstrinn) {
        periode.annoter(Loennstrinn.class, loennstrinn);

        final FinnLoennForLoennstrinn oppslag = new FinnLoennForLoennstrinn(periode, loennstrinn);
        oppslag.loennForLoennstrinn().ifPresent((LoennstrinnBeloep beloep) -> {
            periode.annoter(LoennstrinnBeloep.class, beloep);
        });
    }

    private Stillingsendring gjeldende(final StillingsforholdPeriode periode) {
        return periode.gjeldende();
    }

    private void markerSisteUnderlagsperiodeSomSistePeriodeVissStillingsforholdetErSluttmeldt(
            final Underlag underlag, final Underlagsperiode periode) {
        underlag.last().filter(siste -> siste == periode).ifPresent((Underlagsperiode siste) -> {
            periode.annoter(SistePeriode.class, SistePeriode.INSTANCE);
        });
    }

}
