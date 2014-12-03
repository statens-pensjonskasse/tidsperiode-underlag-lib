package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.internal.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link Regelperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class RegelperiodeTest {
    /**
     * Verifiserer at annotasjonstypen er lik beregningsregelens klasse ved annotering av underlagsperioder.
     */
    @Test
    public void skalBrukeRegelinstansensTypeSomAnnotasjonstypeVedAnnoteringAvUnderlagsperiode() {
        final BeregningsRegel<?> regel = new MaskineltGrunnlagRegel();

        final Underlagsperiode periode = new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2006.08.01"))
                .tilOgMed(dato("2006.08.31"))
                .bygg();
        new Regelperiode<>(
                dato("1989.01.01"),
                empty(),
                regel
        ).annoter(periode);

        assertThat(periode.valgfriAnnotasjonFor(MaskineltGrunnlagRegel.class))
                .isEqualTo(of(regel));
        assertThat(periode.valgfriAnnotasjonFor(BeregningsRegel.class))
                .isEqualTo(empty());
    }
}