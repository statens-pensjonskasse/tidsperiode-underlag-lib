package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.time.LocalDate.MIN;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * {@link Stillingsendring}
 * representerer en tilstandsendring i et stillingsforhold der et eller flere stillingsrelaterte verdier
 * endres.
 * <p>
 * Stillingsendringer kommer fra stillingshistorikken til medlemmet og hver endring tilh�rer et bestemt stillingsforhold.
 * Stillingsendringer som ikke er tilknyttet et stillingsforhold tas ikke hensyn til i tidsseriegenereringen og kan
 * derfor ses bort fra.
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsendring {
    private Optional<Foedselsdato> foedselsdato = empty();

    private Optional<Personnummer> personnummer = empty();

    private Optional<StillingsforholdId> stillingsforhold = empty();

    private Optional<LocalDate> registreringsdato = empty();

    private Optional<LocalDate> aksjonsdato = empty();

    private Optional<Aksjonskode> aksjonskode = of(Aksjonskode.UKJENT);

    private Optional<Stillingsprosent> stillingsprosent = empty();

    private Optional<Stillingskode> stillingskode = empty();

    private Optional<Loennstrinn> loennstrinn = empty();

    private Optional<DeltidsjustertLoenn> loenn;

    private Optional<Fastetillegg> fastetillegg = empty();

    private Optional<Variabletillegg> variabletillegg = empty();

    private Optional<Funksjonstillegg> funksjonstillegg = empty();

    /**
     * Tilh�yrer stillingsendringa det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet som stillingsendringa skal sjekkast opp mot
     * @return <code>true</code> dersom stillingsendringar tilh�yrer stillingsforholdet,
     * <code>false</code> viss den tilh�yrer eit anna stillingsforhold
     */
    public boolean tilhoerer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.map(id -> id.equals(stillingsforhold)).orElse(false);
    }

    /**
     * F�dselsdatoen til medlemmet.
     *
     * @param dato datoen medlemmet vart f�dt
     * @return <code>this</code>
     */
    public Stillingsendring foedselsdato(final Foedselsdato dato) {
        this.foedselsdato = of(dato);
        return this;
    }

    /**
     * F�dselsdatoen til medlemmet.
     *
     * @return datoen medlemmet vart f�dt
     */
    public Foedselsdato foedselsdato() {
        return foedselsdato.orElseThrow(
                () -> new IllegalStateException(
                        "f�dselsdato har ikkje blitt populert inn i stillingsendringa.\n" + this
                )
        );
    }

    /**
     * Personnummeret til medlemmet.
     *
     * @param personnummer medlemmet sitt personnummer
     * @return <code>this</code>
     */
    public Stillingsendring personnummer(final Personnummer personnummer) {
        this.personnummer = of(personnummer);
        return this;
    }

    /**
     * Personnummeret til medlemmet.
     *
     * @return medlemmet sitt personnummer
     */
    public Personnummer personnummer() {
        return personnummer.orElseThrow(
                () -> new IllegalStateException(
                        "personnummer har ikkje blitt populert inn i stillingsendringa.\n" + this
                )
        );
    }

    /**
     * Stillingsforholdet endringa tilh�yrer.
     *
     * @param stillingsforhold stillingsforholdnummeret for stillingsforholdet endringa tilh�yrer
     * @return <code>this</code>
     */
    public Stillingsendring stillingsforhold(final StillingsforholdId stillingsforhold) {
        this.stillingsforhold = of(stillingsforhold);
        return this;
    }

    /**
     * Stillingsforholdet endringa tilh�yrer.
     *
     * @return stillingsforholdnummeret for stillingsforholdet endringa tilh�yrer
     */
    public StillingsforholdId stillingsforhold() {
        return stillingsforhold.orElseThrow(
                () -> new IllegalStateException(
                        "stillingsforholdId har ikkje blitt populert inn i stillingsendringa.\n" + this
                )
        );
    }

    /**
     * Aksjonskoden som indikerer hvilken type stillingsendring det er snakk om.
     *
     * @param aksjonskode aksjonskoda som representerer kva type stillingsendring det er snakk om
     * @return <code>this</code>
     * @throws NullPointerException dersom <code>aksjonskode</code> er <code>null</code>
     */
    public Stillingsendring aksjonskode(final Aksjonskode aksjonskode) {
        this.aksjonskode = of(requireNonNull(aksjonskode, () -> "aksjonskode er p�krevd, men var null"));
        return this;
    }

    /**
     * Aksjonskoden som indikerer hvilken type stillingsendring det er snakk om.
     *
     * @return aksjonskoda som representerer kva type stillingsendring det er snakk om
     */
    public Aksjonskode aksjonskode() {
        return aksjonskode.get();
    }

    /**
     * Oppretter en ny, tom stillingsendring.
     *
     * @return en ny og tom stillingsendring
     */
    public static Stillingsendring stillingsendring() {
        return new Stillingsendring();
    }

    /**
     * Datoen som stillingsendringen gjelder fra og med.
     *
     * @param dato en tekstlig representasjon av en dato p� formatet yyyy.MM.dd
     * @return <code>this</code>
     */
    public Stillingsendring aksjonsdato(final LocalDate dato) {
        this.aksjonsdato = ofNullable(dato);
        return this;
    }

    /**
     * Datoen som stillingsendringen gjelder fra og med.
     *
     * @return datoen stillingsendringen inntreffer p�
     */
    public LocalDate aksjonsdato() {
        return aksjonsdato.orElseThrow(this::stillingsendringHarIkkeAksjonsdato);
    }

    /**
     * Representerer stillingsendringen en sluttmelding?
     *
     * @return <code>true</code> dersom endringen er en sluttmelding,
     * <code>false</code> ellers
     */
    public boolean erSluttmelding() {
        return Aksjonskode.SLUTTMELDING.equals(aksjonskode());
    }

    /**
     * Gjeldande l�nnstrinn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med l�nn eller med l�nnstrinn, men aldri med begge to.
     *
     * @return stillingas l�nnstrinn viss den ikkje innrapporteres med l�nnsbel�p
     */
    public Optional<Loennstrinn> loennstrinn() {
        return loennstrinn;
    }

    /**
     * Gjeldende l�nnstrinn for stillingen.
     *
     * @param loennstrinn stillingens l�nnstrinn
     * @return <code>this</code>
     */
    public Stillingsendring loennstrinn(final Optional<Loennstrinn> loennstrinn) {
        this.loennstrinn = loennstrinn;
        return this;
    }

    /**
     * Gjeldande l�nn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med l�nn eller med l�nnstrinn, men aldri med begge to.
     *
     * @return stillingas l�nn viss den ikkje innrapporteres med l�nnstrinn
     */
    public Optional<DeltidsjustertLoenn> loenn() {
        return loenn;
    }

    /**
     * Gjeldande l�nn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med l�nn eller med l�nnstrinn, men aldri med begge to.
     *
     * @param loenn stillingas l�nn viss den ikkje innrapporteres med l�nnstrinn
     * @return <code>this</code>
     */
    public Stillingsendring loenn(final Optional<DeltidsjustertLoenn> loenn) {
        this.loenn = loenn;
        return this;
    }

    /**
     * Det faste tillegget i �rsl�nn for stillinga.
     * <p>
     * Det faste tillegget som blir innrapportert skal vere innrapportert deltidsjustert og det skal representere
     * totalt fast tillegg for heile premie�ret, p� samme m�te som innrapportert l�nn er deltidsjustert og gjeld
     * heile �ret.
     *
     * @return det faste tillegget i �rsl�nn for stillinga
     */
    public Optional<Fastetillegg> fastetillegg() {
        return fastetillegg;
    }

    /**
     * @param fastetillegg faste tillegget i �rsl�nn for stillinga som skal settes for stillingsendringen
     * @return <code>this</code>
     * @see #fastetillegg()
     */
    public Stillingsendring fastetillegg(final Optional<Fastetillegg> fastetillegg) {
        this.fastetillegg = requireNonNull(fastetillegg);
        return this;
    }

    /**
     * Det variable tillegget i �rsl�nn for stillinga.
     * <p>
     * Det variable tillegget som blir innrapportert skal vere innrapportert deltidsjustert og det skal representere
     * totalt variabelt tillegg for heile premie�ret, p� samme m�te som innrapportert l�nn er deltidsjustert og gjeld
     * heile �ret.
     *
     * @return det variable tillegget i �rsl�nn for stillinga
     */
    public Optional<Variabletillegg> variabletillegg() {
        return variabletillegg;
    }

    /**
     * @param variabletillegg som skal settes i �rsl�nn for stillinga
     * @return <code>this</code>
     * @see #variabletillegg()
     */
    public Stillingsendring variabletillegg(final Optional<Variabletillegg> variabletillegg) {
        this.variabletillegg = requireNonNull(variabletillegg);
        return this;
    }

    /**
     * Funksjonstillegget i �rsl�nn for stillinga.
     * <p>
     * Funksjonstillegget som blir innrapportert skal ikkje deltidsjusterast og blir derfor innrapportert utan � ta
     * vere kobla p� noko vis til stillingas stillingsprosent. Tillegget skal representere totalt funksjonstillegg
     * for heile premie�ret, p� samme m�te som innrapportert l�nn gjeld heile �ret.
     *
     * @return funksjonstillegget for i �rsl�nn for stillinga
     */
    public Optional<Funksjonstillegg> funksjonstillegg() {
        return funksjonstillegg;
    }

    /**
     * @param funksjonstillegg som skal settes i �rsl�nn for stillinga
     * @return <code>this</code>
     * @see #funksjonstillegg()
     */
    public Stillingsendring funksjonstillegg(final Optional<Funksjonstillegg> funksjonstillegg) {
        this.funksjonstillegg = requireNonNull(funksjonstillegg);
        return this;
    }

    /**
     * Stillingsbr�ken for stillingsforholdet.
     *
     * @return stillingsbr�ken for stillingsforholdet
     */
    public Stillingsprosent stillingsprosent() {
        return stillingsprosent.get();
    }

    /**
     * Stillingsbr�ken for stillingsforholdet.
     *
     * @param stillingsprosent stillingsbr�ken for stillingsforholdet
     * @return <code>this</code>
     */
    public Stillingsendring stillingsprosent(Stillingsprosent stillingsprosent) {
        this.stillingsprosent = ofNullable(stillingsprosent);
        return this;
    }

    /**
     * Datoen stillingsendringa vart registrert hos SPK.
     *
     * @param dato datoen stillingsendringa vart registrert
     * @return <code>this</code>
     */
    public Stillingsendring registreringsdato(final LocalDate dato) {
        this.registreringsdato = ofNullable(dato);
        return this;
    }

    /**
     * Datoen stillingsendringa vart registrert hos SPK.
     *
     * @return dato datoen stillingsendringa vart registrert
     */
    public LocalDate registreringsdato() {
        return registreringsdato.orElse(MIN);
    }

    /**
     * Stillingsforholdet si stillingskode.
     * <p>
     * For apotekordninga er feltet p�krevd for alle stillingar som er innrapportert, b�de med og uten l�nnstrinn.
     *
     * @return stillingsendringa si stillingskode
     */
    public Optional<Stillingskode> stillingskode() {
        return stillingskode;
    }

    /**
     * @param stillingskode som skal brukes for stillingsendringen
     * @return stillingsendringa si stillingskode
     * @see #stillingskode()
     */
    public Stillingsendring stillingskode(final Optional<Stillingskode> stillingskode) {
        this.stillingskode = requireNonNull(stillingskode);
        return this;
    }

    /**
     * Annoterer underlagsperioda med stillingsendringas verdiar som er p�krevd for beregning av maskinelt grunnlag.
     * <p>
     * F�lgjande verdiar blir fors�kt annotert p� perioda viss stillingsendringa har ein ikkje-tom verdi for dei:
     * <ul>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode}</li>
     * <li>{@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn}</li>
     * </ul>
     *
     * @param periode underlagsperioda som skal annoterast
     */
    void annoter(final Annoterbar<?> periode) {
        periode.annoter(Foedselsnummer.class, new Foedselsnummer(foedselsdato(), personnummer()));
        periode.annoter(StillingsforholdId.class, stillingsforhold);

        periode.annoter(Aksjonskode.class, aksjonskode());

        periode.annoter(Stillingsprosent.class, stillingsprosent());
        periode.annoter(Stillingskode.class, stillingskode());

        periode.annoter(DeltidsjustertLoenn.class, loenn());
        periode.annoter(Fastetillegg.class, fastetillegg());
        periode.annoter(Variabletillegg.class, variabletillegg());
        periode.annoter(Funksjonstillegg.class, funksjonstillegg());

        periode.annoter(Loennstrinn.class, loennstrinn());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Stillingsendring, registrert ").append(registreringsdato).append(", med aksjonsdato ").append(aksjonsdato).append("\n");
        Stream.of(
                line("Stillingsforhold: ", stillingsforhold),
                line("Aksjonskode: ", aksjonskode),
                line("Stillingsbr�k: ", stillingsprosent),
                line("L�nnstrinn: ", loennstrinn)
        ).forEach(line -> {
            builder.append(" - ");
            line.forEach(builder::append);
            builder.append('\n');
        });
        return builder.toString();

    }

    private static Stream<Object> line(final Object... fields) {
        return Stream.of(fields);
    }

    private IllegalArgumentException stillingsendringHarIkkeAksjonsdato() {
        return new IllegalArgumentException("Stillingsendringen har ingen aksjonsdato.\n" + this);
    }
}
