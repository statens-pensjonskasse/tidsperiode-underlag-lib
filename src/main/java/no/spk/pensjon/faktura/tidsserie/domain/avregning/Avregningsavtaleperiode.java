package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;

/**
 * {@link Avregningsavtaleperiode} representerer informasjon om en avtale som hører til et utkast til avregning. En Avregningsversjon kan omfatter flere
 * {@ink AvtaleId}.
 * <br>
 * Avregningsavtaleperiode angir hvilket premieår som blir avregnet,en {@link AvtaleId} som gjelder i perioden, og {@link Avregningsversjon} som
 * avtalen er knyttet til.
 *
 * @author Snorre E. Brekke
 * @since 1.2.0
 */
public class Avregningsavtaleperiode extends AbstractTidsperiode<Avregningsavtaleperiode> {
    private final Avregningsversjon avregningsversjon;
    private final AvtaleId avtale;

    private Avregningsavtaleperiode(final AvregningsavtaleperiodeBuilder builder) {
        super(builder.fraOgMed.atStartOfYear(), of(builder.tilOgMed.atEndOfYear()));
        this.avregningsversjon = requireNonNull(builder.avregningsversjon, "avregningsversjon er påkrevd, men var null");
        this.avtale = requireNonNull(builder.avtale, "avregningsversjon er påkrevd, men var null");
    }

    public static AvregningsavtaleperiodeBuilder avregningsavtaleperiode() {
        return new AvregningsavtaleperiodeBuilder();
    }

    public AvtaleId avtale() {
        return avtale;
    }

    public Avregningsversjon avregningsversjon() {
        return avregningsversjon;
    }

    public static class AvregningsavtaleperiodeBuilder {
        private Aarstall tilOgMed;
        private Aarstall fraOgMed;
        private Avregningsversjon avregningsversjon;
        private AvtaleId avtale;

        public AvregningsavtaleperiodeBuilder fraOgMed(final Aarstall fraOgMed) {
            this.fraOgMed = requireNonNull(fraOgMed, "fraOgMed er påkrevd, men var null");
            return this;
        }

        public AvregningsavtaleperiodeBuilder tilOgMed(final Aarstall tilOgMed) {
            this.tilOgMed = requireNonNull(tilOgMed, "tilOgMed er påkrevd, men  var null");
            return this;
        }

        public Avregningsavtaleperiode bygg() {
            return new Avregningsavtaleperiode(this);
        }

        public AvregningsavtaleperiodeBuilder versjonsnummer(final Avregningsversjon avregningsversjon) {
            this.avregningsversjon = avregningsversjon;
            return this;
        }

        public AvregningsavtaleperiodeBuilder avtale(final AvtaleId avtale) {
            this.avtale = avtale;
            return this;
        }
    }
}
