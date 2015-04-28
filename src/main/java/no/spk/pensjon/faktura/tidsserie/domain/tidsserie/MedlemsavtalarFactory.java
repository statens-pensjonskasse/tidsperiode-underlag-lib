package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.Builder;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;

import java.util.List;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.AvtaleinformasjonRepository;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link MedlemsavtalarFactory} inneheld algoritma som periodiserer alle avtalekoblingane til medlemmet i
 * kombinasjon med all avtaleinformasjon tilknytta koblingane sine avtalar.
 * <p>
 * Outputen frå algoritma er delt i to:
 * <ul>
 * <li>Ei samling tidsperioder som held på informasjon om alle avtalekoblingane som eit medlem har i kvar periode</li>
 * <li>For kvar avtalekobling, ein komplett representasjon av kva som er gjeldande tilstand for koblinga sin avtale i kvar periode</li>
 * </ul>
 * <p>
 * Innanfor kvar medlemsavtalarperiode gir algoritma følgjande garantiar:
 * <ul>
 * <li>ingen av medlemmet sine stillingar byttar avtale i løpet av perioda</li>
 * <li>ingen avtaleprodukt blir fjerna eller lagt til på nokon av koblingane sine avtalar i løpet av perioda</li>
 * <li>ingen avtaleprodukt på koblingane sine avtalar endrar tilstand i løpet av perioda</li>
 * <li>avtalane sine gjeldande avtaleversjonar endrar ikkje tilstand i løpet av perioda</li>
 * </ul>
 *
 * @author Tarjei Skorgenes
 */
public class MedlemsavtalarFactory {
    private AvtaleinformasjonRepository avtalar = a -> Stream.empty();

    public MedlemsavtalarFactory overstyr(final AvtaleinformasjonRepository avtalar) {
        this.avtalar = requireNonNull(avtalar, () -> "Avtalar er påkrevd, men var null");
        return this;
    }

    /**
     * Periodiserer alle avtalekoblingane og all avtaleinformasjon tilknytta koblingane sine avtalar.
     *
     * @param koblingar avtalekoblingane som skal periodiserast i kombinasjon med all avtaleinformasjon tilknytta
     *                  koblingane sine avtalar
     * @param periode   observasjonsperioda som periodiseringa skal avgrensast til
     * @return ein straum av perioder der ingen avtalekobling eller avtaleinformasjon endrar seg innanfor nokon
     * av periodene, kun mellom periodene
     */
    public Stream<MedlemsavtalarPeriode> periodiser(
            final Stream<Avtalekoblingsperiode> koblingar, final Observasjonsperiode periode) {
        return nyttAvtaleunderlag(
                koblingar.collect(toList()),
                periode
        )
                .stream()
                .map(this::nyMedlemsavtalarPeriode);
    }

    private Underlag nyttAvtaleunderlag(
            final List<Avtalekoblingsperiode> koblingar, final Observasjonsperiode periode) {
        return new UnderlagFactory(periode)
                .addPerioder(koblingar.stream())
                .addPerioder(
                        koblingar
                                .stream()
                                .map(Avtalekoblingsperiode::avtale)
                                .flatMap(avtalar::finn)
                )
                .periodiser();
    }

    private MedlemsavtalarPeriode nyMedlemsavtalarPeriode(final Underlagsperiode periode) {
        final Builder avtalar = medlemsavtalar()
                .fraOgMed(periode.fraOgMed())
                .tilOgMed(periode.tilOgMed());
        periode.koblingarAvType(Avtalekoblingsperiode.class).forEach(kobling -> {
            avtalar.addAvtale(kobling.stillingsforhold(), lagAvtale(periode, kobling));
        });
        return avtalar.bygg();
    }

    /**
     * Orkestrerer oppbygging av ein ny representasjon av gjeldande tilstand for avtalekoblinga sin avtale innanfor
     * underlagsperioda.
     * <p>
     * Avtalen sin tilstand blir bygd opp basert på alle avtaleprodukt og avtaleversjonen som tilhøyrer avtalekoblinga
     * sin avtale og som overlappar underlagsperioda.
     *
     * @param periode underlagsperioda som inneheld informasjon om gjeldande tilstand for avtalekoblinga sin avtale
     * @param kobling avtalekoblinga som regulerer kva avtale gjeldande tilstand skal byggast opp for
     * @return gjeldande tilstand for avtalen innanfor underlagsperioda
     */
    private Avtale lagAvtale(final Underlagsperiode periode, final Avtalekoblingsperiode kobling) {
        final AvtaleId avtale = kobling.avtale();
        final AvtaleBuilder builder = avtale(avtale);
        periode.koblingAvType(Avtaleversjon.class, a -> a.tilhoeyrer(avtale))
                .ifPresent(a -> a.populer(builder));
        periode.koblingarAvType(Avtaleprodukt.class)
                .filter(a -> a.tilhoeyrer(avtale))
                .forEach(a -> a.populer(builder));
        return builder.bygg();
    }
}
