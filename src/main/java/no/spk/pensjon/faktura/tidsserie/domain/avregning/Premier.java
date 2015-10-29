package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;

/**
 * {@link Premier} representerer eit sett med premiebeløp for arbeidsgiver, medlem og administrasjonjsgebyr.
 *
 * @author Tarjei Skorgenes
 */
public final class Premier {
    private final Premiebeloep arbeidsgiver;
    private final Premiebeloep medlem;
    private final Premiebeloep administrasjonsgebyr;

    private Premier(final PremierBuilder builder) {
        this.arbeidsgiver = builder.arbeidsgiver;
        this.medlem = builder.medlem;
        this.administrasjonsgebyr = builder.administrasjonsgebyr;
    }

    /**
     * Oppretter en ny builder som lar ein konstruere nye sett med premiebeløp.
     *
     * @return en builder for konstruksjon av nye premier
     */
    public static PremierBuilder premier() {
        return new PremierBuilder();
    }

    public Premiebeloep arbeidsgiver() {
        return arbeidsgiver;
    }

    public Premiebeloep medlem() {
        return medlem;
    }

    public Premiebeloep administrasjonsgebyr() {
        return administrasjonsgebyr;
    }

    public Premiebeloep total() {
        return arbeidsgiver()
                .plus(medlem())
                .plus(administrasjonsgebyr());
    }

    @Override
    public String toString() {
        return "Premier for arbeidsgiver: kr " + arbeidsgiver + ", medlem: kr " + medlem + " og administrasjonsgebyr: kr " + administrasjonsgebyr + ", totalpremie: kr " + total();
    }

    public static class PremierBuilder {
        private Premiebeloep arbeidsgiver = premiebeloep();
        private Premiebeloep medlem = premiebeloep();
        private Premiebeloep administrasjonsgebyr = premiebeloep();

        public PremierBuilder arbeidsgiver(final Premiebeloep beloep) {
            this.arbeidsgiver = requireNonNull(beloep, "arbeidsgiverpremie er påkrevd, men var null");
            return this;
        }

        public PremierBuilder medlem(final Premiebeloep beloep) {
            this.medlem = requireNonNull(beloep, "medlemspremie er påkrevd, men var null");
            return this;
        }

        public PremierBuilder administrasjonsgebyr(final Premiebeloep beloep) {
            this.administrasjonsgebyr = requireNonNull(beloep, "administrasjonsgebyr er påkrevd, men var null");
            return this;
        }

        /**
         * Bygger et nytt sett med premier basert på premiebeløpene builderen har blitt populert med.
         * <br>
         * Dersom et eller flere av {@link #arbeidsgiver()} , {@link #medlem()} og {@link #administrasjonsgebyr()}
         * ikke har blitt satt, blir standardverdien kr 0 benyttet for det aktuelle premiebeløpet.
         *
         * @return et nytt sett med premier
         */
        public Premier bygg() {
            return new Premier(this);
        }
    }
}
