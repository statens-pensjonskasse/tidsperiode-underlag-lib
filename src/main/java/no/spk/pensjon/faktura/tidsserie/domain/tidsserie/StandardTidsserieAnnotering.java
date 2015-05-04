package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Omregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Maaned;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.time.Month;
import java.util.Optional;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Feilmeldingar.feilDersomPeriodaOverlapparMeirEnnEinAvtaleversjon;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StandardTidsserieAnnotering} representerer den ordinære
 * {@link StillingsforholdunderlagFactory.Annoteringsstrategi strategien}
 * som bør brukast når ein skal annotere underlagsperioder som blir brukt til å generere ein ny tidsserie.
 *
 * @author Tarjei Skorgenes
 */
public class StandardTidsserieAnnotering implements StillingsforholdunderlagFactory.Annoteringsstrategi {
    /**
     * Populerer underlaget og underlagets underlagsperioder med annotasjonar.
     * <p>
     * Kvar underlagsperiode blir først annotert via {@link #annoter(Underlag, Underlagsperiode)}.
     * <p>
     * Deretter  blir siste underlagsperiode blir annotert med {@link SistePeriode} slik at seinare prosesseringa
     * kjapt skal kunne sjekke opp om det eksisterer nokon fleire underlagsperioder i underlaget.
     * <p>
     * Dersom siste underlagsperiode er annotert med premiestatus blir underlaget annotert med denne heilt til slutt.
     *
     * @param underlag underlaget som skal annoterast
     */
    @Override
    public void annoter(final Underlag underlag) {
        underlag.stream().forEach((Underlagsperiode periode) -> annoter(underlag, periode));
        final Optional<Underlagsperiode> sistePeriode = underlag.last();
        sistePeriode.ifPresent(periode -> {
            periode.annoter(SistePeriode.class, SistePeriode.INSTANCE);
            periode.valgfriAnnotasjonFor(Premiestatus.class)
                    .ifPresent(premiestatus -> underlag.annoter(Premiestatus.class, premiestatus));
        });
    }

    /**
     * Annoterer underlagsperioda basert på gjeldande stillingsendring viss perioda
     * er tilknytta eit stillingsforhold
     *
     * @param underlag underlaget som perioda inngår i
     * @param periode  underlagsperioda som skal populerast med annotasjonar
     * @see Underlagsperiode#koblingarAvType(Class)
     * @see StillingsforholdPeriode#annoter(no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void annoter(final Underlag underlag, final Underlagsperiode periode) {
        periode.koblingAvType(Avtalekoblingsperiode.class).ifPresent(avtalekobling -> {
            avtalekobling.annoter(periode);

            periode.koblingarAvType(Avtaleversjon.class)
                    .filter(v -> v.tilhoeyrer(avtalekobling.avtale()))
                    .reduce(feilDersomPeriodaOverlapparMeirEnnEinAvtaleversjon(avtalekobling.avtale(), periode))
                    .ifPresent(versjon -> versjon.annoter(periode));
        });
        periode.koblingAvType(StillingsforholdPeriode.class).ifPresent(stillingsforhold -> {
            stillingsforhold.annoter(periode);
        });
        periode.koblingAvType(Medlemsperiode.class).ifPresent(medlem -> {
            medlem.annoter(periode);
        });
        periode.koblingAvType(MedlemsavtalarPeriode.class).ifPresent(p -> {
            p.annoter(periode);
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
        periode.valgfriAnnotasjonFor(Loennstrinn.class).ifPresent(loennstrinn -> {
            annoterLoennForLoennstrinn(periode);
        });
    }

    private void annoterLoennForLoennstrinn(final Underlagsperiode periode) {
        new FinnLoennForLoennstrinn(periode)
                .loennForLoennstrinn()
                .ifPresent(beloep -> periode.annoter(LoennstrinnBeloep.class, beloep));
    }

}
