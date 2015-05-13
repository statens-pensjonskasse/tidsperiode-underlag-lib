package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * Medlemsavtalar er ei utleda periodetype som representerer avtaleinformasjon tilknytta eit enkelt medlem
 * sine avtalekoblingar.
 *
 * @author Tarjei Skorgenes
 * @see Medlemsavtalar
 */
public class MedlemsavtalarPeriode extends AbstractTidsperiode<MedlemsavtalarPeriode> implements Medlemsavtalar {
    private final Map<StillingsforholdId, Avtale> avtalekoblingar = new HashMap<>();

    /**
     * Opprettar ei ny tidsperiode utan nokon avtalekoblingar for medlemmet innlagt.
     *
     * @see AbstractTidsperiode#AbstractTidsperiode(LocalDate, Optional)
     */
    private MedlemsavtalarPeriode(
            final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
            final Map<StillingsforholdId, Avtale> koblingar) {
        super(fraOgMed, tilOgMed);
        this.avtalekoblingar.putAll(koblingar);
    }

    @Override
    public boolean betalarTilSPKFor(final StillingsforholdId stilling, final Produkt produkt) {
        return avtaleFor(stilling)
                .betalarTilSPKFor(produkt);
    }

    @Override
    public Avtale avtaleFor(final StillingsforholdId stilling) {
        return ofNullable(avtalekoblingar.get(stilling))
                .orElseThrow(() -> feilmeldingStillingUtanAvtalekobling(stilling));
    }

    /**
     * Annoterer underlagsperioda med informasjon om gjeldande {@link Medlemsavtalar} i perioda.
     *
     * @param periode underlagsperioda som skal bli annotert
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Medlemsavtalar.class, this);
    }

    @Override
    public String toString() {
        return "Medlemsavtalar: "
                + avtalekoblingar.size() + " stk\n"
                + avtalekoblingar.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().id()))
                .map(e -> "- " + e.getKey() + " => " + e.getValue())
                .collect(joining("\n"));
    }

    /**
     * Opprettar ein byggar som kan benyttast for å konstruere nye medlemsavtaleperioder.
     *
     * @return ein ny byggar uten nokon datoar eller avtalekoblingar/avtalar populert
     */
    public static Builder medlemsavtalar() {
        return new Builder();
    }

    private IllegalStateException feilmeldingStillingUtanAvtalekobling(final StillingsforholdId stilling) {
        return new IllegalStateException(
                stilling
                        + " kan ikkje koblast til nokon avtale i perioda "
                        + fraOgMed() + "->" + tilOgMed().map(Object::toString).orElse("")
        );
    }

    /**
     * Bygger opp tilstand for avtalene et medlem har for en bestemt periode.
     *
     * @author Tarjei Skorgenes
     * @see MedlemsavtalarPeriode
     */
    public static class Builder {
        private final Map<StillingsforholdId, Avtale> avtalekoblingar = new HashMap<>();

        private Optional<LocalDate> fraOgMed = empty();

        private Optional<LocalDate> tilOgMed = empty();

        private Builder() {
        }

        public LocalDate fraOgMed() {
            return fraOgMed.orElseThrow(() -> new IllegalStateException("Fra og med-dato er påkrevd, men er ikkje satt"));
        }

        public Builder fraOgMed(final LocalDate fraOgMed) {
            this.fraOgMed = of(requireNonNull(fraOgMed, "fra og med-dato er påkrevd, men var null"));
            return this;
        }

        public Optional<LocalDate> tilOgMed() {
            return tilOgMed;
        }

        public Builder tilOgMed(final Optional<LocalDate> tilOgMed) {
            this.tilOgMed = requireNonNull(tilOgMed, "til og med-dato er påkrevd, men var null");
            return this;
        }

        /**
         * Legger til informasjon om tilstanda til avtalen stillinga er tilknytta innanfor perioda.
         *
         * @param stilling stillinga som er kobla til avtalen innanfor perioda
         * @param avtale   informasjon om avtalens tilstand innanfor perioda
         * @return <code>this</code>
         */
        public Builder addAvtale(final StillingsforholdId stilling, final Avtale avtale) {
            avtalekoblingar.put(stilling, avtale);
            return this;
        }

        /**
         * @see AvtaleBuilder#bygg()
         * @see #addAvtale(StillingsforholdId, Avtale)
         */
        public Builder addAvtale(final StillingsforholdId stilling, final AvtaleBuilder avtale) {
            avtalekoblingar.put(stilling, avtale.bygg());
            return this;
        }

        public MedlemsavtalarPeriode bygg() {
            return new MedlemsavtalarPeriode(
                    fraOgMed(),
                    tilOgMed(),
                    avtalekoblingar
            );
        }
    }
}
