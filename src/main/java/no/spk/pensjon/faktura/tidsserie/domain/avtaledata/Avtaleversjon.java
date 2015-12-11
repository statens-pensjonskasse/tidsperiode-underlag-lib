package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * {@link Avtaleversjon} representerer tilstanden til ein avtale innanfor ei bestemt tidsperiode.
 * <p>
 * For fastsats fakturering er det kun premiestatus og premiekategori som er relevant informasjon som kan variere
 * over tid.
 *
 * @author Tarjei Skorgenes
 */
public class Avtaleversjon extends AbstractTidsperiode<Avtaleversjon> implements Avtalerelatertperiode<Avtaleversjon> {
    private final AvtaleId avtale;
    private final Premiestatus status;
    private final Optional<Premiekategori> kategori;

    private Avtaleversjon(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                          final AvtaleId avtale, final Premiestatus status, final Optional<Premiekategori> kategori) {
        super(fraOgMed, tilOgMed);
        this.avtale = requireNonNull(avtale, "avtalenummer er påkrevd, men var null");
        this.status = requireNonNull(status, "premiestatus er påkrevd, men var null");
        this.kategori = kategori;
    }

    /**
     * Annoterer underlagsperioda med gjeldande premiestatus for avtalen innanfor perioda.
     *
     * @param periode perioda som skal annoterast med premiestatus
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Premiestatus.class, status);
        periode.annoter(Premiekategori.class, kategori);
    }

    /**
     * Oppdaterer avtalebyggarens tilstand til å reflektere kva som er gjeldande premiestatus for avtalen.
     *
     * @param avtale avtalebyggaren som inneheld avtaletilstanda som skal oppdaterast
     */
    public void populer(final AvtaleBuilder avtale) {
        avtale.premiestatus(status);
        kategori.ifPresent(avtale::premiekategori);
    }

    /**
     * Avtalens avtalenummer, denne verdien kan ikkje variere over tid og vil vere den samme for alle
     * avtaleversjonar tilknytta samme avtale.
     *
     * @return avtalenummeret
     */
    public AvtaleId avtale() {
        return avtale;
    }

    /**
     * Er avtaleversjonen tillknytta den angitte avtalen?
     *
     * @param avtale avtalenummeret for avtalen vi skal sjekke opp mot
     * @return <code>true</code> dersom avtaleversjonen er tilknytta den angitte avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final AvtaleId avtale) {
        return this.avtale.equals(avtale);
    }

    @Override
    public String toString() {
        return "avtaleversjon " + fraOgMed() + "->" + tilOgMed.map(LocalDate::toString).orElse("") + " med " + status + " med hash " + Objects.hashCode(this);
    }

    /**
     * Opprettar ein ny builder for konstruksjon av nye avtaleversjonar for ein bestemt avtale.
     *
     * @param avtale avtalenummer for avtalen avtaleversjonane skal tilhøyre
     * @return ein ny builder for avtaleversjonar for ein bestemt avtale
     * @throws java.lang.NullPointerException viss <code>avtale</code> er <code>null</code>
     * @since 1.1.1
     */
    public static AvtaleversjonBuilder avtaleversjon(final AvtaleId avtale) {
        return new AvtaleversjonBuilder(requireNonNull(avtale, "avtalenummer er påkrevd, men manglar"));
    }

    /**
     * {@link AvtaleversjonBuilder} lar ein konstruere nye avtaleversjonar for ein bestemt avtale.
     *
     * @author Tarjei Skorgenes
     * @since 1.1.1
     */
    public static class AvtaleversjonBuilder {
        private final AvtaleId avtale;

        private Optional<LocalDate> fraOgMed = empty();

        private Optional<LocalDate> tilOgMed = empty();

        private Optional<Premiestatus> status = empty();

        private Optional<Premiekategori> kategori = empty();

        private AvtaleversjonBuilder(final AvtaleId avtale) {
            this.avtale = avtale;
        }

        public AvtaleversjonBuilder fraOgMed(final LocalDate fraOgMed) {
            this.fraOgMed = ofNullable(fraOgMed);
            return this;
        }

        public AvtaleversjonBuilder tilOgMed(final Optional<LocalDate> tilOgMed) {
            this.tilOgMed = tilOgMed;
            return this;
        }

        public AvtaleversjonBuilder premiestatus(final Premiestatus status) {
            this.status = ofNullable(status);
            return this;
        }

        public AvtaleversjonBuilder premiekategori(final Premiekategori kategori) {
            this.kategori = ofNullable(kategori);
            return this;
        }

        public Avtaleversjon bygg() {
            return new Avtaleversjon(
                    fraOgMed.orElseThrow(feltManglarVerdi("fra og med-dato")),
                    tilOgMed,
                    avtale,
                    status.orElseThrow(feltManglarVerdi("premiestatus")),
                    kategori
            );
        }

        private static Supplier<IllegalStateException> feltManglarVerdi(final String felt) {
            return () -> new IllegalStateException(felt + " er påkrevd, men manglar verdi");
        }
    }
}
