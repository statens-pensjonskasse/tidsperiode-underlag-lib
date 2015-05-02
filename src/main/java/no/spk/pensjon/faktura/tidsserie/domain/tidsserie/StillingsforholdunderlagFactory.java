package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.AvtaleinformasjonRepository;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsperioder;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPerioder;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aar;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link StillingsforholdunderlagFactory} inneheld algoritma som bygger opp stillingsforholdunderlag.
 * <p>
 * Algoritma g�r iterativt gjennom medlemmet sine
 * {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.StillingsforholdPerioder} og bygger eit nytt underlag
 * basert p� det, observasjonsperioda og alle referanseperioder som det skal kombinerast og periodiserast basert p�.
 * <p>
 * N�r underlaget er bygd opp blir det deretter filtrert ned til eit nytt stillingsforholdunderlag som kun inneheld
 * underlagsperioder der stillingsforholdet er aktivt.
 * <p>
 * For � unng� at ein m� holde p� underlaga for alle stillingsforholda til medlemmet i minne p� samme tid,
 * mottar klienten ein callback kvar gang eit nytt stillingsforholdunderlag er ferdig generert.
 * <h2>Annotering</h2>
 * F�r stillingsforholdunderlaget blir sendt til klienten, kan underlaget og underlagsperiodene bli annotert med
 * grunnlagsdata. Som standardoppf�rsel blir inga annotert utf�rt, klienten
 * kan {@link #endreAnnoteringsstrategi(Annoteringsstrategi) plugge inn} ein alternativ {@link Annoteringsstrategi}
 * viss det er �nskelig annotere underlagsperiodene med grunnlagsdata.
 * <h2>Avtaledata</h2>
 * Ettersom antall avtaleperioder for alle avtalar som til ei kvar tid er gyldige, er i st�rrelsesorden mange tusen
 * perioder, er det ikkje �nskelig � legge alle desse til i periodiseringa for eit enkelt stillingsforhold.
 * Derimot er det �nskelig � legge til alle avtaleperioder for alle avtalar stillingsforholdet har vore tilknytta.
 * <p>
 * Klienten kan {@link #endreAvtaleinformasjonRepository(AvtaleinformasjonRepository) plugge inn} eit lager som gir
 * tilgang til tidsperiodisert avtaleinformasjon basert p� avtalenummera stillingaforholda har vore tilknytta.
 * <p>
 * Merk at det er heilt essensielt for ytelsen til prosesseringa at denne informasjonen ikkje krever eksterne oppslage
 * som medf�rer I/O mot disk eller over nettet.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdunderlagFactory {
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
     * blir opp til callbacken � avgjere korleis desse stillingsforholda skal handterast vidare.
     * <p>
     * Kva som skal bli gjort med underlaget etter generering blir fullt og heilt bestemt av callbacken,
     * fasada gjer ingen antagelsar om kva som skjer vidare med underlaga etter at dei har blitt generert.
     * <p>
     * Dersom ein callback feilar og kastar ein exception vil fasada sluke og ignorere denne for � unng� at feil
     * som er spesifikke for eit underlag p�virkar prosesseringa av andre underlag for samme medlem. Det er derfor
     * heilt og holdent opp til callbacken og klienten � handtere alle typer feil og eventuelt sikre at dei blir
     * propagert vidare etter at fasada har fullf�rt generering av underlag for kvart stillingsforhold.
     *
     * @param medlem              alle medlemsdata for medlemmet som stillingsforholda det skal byggast opp underlag
     *                            for tilh�yrer
     * @param callback            callback som blir notifisert fortl�pande etterkvart som nye underlag blir generert
     * @param observasjonsperiode observasjonsperioda som underlagets perioder skal avgrensast til � ligge innanfor
     */
    public void prosesser(final Medlemsdata medlem, final StillingsforholdUnderlagCallback callback,
                          final Observasjonsperiode observasjonsperiode) {
        final Collection<Aar> observerbare = observasjonsperiode.overlappendeAar();
        final Medlemsperioder medlemsperioder = medlem.periodiser()
                .orElseThrow(feilmeldingForMedlemSomKunHarAvtalekobling());

        final Collection<MedlemsavtalarPeriode> avtaleperioder = new MedlemsavtalarFactory()
                .overstyr(avtalar)
                .periodiser(
                        medlem.avtalekoblingar(p -> true),
                        observasjonsperiode
                )
                .collect(toList());
        medlem
                .allePeriodiserbareStillingsforhold()
                .map(medlemsperioder::stillingsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach((StillingsforholdPerioder s) -> {
                    final UnderlagFactory factory = new UnderlagFactory(observasjonsperiode);
                    factory.addPerioder(s.stream());
                    factory.addPerioder(medlemsperioder.stream());
                    factory.addPerioder(medlem.avtalekoblingar(s::tilhoeyrer));
                    factory.addPerioder(avtaleperioder.stream());
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
                        // Callbacken er ansvarlig for � handtere egne feil, vi sluker derfor slike feil her
                        // for � unng� at dei hindrar prosessering av underlag for andre stillingsforhold enn
                        // det callbacken feila p�
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
     * Annoterer stillingsforholdunderlaget og underlagsperiodene som det inneheld med informasjon henta fr�
     * kvar underlagsperiodes koblingar.
     * <p>
     * Ansvaret for detaljane i korleis underlaget og -periodene blir annotert blir delegert til annoteringsstrategien
     * fasade er satt opp til � bruke.
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
     * n�r den bygger opp underlagsperiodene.
     * <p>
     * Kvar tidsperiode vil bli inkludert i periodiseringa og kobla til den eller dei underlagsperiodene
     * som dei overlappar.
     * <p>
     * Hovedintensjonen med dette er � st�tte oppslag av tidsperiodisert informasjon som ikkje er direkte knytta
     * til eit medlem eller stillingsforhold, f.eks. informasjon om avtale, omregningsperioder, l�nnstrinn eller
     * liknande.
     * <p>
     * Referanseperiodene blir tatt hensyn til ved periodisering av underlaget splittar og genererer nye
     * underlagsperioder som startar ved kvar referanseperiodes fr� og med dato og dagen etter kvar referanseperiodes
     * til og med-dato (for dei periodene som ikkje er l�pande).
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
     * alle innlagte regelperioders fr� og med- og til og med-datoar.
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
     * Fasadas standardstrategi er � ikkje annotere periodene den genererer, bruk denne metoda for � overstyre denne
     * oppf�rselen.
     *
     * @param strategi den nye annoteringsstrategien som skal benyttast
     * @throws NullPointerException viss <code>strategi</code> er <code>null</code>
     */
    public void endreAnnoteringsstrategi(final Annoteringsstrategi strategi) {
        this.annotator = requireNonNull(strategi, () -> "annoteringsstrategi er p�krevd, men var null");
    }

    /**
     * Endrar repository for oppslag av avtaleinformasjon.
     * <p>
     * Fasadas standardstrategi for oppslag av tidsperiodisert avtaleinformasjon er � ikkje sl� opp noko
     * ekstra informasjon om dei avtalane som kvart enkelt stillingsforhold er tilknytta via sine avtalekoblingar.
     *
     * @param repository det nye repositoriet for oppslag av tidsperiodisert avtaleinformasjon
     * @throws NullPointerException dersom <code>repository</code> er <code>null</code>
     */
    public void endreAvtaleinformasjonRepository(final AvtaleinformasjonRepository repository) {
        this.avtalar = requireNonNull(repository, () -> "avtaleinformasjonrepository er p�krevd, men var null");
    }

    private static Supplier<IllegalStateException> feilmeldingForMedlemSomKunHarAvtalekobling() {
        return () -> new IllegalStateException(
                "Eit medlem m� ha minst ei stillingsendring eller medregning, det kan ikkje kun ha avtalekoblingar"
        );
    }

    /**
     * {@link StillingsforholdunderlagFactory.Annoteringsstrategi}
     * representerer ein strategi for � annotere
     * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode underlagsperiode}.
     *
     * @author Tarjei Skorgenes
     */
    public static interface Annoteringsstrategi {
        /**
         * Populerer underlagsperioda med annotasjonar ut fr� tilstand i perioda sj�lv eller underlaget.
         *
         * @param underlag underlaget som perioda inng�r i
         * @param periode  underlagsperioda som skal populerast med annotasjonar
         * @see Underlagsperiode#annoter(Class, Object)
         */
        public void annoter(final Underlag underlag, final Underlagsperiode periode);

        /**
         * Populerer underlaget med annotasjonar ut fr� tilstand henta fr� underlagets underlagsperioder.
         * <p>
         * Annoteringa av underlaget blir utf�rt etter at alle perioder har blitt annotert via
         * {@link #annoter(Underlag, Underlagsperiode)} slik at annotasjonar kan bli henta fr� perioder og lagt p�
         * underlaget viss det er �nskelig.
         * <p>
         * Standardoppf�rsel er � ikkje legge til nokon annotasjonar p� underlaget eller p� underlagsperiodene.
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

    private static class IngenAvtaleinformasjon implements AvtaleinformasjonRepository {
        @Override
        public Stream<Tidsperiode<?>> finn(final AvtaleId avtale) {
            return Stream.empty();
        }
    }
}
