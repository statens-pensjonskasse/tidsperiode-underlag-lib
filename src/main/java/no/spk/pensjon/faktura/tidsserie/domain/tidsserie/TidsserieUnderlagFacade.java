package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * {@link TidsserieUnderlagFacade} tilbyr ein høg-nivå API
 * for å bygge opp {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag} for bruk ved generering
 * av tidsseriar for prognosegenerering i forenkla fastsats.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieUnderlagFacade {
    private final Set<Tidsperiode<?>> referanseperioder = new HashSet<>();

    private Annoteringsstrategi annotator = new IngenAnnotering();

    private AvtaleinformasjonRepository avtalar = new IngenAvtaleinformasjon();

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

                    factory.addPerioder(
                            medlem
                                    .avtalekoblingar(s::tilhoeyrer)
                                    .map(Avtalekoblingsperiode::avtale)
                                    .flatMap(this.avtalar::finn)
                    );

                    factory.addPerioder(observerbare.stream().flatMap(Aar::maaneder));
                    factory.addPerioder(observerbare);

                    factory.addPerioder(referanseperioder);

                    final Underlag underlag = genererStillingsforholdUnderlag(factory);
                    annoter(underlag.annoter(StillingsforholdId.class, s.id()));
                    try {
                        callback.prosesser(
                                s.id(),
                                underlag
                        );
                    } catch (final RuntimeException e) {
                        // Callbacken er ansvarlig for å handtere egne feil, vi sluker derfor slike feil her
                        // for å unngå at dei hindrar prosessering av underlag for andre stillingsforhold enn
                        // det callbacken feila på
                    }
                });
    }

    /**
     * Periodiserer og genererer eit nytt underlag for heile observasjonsperioda og avgrensar deretter dette til eit nytt
     * underlag som kun inneheld underlagsperioder der stillingsforholdet er aktivt.
     *
     * @param factory periodiseringsfabrikken som er populert med alle tidsperiodene som skal brukast til periodiseringa
     * @return eit nytt underlag som kun inneheld underlagsperioder tilknytta stillingsforholdet
     */
    private Underlag genererStillingsforholdUnderlag(final UnderlagFactory factory) {
        return factory
                .periodiser()
                .restrict(p -> p.koblingAvType(StillingsforholdPeriode.class).isPresent());
    }

    /**
     * Annoterer stillingsforholdunderlaget og underlagsperiodene som det inneheld med informasjon henta frå
     * kvar underlagsperiodes koblingar.
     * <p>
     * Ansvaret for detaljane i korleis underlaget og -periodene blir annotert blir delegert til annoteringsstrategien
     * fasade er satt opp til å bruke.
     *
     * @param stillingsforholdunderlag eit underlag som kun inneheld underlagsperioder tilkobla eit stillingsforhold
     * @return <code>stillingsforholdunderlag</code>
     * @see Annoteringsstrategi
     */
    Underlag annoter(final Underlag stillingsforholdunderlag) {
        annotator.annoter(stillingsforholdunderlag);
        return stillingsforholdunderlag;
    }

    /**
     * Legger til tidsperioder som inneheld globale referansedata som ein ynskjer at periodiseringa skal ta hensyn til
     * når den bygger opp underlagsperiodene.
     * <p>
     * Kvar tidsperiode vil bli inkludert i periodiseringa og kobla til den eller dei underlagsperiodene
     * som dei overlappar.
     * <p>
     * Hovedintensjonen med dette er å støtte oppslag av tidsperiodisert informasjon som ikkje er direkte knytta
     * til eit medlem eller stillingsforhold, f.eks. informasjon om avtale, omregningsperioder, lønnstrinn eller
     * liknande.
     * <p>
     * Referanseperiodene blir tatt hensyn til ved periodisering av underlaget splittar og genererer nye
     * underlagsperioder som startar ved kvar referanseperiodes frå og med dato og dagen etter kvar referanseperiodes
     * til og med-dato (for dei periodene som ikkje er løpande).
     *
     * @param perioder tidsperiodene som skal inkluderast i tillegg til medlemsdata ved periodisering av
     *                 underlag
     */
    public void addReferansePerioder(final Stream<Tidsperiode<?>> perioder) {
        perioder.forEach(referanseperioder::add);
    }

    /**
     * @see #addReferansePerioder(java.util.stream.Stream)
     */
    public void addReferansePerioder(final Iterable<Tidsperiode<?>> perioder) {
        perioder.forEach(referanseperioder::add);
    }

    /**
     * @see #addReferansePerioder(java.util.stream.Stream)
     */
    public void addReferansePerioder(final Tidsperiode<?>... perioder) {
        addReferansePerioder(Stream.of(perioder));
    }

    /**
     * Legger til ein beregningsregel som skal inkluderast ved periodisering av underlag.
     * <p>
     * Periodiseringa av alle underlag skal ta hensyn til og splitte underlagsperioder som overlappar
     * alle innlagte regelperioders frå og med- og til og med-datoar.
     *
     * @param perioder beregingsreglane og periodene dei er gjeldande for, som skal inkluderast ved periodisering
     *                 av underlag
     * @see #addReferansePerioder(java.util.stream.Stream)
     */
    public void addBeregningsregel(final Stream<Regelperiode<?>> perioder) {
        perioder.forEach(referanseperioder::add);
    }

    /**
     * @see #addBeregningsregel(java.util.stream.Stream)
     */
    public void addBeregningsregel(final Regelperiode<?>... perioder) {
        addReferansePerioder(perioder);
    }

    /**
     * @see #addBeregningsregel(java.util.stream.Stream)
     */
    public void addBeregningsregel(final Iterable<Regelperiode<?>> perioder) {
        perioder.forEach(referanseperioder::add);
    }

    /**
     * Endrar annoteringsstrategi for fasada.
     * <p>
     * Fasadas standardstrategi er å ikkje annotere periodene den genererer, bruk denne metoda for å overstyre denne
     * oppførselen.
     *
     * @param strategi den nye annoteringsstrategien som skal benyttast
     * @throws NullPointerException viss <code>strategi</code> er <code>null</code>
     */
    public void endreAnnoteringsstrategi(final Annoteringsstrategi strategi) {
        this.annotator = requireNonNull(strategi, () -> "annoteringsstrategi er påkrevd, men var null");
    }

    /**
     * Endrar repository for oppslag av avtaleinformasjon.
     * <p>
     * Fasadas standardstrategi for oppslag av tidsperiodisert avtaleinformasjon er å ikkje slå opp noko
     * ekstra informasjon om dei avtalane som kvart enkelt stillingsforhold er tilknytta via sine avtalekoblingar.
     *
     * @param repository det nye repositoriet for oppslag av tidsperiodisert avtaleinformasjon
     * @throws NullPointerException dersom <code>repository</code> er <code>null</code>
     */
    public void endreAvtaleinformasjonRepository(final AvtaleinformasjonRepository repository) {
        this.avtalar = requireNonNull(repository, () -> "avtaleinformasjonrepository er påkrevd, men var null");
    }

    /**
     * {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieUnderlagFacade.Annoteringsstrategi}
     * representerer ein strategi for å annotere
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode underlagsperiode}.
     *
     * @author Tarjei Skorgenes
     */
    public static interface Annoteringsstrategi {
        /**
         * Populerer underlagsperioda med annotasjonar ut frå tilstand i perioda sjølv eller underlaget.
         *
         * @param underlag underlaget som perioda inngår i
         * @param periode  underlagsperioda som skal populerast med annotasjonar
         * @see Underlagsperiode#annoter(Class, Object)
         */
        public void annoter(final Underlag underlag, final Underlagsperiode periode);

        /**
         * Populerer underlaget med annotasjonar ut frå tilstand henta frå underlagets underlagsperioder.
         * <p>
         * Annoteringa av underlaget blir utført etter at alle perioder har blitt annotert via
         * {@link #annoter(Underlag, Underlagsperiode)} slik at annotasjonar kan bli henta frå perioder og lagt på
         * underlaget viss det er ønskelig.
         * <p>
         * Standardoppførsel er å ikkje legge til nokon annotasjonar på underlaget eller på underlagsperiodene.
         *
         * @param underlag underlaget som skal annoterast
         */
        public default void annoter(final Underlag underlag) {
        }
    }

    /**
     * Null-object, annoterer ikkje perioda med nokon verdens ting.
     */
    private static class IngenAnnotering implements Annoteringsstrategi {
        @Override
        public void annoter(final Underlag underlag, final Underlagsperiode periode) {
        }
    }

    /**
     * {@link AvtaleinformasjonRepository} representerer eit repository for oppslag av avtalerelatert informasjon
     * som kan variere over tid.
     *
     * @author Tarjei Skorgenes
     */
    public static interface AvtaleinformasjonRepository {
        /**
         * Slår opp all tidsperiodisert informasjon som er relevant for tidsseriegenereringa for ein bestemt avtale.
         *
         * @param avtale avtalen det skal slåast opp tidsperiodisert avtaleinformasjon om
         * @return ein straum med all avtalerelatert informasjon tilknytta <code>avtale</code>
         */
        Stream<Tidsperiode<?>> finn(final AvtaleId avtale);
    }

    private static class IngenAvtaleinformasjon implements AvtaleinformasjonRepository {
        @Override
        public Stream<Tidsperiode<?>> finn(final AvtaleId avtale) {
            return Stream.empty();
        }
    }
}
