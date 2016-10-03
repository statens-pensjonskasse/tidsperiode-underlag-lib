package no.spk.felles.tidsperiode.underlag;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.felles.tidsperiode.Tidsperiode;

/**
 * Rolle-interface for tidsperiodekoblingane som ei underlagsperiode har til tidsperioidene den overlappar.
 *
 * @author Tarjei Skorgenes
 */
public interface HarKoblingar {
    /**
     * Koblar saman underlagsperioda med ei tidsperiode som overlappar underlagsperioda heilt eller delvis.
     * <p>
     * Det er både mulig og tillatt å koble opp ei underlagsperiode mot fleire tidsperioder av samme type. Brukarane av
     * av underlaget er den som skal styre korvidt fleire tilkobla perioder av samme type er funksjonelt sett tillatt
     * eller ikkje frå bruksmønster til bruksmønster.
     * <p>
     * Av ytelsesmessige årsaker verifiserer ikkje underlagsperioda at <code>kobling</code> faktisk overlappar
     * underlagsperioda, det er opp til klienten å handheve denne kontrakta.
     *
     * @param kobling ei tidsperiode som underlagsperioda skal koblast opp mot
     */
    void kobleTil(Tidsperiode<?> kobling);

    /**
     * Hentar ut koblinga underlagsperioda muligens har til ei tidsperiode av den angitte typen.
     * <p>
     * Denne metoda er primært ei hjelpemetode for å forenkle klientar som har ei forventning til at underlagsperioder
     * kun skal kunne vere tilkobla 0 eller 1 tidsperioder av den bestemte typen. I det generelle tilfellet der
     * underlagsperioder funksjonelt sett kan vere kobla til fleire perioder av samme type, må
     * {@link #koblingarAvType(Class)} brukast framfor denne metoda.
     * <p>
     * Dersom denne metoda blir brukt, antas det derfor at klienten forventar at viss underlagsperioda er kobla opp mot
     * meir enn ei periode av den angitte typen så indikerer dette dårlig datakvalitet. Alternativt at klienten er
     * feilaktig implementert. Det blir derfor kasta ein exception for å sikre at klienten blir gjort oppmerksom på
     * problemet og kan handtere dette på eit eller anna vis.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @param <T> typen tidsperiode
     * @return den eine tidsperioda av den angitte typen som underlagsperioda er tilkobla, eller eit
     * {@link java.util.Optional#empty() tomt} svar viss perioda ikkje er kobla til ei tidsperioda av den angitte typen
     * @throws IllegalStateException dersom perioda er tilkobla meir enn ei tidsperiode av den angitte typen
     */
    <T extends Tidsperiode<T>> Optional<T> koblingAvType(Class<T> type);

    /**
     * @see #koblingAvType(Class, Predicate)
     * @param predikat filter som blir køyrt på koblingane før ønska enkeltperiode blir forsøkt henta ut
     * @param type klassen for typen tidsperiode
     * @param <T> typen tidsperiode
     * @return enkel tidsperiode som matcher filter-kriteriene, dersom den finnes
     */
    <T extends Tidsperiode<T>> Optional<T> koblingAvType(Class<T> type, final Predicate<T> predikat);

    /**
     * Hentar ut alle koblingar underlagsperioda har til tidsperioder av den angitte typen.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @param <T> typen tidsperiode
     * @return ein straum som inneheld alle dei tilkobla periodene av den angitte typen
     */
    <T extends Tidsperiode<?>> Stream<T> koblingarAvType(Class<T> type);
}
