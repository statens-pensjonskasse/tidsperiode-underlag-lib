package no.spk.felles.tidsperiode.underlag;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.felles.tidsperiode.underlag.Assertions.assertUUID;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import no.spk.felles.tidsperiode.GenerellTidsperiode;
import no.spk.felles.tidsperiode.Tidsperiode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link Underlagsperiode}
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at kvar underlagsperiodeinstans får sin eigen unike id.
     * <br>
     * Også ved kopiering av perioder skal den nye perioda få ein identifikator som skiller den frå perioda den er
     * kopiert frå.
     * <br>
     * Derimot skal det ikkje genererast meir enn ein unik identifikator ved forsøk på uthenting av id frå
     * samme periodeinstans.
     */
    @Test
    public void skalGenerereUnikIdentifikatorForAllePerioder() {
        final Underlagsperiode periode = new Underlagsperiode(dato("2000.01.01"), dato("2000.12.31"));
        assertUUID(periode).isEqualTo(periode.id());
        assertUUID(periode).isNotEqualTo(new Underlagsperiode(dato("2000.01.01"), dato("2000.12.31")).id());
        assertUUID(periode).isNotEqualTo(periode.kopierUtenKoblinger(dato("2000.01.01"), dato("2000.12.31")).id());

    }

    /**
     * Verifiserer at all tilstand fr<E5> underlagsperioda blir med n<E5>r ein kopi blir bygd.
     */
    @Test
    public void skalTaMedAlleAnnotasjonarTilUnderlagsperiodaEinKopiererFra() {
        final Underlagsperiode kilde = create("2000.01.01", "2014.12.31");
        kilde.annoter(Object.class, new Object());
        kilde.annoter(String.class, "Hello World!");

        final Underlagsperiode kopi = kilde.kopierUtenKoblinger(kilde.fraOgMed(), kilde.tilOgMed().get());
        assertThat(kopi.annotasjonFor(Object.class)).isEqualTo(kilde.annotasjonFor(Object.class));
        assertThat(kopi.annotasjonFor(String.class)).isEqualTo(kilde.annotasjonFor(String.class));
    }

    /**
     * Verifiserer at den kopierte underlagsperioda ikkje beheld datoane til perioda kopien blir danna frå.
     */
    @Test
    public void skalIkkjeTaMedFraOgMedOgTilOgMedDatoTilUnderlagsperiodaEinKopiererFra() {
        final Underlagsperiode kilde = eiPeriode();
        final Underlagsperiode kopi = kilde.kopierUtenKoblinger(dato("2050.01.01"), dato("2099.01.01"));
        assertThat(kopi.fraOgMed()).as("fra og med-dato for periodekopi")
                .isNotEqualTo(kilde.fraOgMed()).isEqualTo(dato("2050.01.01"));
        assertThat(kopi.tilOgMed()).as("til og med-dato for periodekopi")
                .isNotEqualTo(kilde.tilOgMed()).isEqualTo(of(dato("2099.01.01")));
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

        final Underlagsperiode kopi = kilde.kopierUtenKoblinger(dato("1900.01.01"), dato("2999.01.01"));
        assertThat(kopi.koblingAvType(GenerellTidsperiode.class))
                .as("kobling av type GenerellTidsperiode på den kopierte perioda")
                .isEqualTo(empty());
    }

    /**
     * Verifiserer at {@link java.util.Optional} blir spesialhandtert ved registrering av annotasjonar,
     * ei perioda skal enten kunne ha ein verdi eller ikkje ha den (Optional&lt;Verdi&gt;), den skal ikkje kanskje
     * kunne ha verdi som den kanskje har {Optional&lt;Optional&lt;Verdi&gt;&gt;).
     */
    @Test
    public void skalEkspandereOptionalSinVerdiVedRegistreringAvAnnotasjon() {
        final Integer expected = Integer.valueOf(1);

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
        e.expect(IllegalArgumentException.class);
        e.expectMessage("Annotasjonar av type Optional er ikkje støtta, viss du vil legge til ein valgfri annotasjon må den registrerast under verdiens egen type");
        eiPeriode().annoter(Optional.class, empty());
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
        e.expect(IllegalStateException.class);
        e.expectMessage("Underlagsperioda er kobla til meir enn ei tidsperiode av type");
        e.expectMessage(GenerellTidsperiode.class.getSimpleName());
        e.expectMessage("vi forventa berre 1 kobling av denne typen");

        final Underlagsperiode periode = eiPeriode();
        periode.kobleTil(new GenerellTidsperiode(periode.fraOgMed(), of(periode.fraOgMed().plusMonths(1).minusDays(1))));
        periode.kobleTil(new GenerellTidsperiode(periode.fraOgMed().plusMonths(1), empty()));
        periode.koblingAvType(GenerellTidsperiode.class);
    }

    /**
     * Verifiserer at oppslag av valgfrie annotasjonar ikkje feilar dersom perioda ikkje har ein verdi for
     * annotasjonen.
     */
    @Test
    public void skalIkkjeFeileVedOppslagAvValgfriAnnotasjonSomIkkjeEksistererPaaPerioda() {
        assertThat(
                eiPeriode().valgfriAnnotasjonFor(Object.class)
        ).isEqualTo(empty());
    }

    /**
     * Verifiserer at oppslag av valgfri annotasjon fungerer når perioda har ein verdi for annotasjonen.
     */
    @Test
    public void skalKunneHenteUtVerdiarForValgfrieAnnotasjonar() {
        final Object verdi = new String("valgfrie annotasjonar fungerer fint");

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

        e.expect(PaakrevdAnnotasjonManglarException.class);
        e.expectMessage(periode.toString());
        e.expectMessage("manglar ein påkrevd annotasjon av type");
        e.expectMessage(Integer.class.getSimpleName());

        periode.annotasjonFor(Integer.class);
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
        e.expect(NullPointerException.class);
        e.expectMessage("fra og med-dato er påkrevd");
        e.expectMessage("var null");
        new Underlagsperiode(null, dato("2007.12.31"));
        new Underlagsperiode(null, dato("2007.12.31"));
    }

    /**
     * Verifiserer at underlagsperiodene handhevar at til og med-dato ikkje kan vere løpande, dette for å sikre
     * at post-conditionen til oppbygginga av underlaget (som seier at eit underlag alltid skal representere ei lukka
     * tidsperiode) blir handheva.
     */
    @Test
    public void skalIkkjeKunneOpprettUnderlagsPerioderMedTilOgMedDatoLikNull() {
        e.expect(NullPointerException.class);
        e.expectMessage("til og med-dato er påkrevd");
        e.expectMessage("var null");
        new Underlagsperiode(dato("2007.12.31"), null);
    }

    private Underlagsperiode create(final String fra, final String til) {
        return new Underlagsperiode(dato(fra), dato(til));
    }

    private Underlagsperiode eiPeriode() {
        return create("2007.01.01", "2007.12.31");
    }
}