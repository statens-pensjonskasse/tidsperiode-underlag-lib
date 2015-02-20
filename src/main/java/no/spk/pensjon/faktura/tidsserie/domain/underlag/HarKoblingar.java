package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Rolle-interface for tidsperiodekoblingane som ei underlagsperiode har til tidsperioidene den overlappar.
 *
 * @author Tarjei Skorgenes
 */
interface HarKoblingar {
    /**
     * Koblar saman underlagsperioda med ei tidsperiode som overlappar underlagsperioda heilt eller delvis.
     * <p>
     * Det er b�de mulig og tillatt � koble opp ei underlagsperiode mot fleire tidsperioder av samme type. Brukarane av
     * av underlaget er den som skal styre korvidt fleire tilkobla perioder av samme type er funksjonelt sett tillatt
     * eller ikkje fr� bruksm�nster til bruksm�nster.
     * <p>
     * Av ytelsesmessige �rsaker verifiserer ikkje underlagsperioda at <code>kobling</code> faktisk overlappar
     * underlagsperioda, det er opp til klienten � handheve denne kontrakta.
     *
     * @param kobling ei tidsperiode som underlagsperioda skal koblast opp mot
     */
    void kobleTil(Tidsperiode<?> kobling);

    /**
     * Hentar ut koblinga underlagsperioda muligens har til ei tidsperiode av den angitte typen.
     * <p>
     * Denne metoda er prim�rt ei hjelpemetode for � forenkle klientar som har ei forventning til at underlagsperioder
     * kun skal kunne vere tilkobla 0 eller 1 tidsperioder av den bestemte typen. I det generelle tilfellet der
     * underlagsperioder funksjonelt sett kan vere kobla til fleire perioder av samme type, m�
     * {@link #koblingarAvType(Class)} brukast framfor denne metoda.
     * <p>
     * Dersom denne metoda blir brukt, antas det derfor at klienten forventar at viss underlagsperioda er kobla opp mot
     * meir enn ei periode av den angitte typen s� indikerer dette d�rlig datakvalitet. Alternativt at klienten er
     * feilaktig implementert. Det blir derfor kasta ein exception for � sikre at klienten blir gjort oppmerksom p�
     * problemet og kan handtere dette p� eit eller anna vis.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @return den eine tidsperioda av den angitte typen som underlagsperioda er tilkobla, eller eit
     * {@link java.util.Optional#empty() tomt} svar viss perioda ikkje er kobla til ei tidsperioda av den angitte typen
     * @throws IllegalStateException dersom perioda er tilkobla meir enn ei tidsperiode av den angitte typen
     */
    <T extends Tidsperiode<T>> Optional<T> koblingAvType(Class<T> type);

    /**
     * Hentar ut alle koblingar underlagsperioda har til tidsperioder av den angitte typen.
     * <p>
     * Dersom underlagsperioda ikkje er kobla opp til ei periode av den angitte typen er det ikkje ein feil,
     * ingen exception vil bli kasta i denne situasjonen.
     *
     * @param type datatypen for tidsperioda som underlagsperioda kan vere koble opp mot
     * @return ein straum som inneheld alle dei tilkobla periodene av den angitte typen
     */
    <T extends Tidsperiode<?>> Stream<T> koblingarAvType(Class<T> type);
}
