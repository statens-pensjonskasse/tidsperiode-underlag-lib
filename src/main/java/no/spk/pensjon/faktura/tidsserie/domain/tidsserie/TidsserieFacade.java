package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.StillingsforholdunderlagFactory.AvtaleinformasjonRepository;

import java.util.function.Consumer;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;

/**
 * {@link TidsserieFacade} representerer ei fasade som genererer ein ny tidsserie med observasjonar
 * basert på medlems-, avtale- og lønnsdata tilknytta eit medlem.
 * <p>
 * Første steg i genereringa av tidsseriar for medlemmet er generering av stillingsforholdunderlag for kvart
 * av stillingsforholda som medlemmet har vore aktiv på i løpet av observasjonsperioda. Kvart
 * stillingsforholdunderlag blir periodisert basert på stillingsendringar / medregning og avtalekoblingar. I
 * tillegg blir tidsperiodiserte referansedata som årsperioder, månedsperioder, lønnstrinnperioder,
 * omregningsperioder, regelperioder og avtalerelaterte periodar for stillingas tilknytt avtalar inkludert og
 * bidrar til periodiseringa av underlaget.
 * <p>
 * Outputen frå første steg blir eit stillingsforholdunderlag pr stillingsforhold. Dette underlaget er splitta
 * opp i underlagsperioder som alle strekker seg over ei periode på minimum 1 dag og maksimum 1 måned.
 * <p>
 * Andre steg i tidsseriegenereginga er å bygge opp eit årsunderlag for kvart unike år som observasjonsperioda
 * strekker seg over. Intensjonen med dette underlaget er å tilrettelegge for beregningar som opererer på årsbasis
 * og der verdiane skal summerast saman / aggregerast til ein verdi pr år. Det typiske eksempelet for denne typen
 * beregningar er maskinelt grunnlag / årslønn.
 * <p>
 * Tredje steg tar årsunderlaget og genererer opp 1-12 observasjonsunderlag basert på dette. For kvar måned i året
 * som stillingsforholdet har vore aktivt eller for kvar måned resten av året etter stillingsforholdets siste
 * arbeidsdag, blir det generert eit nytt observasjonsunderlag som inneheld alle synlige endringar fram til og med
 * siste dag i måneden observasjonsunderlaget skal observerast. I tillegg inneheld underlaget ei ny fiktiv periode
 * som strekker seg ut året og som er annotert med samme verdiar som siste synlige periode. Den fiktive perioda
 * representerer ei prognose for korleis ein på observasjonsdatoen antar at stillingsforholdet kjem til å sjå ut
 * resten av året. Dei einaste situasjonen der ei slik fiktiv periode ikkje blir generert er for desember måned
 * eller for månedar der observasjonsdato ligg etter stillingsforholdet sluttdato viss stillinga blir avslutta
 * i løpet av året.
 * <p>
 * Siste steg i tidsseriegenereringa er å fortløpande publisere observasjonsunderlaga som blir generert, til ein
 * observasjonspublikator som er ansvarlig for å prosessere underlaga og periodene dei inneheld og generere dei
 * endelige observasjonane av desse.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieFacade {
    private final StandardTidsserieAnnotering strategi = new StandardTidsserieAnnotering();

    private final ObservasjonsunderlagFactory observasjonsunderlagFactory = new ObservasjonsunderlagFactory();

    private final AarsunderlagFactory aarsunderlagFactory = new AarsunderlagFactory();

    private AvtaleinformasjonRepository repository = avtale -> Stream.empty();

    private Feilhandtering feilhandtering = (s, u, t) -> {
        System.err.println("Generering av tidsserie feila for stillingsforhold " + s.id());
        System.err.println("Observasjonsunderlag: " + u);
        t.printStackTrace(System.err);
    };

    /**
     * Overstyrer repositoriet som tidsperiodisert avtaleinformasjon blir slått opp via.
     *
     * @param repository repositoriet som avtaleinformasjon blir slått opp via
     * @throws NullPointerException dersom <code>repository</code> er <code>null</code>
     */
    public void overstyr(final AvtaleinformasjonRepository repository) {
        this.repository = requireNonNull(repository, () -> "avtaleinformasjonrepository er påkrevd, men var null");
    }

    /**
     * Overstyrer feilhandteringsstrategien til tidsserien.
     * <p>
     * Alle {@link RuntimeException} eller subtyper av denne som blir kasta under generering av observasjonar
     * for eit bestemt stillingsforhold, vil medføre at vidare prosessering av stillingsforholdet blir avbrutt
     * umiddelbart.
     * <p>
     * Kva som skal skje med feilen som avbryt prosesseringa blir handtert av feilhandteringsstrategien som ein her
     * overstyrer.
     *
     * @param feilhandtering den nye feilhandteringsstrategien som skal benyttast
     */
    public void overstyr(final Feilhandtering feilhandtering) {
        this.feilhandtering = requireNonNull(feilhandtering, () -> "Feilhandteringstrategien er påkrevd, men var null");
    }

    /**
     * Genererer observasjonsunderlag for kvart av medlemmets stillingsforhold.
     * <p>
     * Algoritma genererer eit observasjonsunderlag pr stillingsforhold pr måned pr år stillingsforholdet er aktivt.
     * <p>
     * Kvart observasjonsunderlag som genererast blir sendt vidare til <code>publikator</code>. For å unngå at den
     * påvirkar/senkar ytelsen til prosesseringa anbefalast det sterkt at den utfører vidare tung behandling eller
     * persistering av observasjonane asynkront.
     *
     * @param publikator publikatoren som straumen av observasjonsunderlag vil bli sendt vidare til,
     * den er ansvarlig for all vidare behandling/persistering av observasjonane
     * @see #generer(Medlemsdata, Observasjonsperiode, StillingsforholdUnderlagCallback, Stream)
     * @see #lagObservasjonsunderlagGenerator(Observasjonspublikator)
     */
    public void generer(final Medlemsdata medlemsdata, final Observasjonsperiode periode,
            final Observasjonspublikator publikator,
            final Stream<Tidsperiode<?>> referanseperioder) {
        generer(medlemsdata, periode, lagObservasjonsunderlagGenerator(publikator), referanseperioder);
    }

    /**
     * Genererer nye stillingsforholdunderlag for kvart stillingsforhold tilknytta medlemmet og sender dei vidare til
     * <code>callback</code> for vidare behandling.
     *
     * @param medlemsdata medlemsdata for medlemmet som skal prosesserast
     * @param periode observasjonsperioda som det skal genererast observasjonar pr siste dag i månaden for
     * @param callback callbacken som forløpande tar i mot og behandlar vidare kvart stillingsforholdunderlag
     * som blir generert for medlemmet
     * @param referanseperioder tidsperiodiserte referanseperioder som skal inkluderast i periodiseringa
     * av stillingsforholdunderlaget
     */
    public void generer(final Medlemsdata medlemsdata, final Observasjonsperiode periode,
            final StillingsforholdUnderlagCallback callback,
            final Stream<Tidsperiode<?>> referanseperioder) {
        final StillingsforholdunderlagFactory fasade = new StillingsforholdunderlagFactory();
        fasade.addReferansePerioder(referanseperioder);
        fasade.endreAnnoteringsstrategi(strategi);
        fasade.endreAvtaleinformasjonRepository(repository);
        fasade.prosesser(medlemsdata, callback, periode);
    }

    /**
     * Genererer ein ny callback som behandlar stillingsforholdunderlag og genererer
     * månedlige observasjonsunderlag utleda frå dette.
     * <p>
     * Kvart observasjonsunderlag som blir generert blir publisert via <code>publikator</code>en.
     *
     * @param publikator publikatoren mottar alle observasjonsunderlaga som blir generert av callbacken for kvart
     * stillingsforhold den prosesserer
     * @return ein ny callback som vil generere observasjonsunderlag for stillingsforholdet
     */
    public StillingsforholdUnderlagCallback lagObservasjonsunderlagGenerator(final Observasjonspublikator publikator) {
        return (stillingsforhold, underlag) -> {
            try {
                publikator.publiser(
                        aarsunderlagFactory
                                .genererUnderlagPrAar(underlag)
                                .flatMap(observasjonsunderlagFactory::genererUnderlagPrMaaned)
                );
            } catch (final RuntimeException e) {
                feilhandtering.handterFeil(stillingsforhold, underlag, e);
            }
        };
    }

    /**
     * Genererer ein ny callback som behandlar observasjonsunderlag og genererer månedlige observasjonar for kvar avtale
     * underlagets stillingsforhold har vore aktivt på i løpet av premieåret.
     * <p>
     * Dei aggregerte obserasjonane blir så sendt vidare til <code>consumer</code> for vidare behandling og/eller persistering.
     *
     * @param consumer mottakar av alle observasjonane som blir generert av aggregatoren for kvart observasjonsunderlag den prosesserer
     * @return ein ny publikator som observerer, aggregerer og videresender dei aggregerte observasjonane til <code>consumer</code>
     */
    public Observasjonspublikator lagObservasjonsaggregatorPrStillingsforholdOgAvtale(final Consumer<TidsserieObservasjon> consumer) {
        return new GenererObservasjonPrStillingsforholdOgAvtale(consumer);
    }
}
