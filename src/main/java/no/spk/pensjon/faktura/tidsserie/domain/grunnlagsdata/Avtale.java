package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagFactory;

/**
 * Avtale er ein projeksjon av tilstanda til ein avtale på eit bestemt tidspunkt i tid.
 * <p>
 * Projeksjonen blir utleda basert på ei bestemt
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode periode} i ei
 * {@link UnderlagFactory#periodiser() periodisering} av avtalen sine avtaleversjonar og avtaleprodukt.
 *
 * @author Tarjei Skorgenes
 */
public class Avtale {
    private final Set<Produkt> avtaleprodukt = new HashSet<>();

    /**
     * Avtalenummeret som unikt identifiserer avtalen.
     */
    private final AvtaleId id;

    private final Premiestatus status;

    /**
     * Konstruerer ein ny avtale utan nokon produkt innlagt.
     *
     * @param id      avtalenummeret som unikt identifiserer avtalen
     * @param status  avtalen sin premiestatus
     * @param produkt produkta avtalen betalar premie til SPK for
     */
    private Avtale(final AvtaleId id, final Premiestatus status, final Stream<Produkt> produkt) {
        this.id = id;
        this.status = status;
        produkt.forEach(this.avtaleprodukt::add);
    }

    /**
     * Skal avtalen betale premie for det angitte produktet til SPK?
     *
     * @param produkt som skal sjekkes for om avtalen betaler for
     * @return <code>true</code> dersom avtalen har det angitte produktet hos SPK,
     * <code>false</code> ellers
     */
    public boolean betalarTilSPKFor(final Produkt produkt) {
        return avtaleprodukt.contains(produkt);
    }

    /**
     * Avtalenummeret som unikt identifiserer avtalen.
     *
     * @return avtalenummeret for avtalen
     */
    public AvtaleId id() {
        return id;
    }

    /**
     * Gjeldande premiestatus for avtalen.
     *
     * @return avtalens premiestatus
     */
    public Premiestatus premiestatus() {
        return status;
    }

    @Override
    public String toString() {
        return id
                + ", " + premiestatus()
                + ", "
                + avtaleprodukt.size()
                + " produkt ("
                + avtaleprodukt
                .stream()
                .map(Produkt::kode)
                .collect(joining(", "))
                + ")";
    }

    /**
     * Opprettar ein ny byggar for konstruksjon av nye {@link Avtale}-instansar.
     *
     * @param id avtalenummeret som unikt identifiserer avtalen som ein ønskjer å bygge opp tilstanda til
     * @return ein ny avtalebyggar
     * @throws NullPointerException viss <code>id</code> er <code>null</code>
     */
    public static AvtaleBuilder avtale(final AvtaleId id) {
        return new AvtaleBuilder(
                requireNonNull(id, () -> "Avtalenummer er påkrevd, men var null")
        );
    }

    /**
     * {@link AvtaleBuilder} bygger opp ein representasjon av gjeldande tilstand for ein avtale.
     *
     * @author Tarjei Skorgenes
     */
    public static class AvtaleBuilder {
        private final Set<Produkt> avtaleprodukt = new HashSet<>();

        private final AvtaleId id;

        private Premiestatus status = Premiestatus.UKJENT;

        private AvtaleBuilder(final AvtaleId id) {
            this.id = id;
        }

        /**
         * Oppdaterer premiestatusen som avtalen skal settast opp med.
         *
         * @param status premiestatusen som avtalen skal benytte
         * @return <code>this</code>
         * @throws NullPointerException viss <code>status</code> er <code>null</code>
         */
        public AvtaleBuilder premiestatus(Premiestatus status) {
            this.status = requireNonNull(status, () -> "Premiestatus er påkrevd, men vart forsøkt endra til null");
            return this;
        }

        /**
         * Legger til eit produkt som avtalen skal betale premie til SPK for.
         *
         * @param produkt eit produkt som avtalen betalar premie til SPK for
         * @return <code>this</code>
         * @throws NullPointerException viss <code>produkt</code> er <code>null</code>
         */
        public AvtaleBuilder addProdukt(final Produkt produkt) {
            this.avtaleprodukt.add(requireNonNull(produkt, () -> "Produkt er påkrevd, men var null"));
            return this;
        }

        /**
         * Bygger ein ny {@link Avtale} og populerer den basert på byggaren si tilstand.
         *
         * @return ein ny avtale
         */
        public Avtale bygg() {
            return new Avtale(
                    id,
                    status,
                    avtaleprodukt.stream()
            );
        }
    }
}
