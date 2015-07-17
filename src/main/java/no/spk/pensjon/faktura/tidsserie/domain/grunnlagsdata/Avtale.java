package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
    private final Map<Produkt, Premiesats> premiesatser = new HashMap<>();

    private final Set<Produkt> avtaleprodukt = new HashSet<>();

    /**
     * Avtalenummeret som unikt identifiserer avtalen.
     */
    private final AvtaleId id;

    private final Premiestatus status;

    private final Optional<Premiekategori> kategori;

    /**
     * Konstruerer ein ny avtale utan nokon produkt innlagt.
     *
     * @param id      avtalenummeret som unikt identifiserer avtalen
     * @param status  avtalen sin premiestatus
     * @param kategori avtalen sin premiekategori, eller ingenting dersom premiekategori er ukjent
     * @param produkt produkta avtalen betalar premie til SPK for
     * @param satser  premiesatsane som er tilknytta avtalen, kan inkludere produkt som avtalen
     *                ikkje betalar premie  til SPK for
     */
    private Avtale(final AvtaleId id, final Premiestatus status, final Optional<Premiekategori> kategori,
                   final Stream<Produkt> produkt, final Map<Produkt, Premiesats> satser) {
        this.id = id;
        this.status = status;
        this.kategori = kategori;
        this.premiesatser.putAll(satser);
        produkt.forEach(this.avtaleprodukt::add);
    }

    /**
     * Har avtalen ein premiesats for det aktuelle produktet?
     *
     * @param produkt produktet premiesatsen skal vere tilknytta
     * @return avtalen sin premiesats for produktet,
     * eller {@link Optional#empty()} viss avtalen ikkje har det aktuelle produktet
     * @since 1.1.1
     */
    public Optional<Premiesats> premiesatsFor(final Produkt produkt) {
        return ofNullable(premiesatser.get(produkt));
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

    /**
     * Gjeldande premiekategori for avtalen.
     *
     * @return avtalens premiekategori
     * @since 1.1.1
     */
    public Optional<Premiekategori> premiekategori() {
        return kategori;
    }

    @Override
    public String toString() {
        return id
                + ", " + premiestatus()
                + "," + premiekategori()
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

        private final Map<Produkt, Premiesats> premiesatser = new HashMap<>();

        private final AvtaleId id;

        private Premiestatus status = Premiestatus.UKJENT;

        private Optional<Premiekategori> kategori = empty();

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
            this.status = requireNonNull(status, "Premiestatus er påkrevd, men vart forsøkt endra til null");
            return this;
        }

        /**
         * Oppdaterer premiekategorien som avtalen skal settast opp med.
         *
         * @param kategori premiekategorien som avtalen skal benytte
         * @return <code>this</code>
         * @since 1.1.1
         */
        public AvtaleBuilder premiekategori(final Premiekategori kategori) {
            this.kategori = ofNullable(kategori);
            return this;
        }

        /**
         * Legger til eit produkt som avtalen skal betale premie til SPK for.
         *
         * @param produkt eit produkt som avtalen betalar premie til SPK for
         * @return <code>this</code>
         * @throws NullPointerException viss <code>produkt</code> er <code>null</code>
         * @see #addPremiesats(Premiesats)
         * @deprecated
         */
        @Deprecated
        public AvtaleBuilder addProdukt(final Produkt produkt) {
            this.avtaleprodukt.add(requireNonNull(produkt, () -> "Produkt er påkrevd, men var null"));
            return this;
        }

        /**
         * Legger til en premiesats som er tilknyttet avtalen.
         *
         * @param premiesats premiesatsar som er tilknytta avtalen
         * @return <code>this</code>
         * @throws NullPointerException  viss <code>premiesats</code> er <code>null</code>
         * @throws IllegalStateException viss avtalen allereie har ein premiesats med samme produkt som <code>premiesats</code>
         * @since 1.1.1
         */
        public AvtaleBuilder addPremiesats(final Premiesats premiesats) {
            final Produkt produkt = requireNonNull(premiesats, "premiesats er påkrevd, men var null").produkt;
            if (premiesatser.containsKey(produkt)) {
                throw new IllegalStateException(
                        "Avtale "
                                + id.id()
                                + " har meir enn ein premiesats for "
                                + produkt
                                + ".\n"
                                + Stream.of(premiesatser.get(produkt), premiesats)
                                .map(s -> "- " + s)
                                .collect(joining("\n"))
                );
            }
            premiesatser.put(produkt, premiesats);
            if (premiesats.erFakturerbar()) {
                avtaleprodukt.add(premiesats.produkt);
            }
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
                    kategori,
                    avtaleprodukt.stream(),
                    premiesatser
            );
        }
    }
}
