package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Enheitstestar for {@link TidsserieObservasjon}.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieObservasjonTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKombinereAarsverkVedSamanslaaingAvToObservasjonar() {
        final TidsserieObservasjon a = einObservasjon().bygg()
                .registrerMaaling(Aarsverk.class, new Aarsverk(prosent("10%")));
        final TidsserieObservasjon b = einObservasjon().bygg()
                .registrerMaaling(Aarsverk.class, new Aarsverk(prosent("10%")));

        assertThat(a.plus(b).maaling(Aarsverk.class).get().tilProsent().toDouble())
                .isEqualTo(prosent("20%").toDouble(), offset(0.0001));
    }

    @Test
    public void skalTaVarePaaRegistrerteMaalingar() {
        final TidsserieObservasjon observasjon = einObservasjon()
                .bygg();

        final Integer expected = 123;
        observasjon.registrerMaaling(Integer.class, expected);
        assertThat(observasjon.maaling(Integer.class)).isEqualTo(of(expected));
    }

    @Test
    public void skalTaVarePaaSistRegistrerteMaalingar() {
        final TidsserieObservasjon observasjon = einObservasjon()
                .bygg();

        final Integer expected = 123;
        observasjon.registrerMaaling(Integer.class, 2);
        observasjon.registrerMaaling(Integer.class, expected);
        assertThat(observasjon.maaling(Integer.class)).isEqualTo(of(expected));
    }

    @Test
    public void skalTaVarePaaVerdiBasertPaaAngittTypeIkkjeVerdiensKlasse() {
        final TidsserieObservasjon o = einObservasjon().bygg().registrerMaaling(Object.class, "yada yada");
        assertThat(o.maaling(String.class)).isEqualTo(empty());
        assertThat(o.maaling(Object.class)).isEqualTo(of("yada yada"));
    }

    @Test
    public void skalFeileDersomEinLeggerSamanObservasjonarMedForskjelligAvtale() {
        e.expect(IllegalArgumentException.class);
        e.expectMessage("tilhøyrer forskjellige avtalar");
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
        e.expect(IllegalArgumentException.class);
        e.expectMessage("tilhøyrer forskjellige stillingsforhold");
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
        e.expect(IllegalArgumentException.class);
        e.expectMessage("tilhøyrer forskjellige observasjonsdatoar");
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

    private static TidsserieObservasjonBuilder observasjon(final String observasjonsdato) {
        return new TidsserieObservasjonBuilder(dato(observasjonsdato));
    }

    private static class TidsserieObservasjonBuilder {
        private LocalDate dato;
        private Long stillingsforhold;
        private Long avtale;
        private Kroner maskineltgrunnlag;
        private Optional<Premiestatus> premiestatus = empty();

        private TidsserieObservasjonBuilder(final LocalDate dato) {
            this.dato = dato;
        }

        private TidsserieObservasjonBuilder() {
        }

        public TidsserieObservasjonBuilder stilling(final long stillingsforhold) {
            this.stillingsforhold = stillingsforhold;
            return this;
        }

        public TidsserieObservasjonBuilder avtale(final long avtale) {
            this.avtale = avtale;
            return this;
        }

        public TidsserieObservasjonBuilder premiestatus(final String kode) {
            this.premiestatus = of(Premiestatus.valueOf(kode));
            return this;
        }

        public TidsserieObservasjonBuilder maskineltgrunnlag(final int beloep) {
            this.maskineltgrunnlag = Kroner.kroner(beloep);
            return this;
        }

        public TidsserieObservasjon bygg() {
            requireNonNull(dato, "observasjonsdato er påkrevd, men var null");
            requireNonNull(stillingsforhold, "stillingsforhold er påkrevd, men var null");
            requireNonNull(avtale, "avtale er påkrevd, men var null");
            requireNonNull(maskineltgrunnlag, "maskinelt grunnlag er påkrevd, men var null");
            return new TidsserieObservasjon(
                    new StillingsforholdId(stillingsforhold),
                    new AvtaleId(avtale),
                    new Observasjonsdato(dato),
                    maskineltgrunnlag,
                    premiestatus
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


    /**
     * Ein tilfeldig observasjon for testar som ikkje er interessert i stillingsforhold, avtale, observasjonsdato
     * eller maskinelt grunnlag.
     */
    private static TidsserieObservasjonBuilder einObservasjon() {
        return observasjon("2005.08.31")
                .stilling(1)
                .avtale(2)
                .maskineltgrunnlag(0);
    }
}