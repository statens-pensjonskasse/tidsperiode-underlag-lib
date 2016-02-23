package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst;
import no.spk.pensjon.faktura.tidsserie.domain.at.UnderlagsperiodeDefinisjonar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import cucumber.api.java8.No;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Snorre E. Brekke - Computas
 */
@ContextConfiguration(classes = RegelConfiguration.class)
public class RegelDefinisjonar implements No {

    @Autowired
    private UnderlagsperiodeDefinisjonar underlag;

    final ErrorDetector detector = new ErrorDetector();

    public RegelDefinisjonar() {

        Når("^beregning av regel 'er under minstegrense' er utført$", () -> {
            detector.utfoer(() -> beregn(ErUnderMinstegrensaRegel.class));
        });


        Når("^verdi for ordning hentes for perioden$", () -> {
            detector.utfoer(() -> underlag.builder().bygg().valgfriAnnotasjonFor(Ordning.class));
        });

        Så("^skal pensjonsgivende lønn for perioden være (.+)$", (String verdi) -> {
            final Kroner pensjonsgivendeLoenn = KonverterFraTekst.beloep(verdi);
            assertThat(
                    beregn(MaskineltGrunnlagRegel.class)
            )
                    .isEqualTo(pensjonsgivendeLoenn);
        });

        Så("^skal regel deltidsjustert lønn for perioden være (.+)$", (String verdi) -> {
            final Kroner pensjonsgivendeLoenn = KonverterFraTekst.beloep(verdi);
            assertThat(
                    beregn(DeltidsjustertLoennRegel.class)
            )
                    .isEqualTo(pensjonsgivendeLoenn);
        });

        Så("^skal øvre lønnsgrense for perioden være (.+)$", (String verdi) -> {
            final Kroner grense = KonverterFraTekst.beloep(verdi);
            assertThat(
                    beregn(OevreLoennsgrenseRegel.class)
            )
                    .isEqualTo(grense);
        });

        Så("^skal regel lønnstilegg for perioden være (.+)$", (String verdi) -> {
            final Kroner grense = KonverterFraTekst.beloep(verdi);
            assertThat(
                    beregn(LoennstilleggRegel.class)
            )
                    .isEqualTo(grense);
        });

        Så("^skal antall feil for perioden være (\\d+)$", (Integer antallFeil) -> {
            assertThat(detector.antallFeil).isEqualTo(antallFeil);
        });

    }

    private <R, T extends BeregningsRegel<R>> R beregn(Class<T> regel) {
        return underlag.builder()
                .med(instans(regel))
                .bygg()
                .beregn(regel);
    }

    private <T extends BeregningsRegel<?>> T instans(Class<T> regel) {
        try {
            return regel.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    class ErrorDetector {
        int antallFeil;

        ErrorDetector utfoer(final Runnable runnable) {
            try {
                runnable.run();
            } catch (final Exception e) {
                antallFeil++;
            }
            return this;
        }
    }

}
