package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link AktiveStillingar} representerer ei oversikt over alle aktive stillingar eit medlem har.
 * <p>
 * Sidan aktive stillingar kan endre seg over tid er det implisitt at oversikta gjeld for eit bestemt tidspunkt
 * eller ei bestemt tidsperiode der det ikkje inntreff nokon endringar p� korvidt stillingane er aktive eller ei.
 * <p>
 * Merk at oversikta kun definerer ei stilling som aktiv basert p� om stillinga har starta uten � bli avslutta. Det
 * impliserer at stillingar der medlemmet er ute i permisjon utan l�nn, blir betrakta som aktive stillingar.
 *
 * @author Tarjei Skorgenes
 */
public interface AktiveStillingar {
    /**
     * Utledar kva stillingsforhold som er st�rst / har h�gast stillingsprosent innanfor perioda.
     * <p>
     * Merk at vi her kun tar hensyn til stillingsforhold basert p� stillingsendringar. Medregningsbaserte
     * stillingar blir ekskludert sidan dei ikkje har noko konsept om stillingsprosent.
     * <p>
     * Antagelsen her er at denne sjekken kun vil bli brukt ved seinare gruppelivsfakturering og sidan det
     * er eit forsikringsprodukt s� fakturerer vi aldri dei for medregningspaserte stillingsforhold.
     * <p>
     * Dersom fleire stillingsforhold har lik stillingsprosent blir eit av dei plukka tilfeldig som det
     * st�rste. Dette er eit bevist valg gjort av FAN basert p� ein antagelse om at desse tilfella
     * er relativt f� og at det er relativt uviktig kva stilling som blir fakturert for gruppeliv s� lenge
     * vi kun fakturerer eit pr dag/periode.
     *
     * @return stillingsforholdet som har st�rst stillingsprosent i perioda,
     * eller {@link Optional#empty() ingenting} om medlemsperioda kun er tilknytta medregning
     */
    Optional<StillingsforholdId> stoersteStilling();

    /**
     * Utledar kva for eit av stillingsforholda som blir godtatt av filteret som er st�rst / har h�gast stillingsprosent
     * innanfor perioda.
     *
     * @see #stoersteStilling()
     */
    Optional<StillingsforholdId> stoersteStilling(Predicate<StillingsforholdId> filter);

    /**
     * Alle aktive stillingar for medlemmet innanfor perioda. Dette inkluderer b�de
     * stillingsforhold basert p� medregning og p� stillingsendringar.
     *
     * @return medlemmet sine aktive stillingar i perioda
     */
    Stream<AktivStilling> stillingar();

    class AktivStilling {
        private final StillingsforholdId stillingsforhold;

        private final Optional<Prosent> stillingsprosent;

        private final boolean erMedregning;

        public AktivStilling(final StillingsforholdId stillingsforhold, final Optional<Prosent> stillingsprosent) {
            this.stillingsforhold = stillingsforhold;
            this.stillingsprosent = stillingsprosent;
            this.erMedregning = !stillingsprosent.isPresent();
        }

        public boolean erMedregning() {
            return erMedregning;
        }

        public Optional<Prosent> stillingsprosent() {
            return stillingsprosent;
        }

        public StillingsforholdId stillingsforhold() {
            return stillingsforhold;
        }
    }
}
