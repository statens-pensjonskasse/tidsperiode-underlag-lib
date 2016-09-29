package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.GRU_35;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.GRU_36;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.YSK_79;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produktinfo.erEnAv;

import java.util.Optional;

/**
 * Premiesatser for et produkt.
 * <br>
 * På grunn av måten premiesatser er modellert i Kasper kan en premiesats indikere at avtalens forhold til produktet
 * er i en av to mulige tilstander:
 * <ul>
 * <li>Avtalen har produktet skal kunne bli fakturert for produktet.</li>
 * <li>Avtalen har ikke produktet og skal ikke kunne bli fakturert for produktet</li>
 * </ul>
 * Hvilken tilstand avtalens forhold til produktet er i, styres via premiesatsens produktinformasjon.
 *
 * @author Tarjei Skorgenes
 * @since 1.1.1
 */
public class Premiesats {
    /**
     * Produktet premiesatsene gjelder for.
     */
    public final Produkt produkt;

    /**
     * Kode som regulerer om produktet er fakturerbart eller ikke.
     */
    public final Produktinfo produktinfo;

    /**
     * Gjeldende premiesatser, angitt som {@link Prosent prosentsatser} eller {@link Kroner kronebeløp}.
     */
    public final Satser<?> satser;

    private Premiesats(final Produkt produkt, final Produktinfo produktinfo, final Satser<?> satser) {
        this.produkt = requireNonNull(produkt, "produkt er påkrevd, men var null");
        this.produktinfo = requireNonNull(produktinfo, "produktinfo er påkrevd, men var null");
        this.satser = requireNonNull(satser, "satsar er påkrevd, men var null");
    }

    /**
     * Er produktet fakturerbart?
     * <br>
     * Hvorvidt en premiesats er fakturerbar reguleres via produktinformasjonen for premiesatsen. Merk
     * at visse koder indikerer at premiesatsen er en negativ verdi, det vil si at avtalen ikke har
     * produktet selv om det er lagt inn en premiesats.
     *
     * @return <code>true</code> dersom premiesatsen kan faktureres for produktet, <code>false</code> viss ingen
     * fakturering er tillatt for det aktuelle produktets premiesats
     */
    public boolean erFakturerbar() {
        if (!satser.somKroner().isPresent() && !satser.somProsent().isPresent()) {
            return false;
        }

        if (produkt.equals(Produkt.GRU)) {
            return erEnAv(produktinfo, GRU_35, GRU_36);
        }
        if (produkt.equals(Produkt.YSK)) {
            return !erEnAv(produktinfo, YSK_79);
        }
        return true;
    }

    @Override
    public String toString() {
        return "produkt=" + produkt.kode() + ", produktinfo=" + produktinfo.toString() + ", satser=" + satser;
    }

    /**
     * Konstruerer ein ny builder for konstruksjon av nye premiesatsar tilknytta eit bestemt produkt.
     *
     * @param produkt produktet premiesatsane som builderen bygger skal vere tilkobla
     * @return ein ny builder for premiesatsar tilknytta det angitte produktet
     */
    public static Builder premiesats(Produkt produkt) {
        return new Builder(produkt);
    }

    /**
     * Hentar ut prosentverdiane for premiesatsane.
     *
     * @return premiesatsane sine satsar angitt i prosent,
     * eller {@link Optional#empty()} dersom produktet benyttar kronebeløp og ikkje prosentsatsar.
     * @since 1.1.1
     */
    public Optional<Satser<Prosent>> prosentsatser() {
        return satser.somProsent();
    }

    /**
     * Hentar ut kroneverdiane for premiesatsane.
     *
     * @return premiesatsane sine satsar angitt som kronebeløp,
     * eller {@link Optional#empty()} dersom produktet benyttar prosentsatsar og ikkje kronebeløp
     * @since 1.1.1
     */
    public Optional<Satser<Kroner>> beloepsatsar() {
        return satser.somKroner();
    }

    /**
     * {@link Builder} konstruerer nye premiesatsar tilknytta eit bestemt produkt.
     *
     * @author Tarjei Skorgenes
     */
    public static class Builder {
        private Optional<Produktinfo> produktinfo = empty();

        private Optional<Satser<?>> satser = empty();

        private final Produkt produkt;

        private Builder(final Produkt produkt) {
            this.produkt = produkt;
        }

        private Builder(final Builder other) {
            this(other.produkt);
            produktinfo = other.produktinfo;
            satser = other.satser;
        }

        public Builder kopi() {
            return new Builder(this);
        }

        public Premiesats bygg() {
            return new Premiesats(
                    produkt,
                    produktinfo.orElseThrow(this::produktinfoManglar),
                    satser.orElseThrow(this::satserMangler)
            );
        }

        public Builder produktinfo(final Produktinfo produktinfo) {
            this.produktinfo = ofNullable(produktinfo);
            return this;
        }

        public Builder satser(Satser<?> satser) {
            this.satser = ofNullable(satser);
            return this;
        }

        private RuntimeException satserMangler() {
            return new IllegalStateException("satser er påkrevd, men mangler verdi");
        }

        private RuntimeException produktinfoManglar() {
            return new IllegalStateException("produktinfo er påkrevd, men mangler verdi");
        }
    }
}
