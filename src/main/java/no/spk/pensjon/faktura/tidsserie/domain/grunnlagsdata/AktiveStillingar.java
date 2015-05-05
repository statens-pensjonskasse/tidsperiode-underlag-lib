package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * {@link AktiveStillingar} representerer ei oversikt over alle aktive stillingar eit medlem har.
 * <p>
 * Sidan aktive stillingar kan endre seg over tid er det implisitt at oversikta gjeld for eit bestemt tidspunkt
 * eller ei bestemt tidsperiode der det ikkje inntreff nokon endringar på korvidt stillingane er aktive eller ei.
 * <p>
 * Merk at oversikta kun definerer ei stilling som aktiv basert på om stillinga har starta uten å bli avslutta. Det
 * impliserer at stillingar der medlemmet er ute i permisjon utan lønn, blir betrakta som aktive stillingar.
 *
 * @author Tarjei Skorgenes
 */
public interface AktiveStillingar {
    /**
     * Utledar kva stillingsforhold som er størst / har høgast stillingsprosent innanfor perioda.
     * <p>
     * Merk at vi her kun tar hensyn til stillingsforhold basert på stillingsendringar. Medregningsbaserte
     * stillingar blir ekskludert sidan dei ikkje har noko konsept om stillingsprosent.
     * <p>
     * Antagelsen her er at denne sjekken kun vil bli brukt ved seinare gruppelivsfakturering og sidan det
     * er eit forsikringsprodukt så fakturerer vi aldri dei for medregningspaserte stillingsforhold.
     * <p>
     * Dersom fleire stillingsforhold har lik stillingsprosent blir eit av dei plukka tilfeldig som det
     * største. Dette er eit bevist valg gjort av FAN basert på ein antagelse om at desse tilfella
     * er relativt få og at det er relativt uviktig kva stilling som blir fakturert for gruppeliv så lenge
     * vi kun fakturerer eit pr dag/periode.
     *
     * @return stillingsforholdet som har størst stillingsprosent i perioda,
     * eller {@link Optional#empty() ingenting} om medlemsperioda kun er tilknytta medregning
     */
    Optional<StillingsforholdId> stoersteStilling();

    /**
     * Utledar kva for eit av stillingsforholda som blir godtatt av filteret som er størst / har høgast stillingsprosent
     * innanfor perioda.
     *
     * @see #stoersteStilling()
     */
    Optional<StillingsforholdId> stoersteStilling(Predicate<StillingsforholdId> filter);

    /**
     * Alle aktive stillingar for medlemmet innanfor perioda. Dette inkluderer både
     * stillingsforhold basert på medregning og på stillingsendringar.
     *
     * @return medlemmet sine aktive stillingar i perioda
     */
    Stream<AktivStilling> stillingar();

    /**
     * AktivStilling representerer tilstanda til ei stilling som på eit gitt tidspunkt i er aktiv.
     * <p>
     * Merk at oversikta kun definerer ei stilling som aktiv basert på om det eksisterer ei stillingsforholdperiode for
     * stilling som overlappar tidspunktet aktive stillingar blir henta ut for. Det impliserer at stillingar der
     * medlemmet er ute i permisjon utan lønn, blir betrakta som aktive stillingar.
     * <p>
     * Skulle ein ønske å ignorere aktive stillingar der medlemmet er ute i permisjon utan lønn, kan
     * ein gjere det ved å filtrere på gjeldande aksjonskode for stillinga.
     *
     * @author Tarjei Skorgenes
     */
    class AktivStilling {
        private final StillingsforholdId stillingsforhold;

        private final Optional<Prosent> stillingsprosent;
        private final Optional<Aksjonskode> aksjonskode;

        private final boolean erMedregning;

        /**
         * Konstruerer ein ny instans med informasjon om ei aktiv stilling.
         *
         * @param stillingsforhold stillinga sitt stillingsforholdnummer
         * @param stillingsprosent gjeldande stillingsprosent dersom stillinga ikkje er medregningsbasert
         * @param aksjonskode      gjeldande aksjonskode dersom stillinga ikkje er medregningsbasert
         * @throws java.lang.NullPointerException  dersom nokon av parameterverdiane er <code>null</code>
         * @throws java.lang.IllegalStateException dersom stillinga ikkje er medregningsbasert og
         *                                         stillingsprosent eller aksjonskode manglar
         */
        public AktivStilling(final StillingsforholdId stillingsforhold, final Optional<Prosent> stillingsprosent,
                             final Optional<Aksjonskode> aksjonskode) {
            this.stillingsforhold = requireNonNull(stillingsforhold, "stillingsforhold er påkrevd, men var null");
            this.stillingsprosent = requireNonNull(stillingsprosent, "stillingsprosent er påkrevd, men var null");
            this.aksjonskode = requireNonNull(aksjonskode, "aksjonskode er påkrevd, men var null");
            this.erMedregning = !stillingsprosent.isPresent();

            // Begge må enten vere true eller false, alle andre kombinasjonar er feil
            if (aksjonskode.isPresent() ^ stillingsprosent.isPresent()) {
                throw new IllegalStateException(
                        "stillingsforhold som ikkje er medregningsbaserte må ha både stillingsprosent og aksjonskode\n"
                                + "Stillingsforhold: " + stillingsforhold + "\n"
                                + "Aksjonskode: " + aksjonskode + "\n"
                                + "Stillingsprosent: " + stillingsprosent
                );
            }
        }

        /**
         * Er stillinga basert på medregning?
         *
         * @return <code>true</code> dersom stillinga er medregningsbasert,
         * <code>false</code> om den er basert på stillingsendringar
         */
        public boolean erMedregning() {
            return erMedregning;
        }

        /**
         * Gjeldande stillingsprosent for stillingar basert på stillingsendringar.
         * <p>
         * For stillingar basert på {@link #erMedregning()}, vil stillingsprosent vere {@link Optional#empty() tom}.
         *
         * @return gjeldande stillingsprosent for ikkje-medregningsbaserte stillingar
         */
        public Optional<Prosent> stillingsprosent() {
            return stillingsprosent;
        }

        /**
         * Stillingsforholdnummeret som unikt identifiserer den aktive stillinga.
         *
         * @return den aktive stillinga sitt stillingsforholdnummer
         */
        public StillingsforholdId stillingsforhold() {
            return stillingsforhold;
        }

        /**
         * Gjeldande aksjonskode for stillingar basert på stillingsendringar.
         * <p>
         * For stillingar basert på {@link #erMedregning()}, vil aksjonskoda vere {@link Optional#empty() tom}.
         *
         * @return gjeldande aksjonskode for ikkje-medregningsbaserte stillingar
         */
        public Optional<Aksjonskode> aksjonskode() {
            return aksjonskode;
        }

        /**
         * Genererer ei ny aktiv stilling med stillingsprosent
         * justert til samme størrelse som <code>verdi</code>.
         * <p>
         * Dersom stillinga er medregningsbasert blir ikkje stillingsprosenten endra.
         *
         * @param verdi den nye stillingsprosenten for den aktive stillinga
         * @return ein ny instans av den aktive stillinga men med ny stillingsprosent,
         * eller <code>this</code> dersom stillinga er medregningsbasert
         */
        public AktivStilling juster(final Prosent verdi) {
            return new AktivStilling(
                    stillingsforhold(),
                    stillingsprosent()
                            .map(p -> verdi),
                    aksjonskode()
            );
        }

        @Override
        public String toString() {
            return stillingsforhold + ", aksjonskode: " + aksjonskode + ", stillingsprosent: " + stillingsprosent();
        }

        /**
         * Konstruerer ei ny medregningbasert stilling.
         *
         * @param stillingsforhold stillingsforholdnummeret for stillinga
         * @return den nye medregningsbaserte stillinga utan stillingsprosent og aksjonskode
         */
        public static AktivStilling medregning(final StillingsforholdId stillingsforhold) {
            return new AktivStilling(
                    stillingsforhold,
                    empty(),
                    empty()
            );
        }
    }
}
