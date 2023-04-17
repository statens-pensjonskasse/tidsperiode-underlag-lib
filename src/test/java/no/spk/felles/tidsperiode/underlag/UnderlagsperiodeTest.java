package no.spk.felles.tidsperiode.underlag;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.felles.tidsperiode.Datoar.dato;
import static no.spk.felles.tidsperiode.underlag.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

/**
 * Enheitstestar for {@link Underlagsperiode}
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeTest {
    /**
     * Verifiserer at all tilstand frå underlagsperioda blir med når ein kopi blir bygd.
     */
    @Test
    public void skalTaMedAlleAnnotasjonarTilUnderlagsperiodaEinKopiererFra() {
        final Underlagsperiode kilde = create("2000.01.01", "2014.12.31");
        kilde.annoter(Object.class, new Object());
        kilde.annoter(String.class, "Hello World!");

        assertThat(
                kilde.kopierUtenKoblinger(kilde.fraOgMed(), kilde.tilOgMed().get())
        )
                .harAnnotasjon(Object.class, kilde.annotasjonFor(Object.class))
                .harAnnotasjon(String.class, kilde.annotasjonFor(String.class));
    }

    /**
     * Verifiserer at den kopierte underlagsperioda ikkje beheld datoane til perioda kopien blir danna frå.
     */
    @Test
    public void skalIkkjeTaMedFraOgMedOgTilOgMedDatoTilUnderlagsperiodaEinKopiererFra() {
        assertThat(
                eiPeriode()
                        .kopierUtenKoblinger(
                                dato("2050.01.01"),
                                dato("2099.01.01")
                        )
        )
                .harFraOgMed("2050.01.01")
                .harTilOgMed("2099.01.01");
    }

    /**
     * Verifiserer at koblingane til kildeperioda ikkje blir kopiert over på
     * den nye periode når ein lagar ein kopi.
     */
    @Test
    public void skalIkkjeTaMedKoblingarFraUnderlagsperiodaEinKopiererFra() {
        final GenerellTidsperiode kobling = new GenerellTidsperiode(dato("1917.01.01"), empty());

        final Underlagsperiode kilde = eiPeriode();
        kilde.kobleTil(kobling);

        assertThat(
                kilde
                        .kopierUtenKoblinger(
                                dato("1900.01.01"),
                                dato("2999.01.01")
                        )
        )
                .harKoblingarAvType(GenerellTidsperiode.class, AbstractIterableAssert::isEmpty)
        ;
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    public void skalEkspandereOptionalSinVerdiVedRegistreringAvAnnotasjon() {
        final Integer expected = 1;

        final Underlagsperiode periode = eiPeriode();
        periode.annoter(Integer.class, of(expected));
        assertThat(periode.annotasjonFor(Integer.class)).isEqualTo(expected);
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    public void skalIkkjeRegistrereEinVerdiVissVerdiErEinTomOptional() {
        final Underlagsperiode periode = eiPeriode();
        periode.annoter(Integer.class, empty());
        assertThat(periode.valgfriAnnotasjonFor(Integer.class).isPresent()).isFalse();
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    public void skalFeileVissAnnotasjonstypeErOptional() {
        assertThatCode(
                () ->
                        eiPeriode()
                                .annoter(Optional.class, empty())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Annotasjonar av type Optional er ikkje støtta, viss du vil legge til ein valgfri annotasjon må den registrerast under verdiens egen type");
    }

    /**
     * Verifiserer at uthenting av periodekobling kun slår opp basert på koblinga si hovedtype,
     * ikkje basert på supertyper  eller interface som koblingstypen arvar frå eller implementerer.
     * <p>
     * Intensjonen med dette er å unngå ytelsesproblem ved oppslag av koblingar som underlagsperioda manglar
     * men der periodetypen som blir slått opp har eit djupt type-hierarki.
     */
    @Test
    public void skalKunSlaaOppKoblingarBasertPaKoblingasHovedtype() {
        final Underlagsperiode periode = eiPeriode();
        periode.kobleTil(new GenerellTidsperiode(periode.fraOgMed(), empty()));

        assertThat(periode.valgfriAnnotasjonFor(Tidsperiode.class)).isEqualTo(empty());
    }

    /**
     * Verifiserer at uthenting av periodekobling returnerer korrekt tilkobla tidsperiode.
     */
    @Test
    public void skalReturnereTidsperiodeAvDenOenskaTypen() {
        final Underlagsperiode periode = eiPeriode();

        final GenerellTidsperiode kobling = new GenerellTidsperiode(periode.fraOgMed(), empty());
        periode.kobleTil(kobling);

        assertThat(periode.koblingAvType(GenerellTidsperiode.class)).isEqualTo(of(kobling));
    }

    /**
     * Verifiserer at uthenting av periodekobling ikkje feilar når det ikkje eksisterer
     * ei tilkobla tidsperioda av den ønska typen.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void skalIkkjeFeileDersomUnderlagsperiodaIkkjeErKoblaTilMinstEiTidsperiodeAvDenOenskaTypen() {
        assertThat(eiPeriode().koblingAvType(Tidsperiode.class)).isEqualTo(empty());
    }

    /**
     * Verifiserer at uthenting av ei periodekobling feilar dersom underlagsperioda er tilkobla meir enn ei
     * tidsperiode av den ønska typen.
     */
    @Test
    public void skalFeileDersomUnderlagsperiodaErTilkoblaMeirEnnEiTidsperiodeAvDenOenskaTypen() {
        assertThatCode(
                () -> {
                    final Underlagsperiode periode = eiPeriode();
                    periode.kobleTil(new GenerellTidsperiode(periode.fraOgMed(), of(periode.fraOgMed().plusMonths(1).minusDays(1))));
                    periode.kobleTil(new GenerellTidsperiode(periode.fraOgMed().plusMonths(1), empty()));
                    periode.koblingAvType(GenerellTidsperiode.class);
                }
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Underlagsperioda er kobla til meir enn ei tidsperiode av type")
                .hasMessageContaining(GenerellTidsperiode.class.getSimpleName())
                .hasMessageContaining("vi forventa berre 1 kobling av denne typen")
        ;
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar ikkje feilar dersom perioda ikkje har ein verdi for
     * annotasjonen.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonSomIkkjeEksistererPaaPerioda() {
        assertThat(
                eiPeriode().valgfriAnnotasjonFor(Object.class)
        )
                .isEqualTo(empty());
    }

    /**
     * Verifiserer at oppslag av valgfri annotasjon fungerer når perioda har ein verdi for annotasjonen.
     */
    @Test
    public void skalKunneHenteUtVerdiarForValgfrieAnnotasjonar() {
        final Object verdi = "valgfrie annotasjonar fungerer fint";

        final Underlagsperiode periode = eiPeriode();
        periode.annoter(Object.class, verdi);

        assertThat(periode.valgfriAnnotasjonFor(Object.class)).isEqualTo(of(verdi));
    }

    /**
     * Verifiserer at oppslag av påkrevde annotasjonar via
     * {@link Underlagsperiode#annotasjonFor(Class)} feilar med
     * ein exception dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    public void skalFeileVedOppslagAvPaakrevdAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        final Underlagsperiode periode = create("2005.01.01", "2005.12.31");

        assertThatCode(
                () -> periode.annotasjonFor(Integer.class)
        )
                .isInstanceOf(PaakrevdAnnotasjonManglarException.class)
                .hasMessageContaining(periode.toString())
                .hasMessageContaining("manglar ein påkrevd annotasjon av type")
                .hasMessageContaining(Integer.class.getSimpleName())
        ;
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar via
     * {@link Underlagsperiode#valgfriAnnotasjonFor(Class)} ikkje
     * feilar og returnerer ein tom verdi dersom perioda ikkje er annotert med den ønska typen annotasjon.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonVissPeriodeIkkjeHarBlittAnnotertMedDenAktuelleTypen() {
        assertThat(create("2005.02.02", "2005.03.03").valgfriAnnotasjonFor(Long.class)).isEqualTo(empty());
    }

    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedFraOgMedDatoLikNull() {
        assertThatCode(
                () -> new Underlagsperiode(null, dato("2007.12.31"))
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("fra og med-dato er påkrevd")
                .hasMessageContaining("var null")
        ;
    }

    @Test
    public void skal_kunne_kalle_regler_rekursivt_uten_concurrentModificationException_på_cache() {
        String expected = "testverdi";
        assertBeregn(
                eiPeriode()
                        .annoter(FoersteRegel.class, new FoersteRegel())
                        .annoter(AndreRegel.class, new AndreRegel(expected)),
                FoersteRegel.class
        )
                .isEqualTo(expected);
    }

    @Test
    public void skal_aldri_kalle_en_regel_mer_enn_en_gang_per_periode_fordi_regelene_er_cachet_og_idempotente() {
        final Underlagsperiode periode = eiPeriode();

        final TredjeRegel regel = new TredjeRegel();
        periode.annoter(TredjeRegel.class, regel);

        assertBeregn(periode, TredjeRegel.class).isEqualTo(1);
        assertBeregn(periode, TredjeRegel.class).isEqualTo(1);

        regel.assertTeller().isEqualTo(1);
    }

    private <T> AbstractObjectAssert<?, T> assertBeregn(Underlagsperiode periode, Class<? extends BeregningsRegel<T>> regelType) {
        return assertThat(
                periode.beregn(regelType)
        )
                .as("Resultat av å kalle %s på %s", regelType.getSimpleName(), periode);
    }

    private Underlagsperiode create(final String fra, final String til) {
        return new Underlagsperiode(dato(fra), dato(til));
    }

    private Underlagsperiode eiPeriode() {
        return create("2007.01.01", "2007.12.31");
    }

    private static class FoersteRegel implements BeregningsRegel<String> {


        @Override
        public String beregn(Beregningsperiode<?> periode) {
            return periode.beregn(AndreRegel.class);
        }
    }

    private static class AndreRegel implements BeregningsRegel<String> {

        private final String testverdi;

        public AndreRegel(String testverdi) {

            this.testverdi = testverdi;
        }

        @Override
        public String beregn(Beregningsperiode<?> periode) {
            return testverdi;
        }
    }

    private static class TredjeRegel implements BeregningsRegel<Integer> {
        private int teller;

        @Override
        public Integer beregn(Beregningsperiode<?> periode) {
            return ++teller;
        }

        AbstractIntegerAssert<?> assertTeller() {
            return assertThat(teller).as("antall ganger regelen ble kallet av underlagsperioden");
        }
    }
}