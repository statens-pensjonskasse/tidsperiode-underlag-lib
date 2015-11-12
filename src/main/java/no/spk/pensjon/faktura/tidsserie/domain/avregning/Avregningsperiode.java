package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * {@link Avregningsperiode} representerer informasjon om eit utkast til avregning.
 * <br>
 * Avregningsperioda inneheld informasjon om kva premieår som blir avregna, og versjonsnummeret for avregningsutkastet
 * som har blitt generert av avregningsutkast-batchen.
 *
 * @author Tarjei Skorgenes
 * @since 1.2.0
 */
public class Avregningsperiode extends AbstractTidsperiode<Avregningsperiode> {
    private final Avregningsversjon avregningsversjon;

    private Avregningsperiode(final AvregningsperiodeBuilder builder) {
        super(builder.fraOgMed.atStartOfYear(), of(builder.tilOgMed.atEndOfYear()));
        this.avregningsversjon = requireNonNull(builder.avregningsversjon, "avregningsversjon er påkrevd, men var null");
    }

    /**
     * Konstruerer ein builder for generering av nye avregningsperioder.
     *
     * @return ein builder for avregningsperioder
     */
    public static AvregningsperiodeBuilder avregningsperiode() {
        return new AvregningsperiodeBuilder();
    }

    /**
     * Annoterer perioda med informasjon om kva avregningsversjon perioda tilhøyrer.
     *
     * @param periode underlagsperioda som avregningsversjonen skal annoterast på
     * @see Avregningsversjon
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Avregningsversjon.class, avregningsversjon);
    }

    public static class AvregningsperiodeBuilder {
        private Aarstall tilOgMed;
        private Aarstall fraOgMed;
        private Avregningsversjon avregningsversjon;

        public AvregningsperiodeBuilder fraOgMed(final Aarstall fraOgMed) {
            this.fraOgMed = requireNonNull(fraOgMed, "fraOgMed er påkrevd, men var null");
            return this;
        }

        public AvregningsperiodeBuilder tilOgMed(final Aarstall tilOgMed) {
            this.tilOgMed = requireNonNull(tilOgMed, "tilOgMed er påkrevd, men  var null");
            return this;
        }

        public Avregningsperiode bygg() {
            return new Avregningsperiode(this);
        }

        public AvregningsperiodeBuilder versjonsnummer(final Avregningsversjon avregningsversjon) {
            this.avregningsversjon = avregningsversjon;
            return this;
        }
    }
}
