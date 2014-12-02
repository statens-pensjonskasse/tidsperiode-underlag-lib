package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.LocalDate.MIN;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * {@link Stillingsendring}
 * representerer en tilstandsendring i et stillingsforhold der et eller flere stillingsrelaterte verdier
 * endres.
 * <p>
 * Stillingsendringer kommer fra stillingshistorikken til medlemmet og hver endring tilhører et bestemt stillingsforhold.
 * Stillingsendringer som ikke er tilknyttet et stillingsforhold tas ikke hensyn til i tidsseriegenereringen og kan
 * derfor ses bort fra.
 *
 * @author Tarjei Skorgenes
 */
public class Stillingsendring {
    private StillingsforholdId stillingsforhold;

    private Optional<LocalDate> registreringsdato = empty();

    private Optional<LocalDate> aksjonsdato = empty();

    private Optional<String> aksjonskode = empty();

    private Optional<Stillingsprosent> stillingsprosent = empty();

    private Optional<Loennstrinn> loennstrinn = empty();

    private Optional<DeltidsjustertLoenn> loenn;

    private Optional<Fastetillegg> fastetillegg = empty();

    /**
     * Tilhøyrer stillingsendringa det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet som stillingsendringa skal sjekkast opp mot
     * @return <code>true</code> dersom stillingsendringar tilhøyrer stillingsforholdet,
     * <code>false</code> viss den tilhøyrer eit anna stillingsforhold
     */
    public boolean tilhoerer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Stillingsforholdet endringa tilhøyrer.
     *
     * @param stillingsforhold stillingsforholdnummeret for stillingsforholdet endringa tilhøyrer
     * @return <code>this</code>
     */
    public Stillingsendring stillingsforhold(final StillingsforholdId stillingsforhold) {
        this.stillingsforhold = stillingsforhold;
        return this;
    }

    /**
     * Stillingsforholdet endringa tilhøyrer.
     *
     * @return stillingsforholdnummeret for stillingsforholdet endringa tilhøyrer
     */
    public StillingsforholdId stillingsforhold() {
        return stillingsforhold;
    }

    /**
     * Aksjonskoden som indikerer hvilken type stillingsendring det er snakk om.
     *
     * @param text en tekstlig representasjon av en 3-sifra tallkode som representerer endringstypen
     * @return <code>this</code>
     */
    public Stillingsendring aksjonskode(final String text) {
        this.aksjonskode = ofNullable(text);
        return this;
    }

    /**
     * Aksjonskoden som indikerer hvilken type stillingsendring det er snakk om.
     *
     * @return en tekstlig representasjon av en 3-sifra tallkode som representerer endringstypen
     */
    public String aksjonskode() {
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
     * @param dato en tekstlig representasjon av en dato på formatet yyyy.MM.dd
     * @return <code>this</code>
     */
    public Stillingsendring aksjonsdato(final LocalDate dato) {
        this.aksjonsdato = ofNullable(dato);
        return this;
    }

    /**
     * Datoen som stillingsendringen gjelder fra og med.
     *
     * @return datoen stillingsendringen inntreffer på
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
        return "031".equals(aksjonskode.orElse(null));
    }

    /**
     * Gjeldande lønnstrinn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med lønn eller med lønnstrinn, men aldri med begge to.
     *
     * @return stillingas lønnstrinn viss den ikkje innrapporteres med lønnsbeløp
     */
    public Optional<Loennstrinn> loennstrinn() {
        return loennstrinn;
    }

    /**
     * Gjeldende lønnstrinn for stillingen.
     *
     * @param loennstrinn stillingens lønnstrinn
     * @return <code>this</code>
     */
    public Stillingsendring loennstrinn(final Optional<Loennstrinn> loennstrinn) {
        this.loennstrinn = loennstrinn;
        return this;
    }

    /**
     * Gjeldande lønn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med lønn eller med lønnstrinn, men aldri med begge to.
     *
     * @return stillingas lønn viss den ikkje innrapporteres med lønnstrinn
     */
    public Optional<DeltidsjustertLoenn> loenn() {
        return loenn;
    }

    /**
     * Gjeldande lønn for stillinga.
     * <p>
     * Ei stilling kan bli innrapportert enten med lønn eller med lønnstrinn, men aldri med begge to.
     *
     * @param loenn stillingas lønn viss den ikkje innrapporteres med lønnstrinn
     */
    public Stillingsendring loenn(final Optional<DeltidsjustertLoenn> loenn) {
        this.loenn = loenn;
        return this;
    }

    /**
     * Det faste tillegget i årslønn for stillinga.
     * <p>
     * Det faste tillegget som blir innrapportert skal vere innrapportert deltidsjustert og det skal representere
     * totalt fast tillegg for heile premieåret, på samme måte som innrapportert lønn er deltidsjustert og gjeld
     * heile året.
     *
     * @return det faste tillegget i årslønn for stillinga
     */
    public Optional<Fastetillegg> fastetillegg() {
        return fastetillegg;
    }

    /**
     * @see #fastetillegg()
     */
    public Stillingsendring fastetillegg(final Optional<Fastetillegg> fastetillegg) {
        this.fastetillegg = requireNonNull(fastetillegg);
        return this;
    }

    /**
     * Stillingsbrøken for stillingsforholdet.
     *
     * @return stillingsbrøken for stillingsforholdet
     */
    public Stillingsprosent stillingsprosent() {
        return stillingsprosent.get();
    }

    /**
     * Stillingsbrøken for stillingsforholdet.
     *
     * @param stillingsprosent stillingsbrøken for stillingsforholdet
     * @return <code></code>this</code>
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Stillingsendring, registrert ").append(registreringsdato).append(", gjeldende fra ").append(aksjonsdato).append("\n");
        Stream.of(
                line("Aksjonskode: ", aksjonskode),
                line("Stillingsbrøk: ", stillingsprosent),
                line("Lønnstrinn: ", loennstrinn)
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
