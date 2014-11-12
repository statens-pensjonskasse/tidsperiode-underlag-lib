package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * {@link TidsserieUnderlagFacade} tilbyr ein høg-nivå API
 * for å bygge opp {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} for bruk ved generering
 * av tidsseriar for prognosegenerering i forenkla fastsats.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieUnderlagFacade {
    private final Set<Tidsperiode> referanseperioder = new HashSet<>();

    /**
     * Bygger opp underlag for kvart unike stillingsforhold som medlemmet er eller har vore tilknytta innanfor
     * observasjonsperioda.
     * <p>
     * Fasada forventar at <code>medlemsdata</code> inneheld stillingsendringar, avtalekoblingar og medregningar
     * for eit bestemt medlem
     * <p>
     * For kvart stillingsforhold som det eksisterer informasjon om i <code>medlemsdata</code> vil det blir
     * generert eit nytt underlag som igjen blir sendt til <code>callback</code>en for vidare prosessering.
     * <p>
     * Dersom eit eller fleire av medlemmets stillingsforhold ikkje har vore aktive innanfor observasjonsperioda
     * vil det likevel bli generert underlag for desse. Dei genererte underlaga vil i desse tilfella vere tomme, det
     * blir opp til callbacken å avgjere korleis desse stillingsforholda skal handterast vidare.
     * <p>
     * Kva som skal bli gjort med underlaget etter generering blir fullt og heilt bestemt av callbacken,
     * fasada gjer ingen antagelsar om kva som skjer vidare med underlaga etter at dei har blitt generert.
     * <p>
     * Dersom ein callback feilar og kastar ein exception vil fasada sluke og ignorere denne for å unngå at feil
     * som er spesifikke for eit underlag påvirkar prosesseringa av andre underlag for samme medlem. Det er derfor
     * heilt og holdent opp til callbacken og klienten å handtere alle typer feil og eventuelt sikre at dei blir
     * propagert vidare etter at fasada har fullført generering av underlag for kvart stillingsforhold.
     *
     * @param medlem              alle medlemsdata for medlemmet som stillingsforholda det skal byggast opp underlag
     *                            for tilhøyrer
     * @param callback            callback som blir notifisert fortløpande etterkvart som nye underlag blir generert
     * @param observasjonsperiode observasjonsperioda som underlagets perioder skal avgrensast til å ligge innanfor
     */
    public void prosesser(final Medlemsdata medlem, final StillingsforholdUnderlagCallback callback,
                          final Observasjonsperiode observasjonsperiode) {
        final Collection<Aar> observerbare = observasjonsperiode.overlappendeAar();
        medlem
                .alleStillingsforholdPerioder()
                .forEach(s -> {
                    final UnderlagFactory factory = new UnderlagFactory(observasjonsperiode);
                    factory.addPerioder(s.perioder());
                    factory.addPerioder(medlem.avtalekoblingar(s::tilhoeyrer));

                    factory.addPerioder(observerbare.stream().flatMap(Aar::maaneder));
                    factory.addPerioder(observerbare);

                    factory.addPerioder(referanseperioder);

                    try {
                        callback.prosesser(
                                s.id(),
                                factory
                                        .periodiser()
                                        .restrict(p -> p.koblingAvType(StillingsforholdPeriode.class).isPresent())
                        );
                    } catch (final RuntimeException e) {
                        // Callbacken er ansvarlig for å handtere egne feil, vi sluker derfor slike feil her
                        // for å unngå at dei hindrar prosessering av underlag for andre stillingsforhold enn
                        // det callbacken feila på
                    }
                });
    }

    /**
     * Legger til ein beregningsregel som skal inkluderast ved periodisering av underlag.
     * <p>
     * Periodiseringa av alle underlag skal ta hensyn til og splitte underlagsperioder som overlappar
     * alle innlagte regelperioders frå og med- og til og med-datoar.
     *
     * @param perioder beregingsreglane og periodene dei er gjeldande for, som skal inkluderast ved periodisering
     *                 av underlag
     */
    public void addBeregningsregel(final Stream<Regelperiode> perioder) {
        perioder.forEach(p -> referanseperioder.add(p));
    }

    /**
     * @see #addBeregningsregel(java.util.stream.Stream)
     */
    public void addBeregningsregel(final Regelperiode... perioder) {
        addBeregningsregel(asList(perioder));
    }

    /**
     * @see #addBeregningsregel(java.util.stream.Stream)
     */
    public void addBeregningsregel(final Iterable<Regelperiode> perioder) {
        perioder.forEach(p -> referanseperioder.add(p));
    }

}
