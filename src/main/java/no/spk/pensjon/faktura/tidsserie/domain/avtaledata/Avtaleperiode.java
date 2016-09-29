package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * En tidsperiode som gjelder så lenge en avtale er gyldig.
 * @author Snorre E. Brekke - Computas
 */
public class Avtaleperiode extends AbstractTidsperiode<Avtaleperiode> implements Avtalerelatertperiode<Avtaleperiode>  {
    private final AvtaleId avtaleId;
    private final ArbeidsgiverId arbeidsgiverId;
    private final Optional<Ordning> ordning;

    /**
     * Lager en ny Avtaleperiode
     * @param fraOgMed periodens startdato
     * @param tilOgMed periodens valgfrie sluttdato
     * @param avtaleId avtaleperiodens avtaleid
     * @param arbeidsgiverId avtaleperiodens arbeidsgiverid
     * @deprecated siden 2.1.0 - benytt {@link Avtaleperiode#avtaleperiode(AvtaleId)}
     * @see Avtaleperiode#avtaleperiode(AvtaleId)
     * @see AvtaleperiodeBuilder
     */
    @Deprecated
    public Avtaleperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, AvtaleId avtaleId, ArbeidsgiverId arbeidsgiverId) {
        this(avtaleperiode(avtaleId)
                .fraOgMed(fraOgMed)
                .tilOgMed(tilOgMed)
                .arbeidsgiverId(arbeidsgiverId)
        );
    }

    /**
     * Lager en ny Avtaleperiode
     * @param builder holder på tilstanden til avtaleperioden som skal lages
     * @since 2.1.0
     */
    private Avtaleperiode(AvtaleperiodeBuilder builder) {
        super(builder.fraOgMed.get(), builder.tilOgMed);
        this.avtaleId = builder.avtale;
        this.arbeidsgiverId = builder.arbeidsgiverId.get();
        this.ordning = builder.ordning;
    }

    @Override
    public AvtaleId avtale() {
        return avtaleId;
    }

    /**
     * Er avtaleperioden tillknytta den angitte avtalen?
     *
     * @param avtale avtalenummeret for avtalen vi skal sjekke opp mot
     * @return <code>true</code> dersom avtaleperioden er tilknytta den angitte avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(AvtaleId avtale) {
        return this.avtaleId.equals(avtale);
    }

    public ArbeidsgiverId arbeidsgiverId() {
        return arbeidsgiverId;
    }

    public Optional<Ordning> ordning() {
        return ordning;
    }

    /**
     * Oppdaterer avtalebyggarens tilstand til å reflektere kva som er gjeldande ordning for avtalen.
     *
     * @param avtale avtalebyggaren som inneheld avtaletilstanda som skal oppdaterast
     */
    public void populer(final Avtale.AvtaleBuilder avtale) {
        avtale.ordning(ordning);
    }

    @Override
    public String toString() {
        return String.format("%s[%s->%s,%s,%s]", "Avtale", fraOgMed(), tilOgMed().map(LocalDate::toString).orElse(""), avtaleId, arbeidsgiverId);
    }

    /**
     * Opprettar ein ny builder for konstruksjon av nye avtaleperiode for ein bestemt avtale.
     *
     * @param avtale avtalenummer for avtalen avtalepeerioden skal tilhøyre
     * @return ein ny builder for avtaleperiode for ein bestemt avtale
     * @throws java.lang.NullPointerException viss <code>avtale</code> er <code>null</code>
     * @since 2.1.0
     */
    public static AvtaleperiodeBuilder avtaleperiode(final AvtaleId avtale) {
        return new AvtaleperiodeBuilder(requireNonNull(avtale, "avtalenummer er påkrevd, men manglar"));
    }

    /**
     * {@link AvtaleperiodeBuilder} lar ein konstruere nye avtaleperioder for ein bestemt avtale.
     *
     * @since 2.1.0
     */
    public static class AvtaleperiodeBuilder{
        private final AvtaleId avtale;

        private Optional<LocalDate> fraOgMed = empty();

        private Optional<LocalDate> tilOgMed = empty();

        private Optional<ArbeidsgiverId> arbeidsgiverId = empty();

        private Optional<Ordning> ordning = empty();

        private AvtaleperiodeBuilder(final AvtaleId avtale) {
            this.avtale = avtale;
        }

        public AvtaleperiodeBuilder fraOgMed(final LocalDate fraOgMed) {
            this.fraOgMed = ofNullable(fraOgMed);
            return this;
        }

        public AvtaleperiodeBuilder tilOgMed(final LocalDate tilOgMed) {
            this.tilOgMed = ofNullable(tilOgMed);
            return this;
        }

        public AvtaleperiodeBuilder tilOgMed(final Optional<LocalDate> tilOgMed) {
            this.tilOgMed = tilOgMed;
            return this;
        }

        public AvtaleperiodeBuilder arbeidsgiverId(final ArbeidsgiverId arbeidsgiverId) {
            this.arbeidsgiverId = ofNullable(arbeidsgiverId);
            return this;
        }

        public AvtaleperiodeBuilder ordning(final Ordning ordning) {
            this.ordning = ofNullable(ordning);
            return this;
        }

        public AvtaleperiodeBuilder ordning(final Optional<Ordning> ordning) {
            this.ordning = ordning;
            return this;
        }

        public Avtaleperiode bygg() {
            fraOgMed.orElseThrow(feltManglarVerdi("fra og med-dato"));
            arbeidsgiverId.orElseThrow(feltManglarVerdi("arbeidsgiverId"));
            return new Avtaleperiode(this);
        }

        private static Supplier<IllegalStateException> feltManglarVerdi(final String felt) {
            return () -> new IllegalStateException(felt + " er påkrevd, men manglar verdi");
        }
    }
}
