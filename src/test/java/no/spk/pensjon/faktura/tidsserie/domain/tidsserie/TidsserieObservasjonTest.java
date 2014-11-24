package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enheitstestar for {@link TidsserieObservasjon}.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieObservasjonTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalFeileDersomEinLeggerSamanObservasjonarMedForskjelligAvtale() {
        e.expect(AssertionError.class);
        final TidsserieObservasjonBuilder builder = observasjon("2008.02.29")
                .stilling(1L)
                .maskineltgrunnlag(100_000);
        builder.avtale(1).bygg()
                .plus(
                        builder.avtale(2).bygg()
                );
    }

    @Test
    public void skalFeileDersomEinLeggerSamanObservasjonarMedForskjelligStillingsforhold() {
        e.expect(AssertionError.class);
        final TidsserieObservasjonBuilder builder = observasjon("2008.03.31")
                .avtale(1L)
                .maskineltgrunnlag(100_000);
        builder.stilling(1).bygg()
                .plus(
                        builder.stilling(2).bygg()
                );
    }

    @Test
    public void skalFeileDersomEinLeggerSamanObservasjonarMedForskjelligObservasjonsdato() {
        e.expect(AssertionError.class);
        final TidsserieObservasjonBuilder builder = observasjon("2008.06.30")
                .avtale(3L)
                .stilling(1L)
                .maskineltgrunnlag(100_000);
        builder.observasjonsdato("2008.03.31").bygg()
                .plus(
                        builder.observasjonsdato("2007.01.31").bygg()
                );
    }

    @Test
    public void skalTilhoeyreObservasjonsdatoensAarstall() {
        final TidsserieObservasjon observasjon = observasjon("2008.03.04")
                .stilling(1L)
                .avtale(2L)
                .maskineltgrunnlag(100_000)
                .bygg();
        assertTilhoeyrer(
                observasjon,
                new Aarstall(2008)
        )
                .isTrue();

        assertTilhoeyrer(
                observasjon,
                new Aarstall(2007)
        )
                .isFalse();
        assertTilhoeyrer(
                observasjon,
                new Aarstall(2009)
        )
                .isFalse();
    }

    private static TidsserieObservasjonBuilder observasjon(String observasjonsdato) {
        return new TidsserieObservasjonBuilder(dato(observasjonsdato));
    }

    private static class TidsserieObservasjonBuilder {
        private LocalDate dato;
        private long stillingsforhold;
        private long avtale;
        private Kroner maskineltgrunnlag;

        private TidsserieObservasjonBuilder(final LocalDate dato) {
            this.dato = dato;
        }

        public TidsserieObservasjonBuilder stilling(final long stillingsforhold) {
            this.stillingsforhold = stillingsforhold;
            return this;
        }

        public TidsserieObservasjonBuilder avtale(final long avtale) {
            this.avtale = avtale;
            return this;
        }

        public TidsserieObservasjonBuilder maskineltgrunnlag(final int beloep) {
            this.maskineltgrunnlag = Kroner.kroner(beloep);
            return this;
        }

        public TidsserieObservasjon bygg() {
            return new TidsserieObservasjon(
                    new StillingsforholdId(stillingsforhold),
                    new AvtaleId(avtale),
                    new Observasjonsdato(dato),
                    maskineltgrunnlag
            );
        }

        public TidsserieObservasjonBuilder observasjonsdato(final String observasjonsdato) {
            this.dato = dato(observasjonsdato);
            return this;
        }
    }

    private static AbstractBooleanAssert<?> assertTilhoeyrer(final TidsserieObservasjon observasjon, final Aarstall aarstall) {
        return assertThat(observasjon.tilhoeyrer(aarstall)).
                as("Tilhøyrer " + observasjon + ", årstall " + 2008 + "?");
    }
}