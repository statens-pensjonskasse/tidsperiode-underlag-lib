package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst;
import no.spk.pensjon.faktura.tidsserie.domain.at.UnderlagsperiodeDefinisjonar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

import cucumber.api.java8.No;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Snorre E. Brekke - Computas
 */
@ContextConfiguration(classes = RegelDefinisjonar.SpringConfiguration.class)
public class RegelDefinisjonar implements No {
    @Configuration
    public static class SpringConfiguration {
    }

    @Autowired
    private UnderlagsperiodeDefinisjonar underlag;

    public RegelDefinisjonar() {
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
}
