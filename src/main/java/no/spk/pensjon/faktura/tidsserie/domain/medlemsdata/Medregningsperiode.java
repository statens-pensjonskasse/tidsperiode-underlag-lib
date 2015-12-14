package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;

/**
 * {@link Medregningsperiode} representerer medregninga for eit bistilling eller ei stilling med tillegg frå annan
 * arbeidsgivar.
 *
 * @author Tarjei Skorgenes
 */
public class Medregningsperiode extends AbstractTidsperiode<Medregningsperiode> {
    private final Medregning medregning;
    private final Medregningskode kode;
    private final StillingsforholdId stillingsforhold;
    private final Foedselsdato foedselsdato;
    private final Personnummer personnummer;

    /**
     * Konstruerer ei ny medregning som har ein frå og med-dato og som kan ha
     * ein til og med-dato, eller som kan vere løpande og dermed har ein tom til og med-dato
     *
     * @param fraOgMed         første dag i tidsperioda
     * @param tilOgMed         viss {@link Optional#isPresent() present}, siste dag i tidsperioda, viss ikkje
     * @param medregning       beløpet som skal medregnast for stillingsforholdet
     * @param kode             medregningskoda som indikerer kva type medregning det er snakk om
     * @param stillingsforhold stillingsforholdnummeret som identifiserer stillingsforholdet medregningar tilhøyrer
     * @param foedselsdato     datoen medlemmet vart født
     * @param personnummer     personnummeret som i kombinasjon med fødselsdato, unikt identifiserer medlemmet
     * @throws NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    private Medregningsperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                               final Medregning medregning, final Medregningskode kode,
                               final StillingsforholdId stillingsforhold,
                               final Foedselsdato foedselsdato, final Personnummer personnummer) {
        super(fraOgMed, tilOgMed);
        this.medregning = requireNonNull(medregning, "medregning er påkrevd, men var null");
        this.kode = requireNonNull(kode, "medregningskode er påkrevd, men var null");
        this.stillingsforhold = requireNonNull(stillingsforhold, "stillingsforhold er påkrevd, men var null");
        this.foedselsdato = requireNonNull(foedselsdato, "fødselsdato er påkrevd, men var null");
        this.personnummer = requireNonNull(personnummer, "personnummer er påkrevd, men var null");
    }

    /**
     * Tilhøyrer medregninge det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet som medregninga skal sjekkast om tilhøyrer
     * @return <code>true</code> dersom medreginga tilhøyrer stillingsforholdet,
     * <code>false</code> viss den tilhøyrer eit anna stillingsforhold
     */
    public boolean tilhoerer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * @return Beløpet som skal medregnast for stillingsforholdet.
     */
    public Medregning beloep() {
        return medregning;
    }

    /**
     * Kode som indikerer kva type medregning det er snakk om.
     * <p>
     * For fastsatsfaktureringa er det kun bistillingar og lønn annen arbeidsgivar som skal medregnast.
     *
     * @return Medredningskode i perioden
     */
    public Medregningskode kode() {
        return kode;
    }

    /**
     * Stillingsforholdet medregninga er tilknytta.
     *
     * @return medregningas stillingsforhold
     */
    public StillingsforholdId stillingsforhold() {
        return stillingsforhold;
    }

    /**
     * Fødselsdatoen til medlemmet.
     *
     * @return fødselsdatoen til medlemmet.
     */
    public Foedselsdato foedselsdato() {
        return foedselsdato;
    }

    /**
     * Personnummeret til medlemmet.
     *
     * @return medlemmet sitt personnummer
     */
    public Personnummer personnummer() {
        return personnummer;
    }

    /**
     * Annoterer underlagsperioda med informasjon om medregningskode, medregna beløp og stillingsforhold.
     *
     * @param periode underlagsperioda som skal annoterast
     * @see no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning
     * @see no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(Medregning.class, beloep());
        periode.annoter(Medregningskode.class, kode());
        periode.annoter(StillingsforholdId.class, stillingsforhold);
        periode.annoter(Foedselsnummer.class, new Foedselsnummer(foedselsdato, personnummer));
    }

    /**
     * Startar konstruksjon av ei ny medregningsperiode.
     *
     * @return ein builder som kan konstruere nye medregningsperioder
     */
    public static Builder medregning() {
        return new Builder();
    }

    /**
     * Bygger for medregningsperioder.
     */
    public static class Builder {
        private StillingsforholdId stilling;

        private LocalDate fraOgMed;

        private Optional<LocalDate> tilOgMed;

        private Medregning beloep;

        private Medregningskode kode;

        private Foedselsdato foedselsdato;

        private Personnummer personnummer;

        private Builder() {
        }

        private Builder(final Builder other) {
            this.stilling = other.stilling;
            this.fraOgMed = other.fraOgMed;
            this.tilOgMed = other.tilOgMed;
            this.beloep = other.beloep;
            this.kode = other.kode;
            this.foedselsdato = other.foedselsdato;
            this.personnummer = other.personnummer;
        }

        /**
         * Lagar ein ny builder som er pre-populert med ein kopi av gjeldande builders tilstand.
         *
         * @return ein ny, pre-populert builder basert på gjeldande builder sin tilstand
         */
        public Builder kopi() {
            return new Builder(this);
        }

        /**
         * Konstruerer og populerer ei ny medregningsperiode basert på builderens tilstand.
         *
         * @return ei ny medregningsperiode
         */
        public Medregningsperiode bygg() {
            return new Medregningsperiode(
                    requireNonNull(fraOgMed, "fra og med-dato er påkrevd, men var null"),
                    requireNonNull(tilOgMed, "til og med-dato er påkrevd, men var null"),
                    requireNonNull(beloep, "beløp er påkrevd, men var null"),
                    requireNonNull(kode, "medregningskode er påkrevd, men var null"),
                    requireNonNull(stilling, "stillingsforholdnummer er påkrevd, men var null"),
                    requireNonNull(foedselsdato, "fødselsdato er påkrevd, men var null"),
                    requireNonNull(personnummer, "personnummer er påkrevd, men var null")
            );
        }

        /**
         * Frå og med-datoen for nye medregningsperioder.
         *
         * @param dato datoen nye medregningsperioder skal starte på
         * @return <code>this</code>
         */
        public Builder fraOgMed(final LocalDate dato) {
            this.fraOgMed = requireNonNull(dato, "fra og med-dato er påkrevd, men var null");
            return this;
        }

        /**
         * Til og med-datoen for nye medregningsperioder.
         *
         * @param dato til og med-datoen nye medregningsperioder skal avsluttast,
         *             {@link Optional#empty()} for å indikere at perioda er løpande
         * @return <code>this</code>
         */
        public Builder tilOgMed(final Optional<LocalDate> dato) {
            this.tilOgMed = requireNonNull(dato, "til og med-dato er påkrevd, men var null");
            return this;
        }

        /**
         * Indikerer at nye medregningsperioder skal vere løpande.
         *
         * @return <code>this</code>
         */
        public Builder loepende() {
            return tilOgMed(empty());
        }

        /**
         * Fødselsdatoen til medlemmet nye medregningsperioder skal vere tilknytta.
         *
         * @param dato fødselsdatoen til medlemmet
         * @return <code>this</code>
         */

        public Builder foedselsdato(final Foedselsdato dato) {
            this.foedselsdato = requireNonNull(dato, "fødselsdato er påkrevd, men var null");
            return this;
        }

        /**
         * Personnummeret til medlemmet nye medregningsperioder skal vere tilknytta.
         *
         * @param nummer personnummeret til medlemmet
         * @return <code>this</code>
         */
        public Builder personnummer(final Personnummer nummer) {
            this.personnummer = requireNonNull(nummer, "personnummer er påkrevd, men var null");
            return this;
        }

        /**
         * Stillingsforholdet nye medregningsperioder skal koblast til.
         *
         * @param id stillingsforholdnummeret for stillingsforholdet
         * @return <code>this</code>
         * @throws java.lang.NullPointerException viss <code>id</code> er <code>null</code>
         */
        public Builder stillingsforhold(final StillingsforholdId id) {
            this.stilling = requireNonNull(id, "stillingsforholdnummer er påkrevd, men var null");
            return this;
        }

        /**
         * Medregningskoda nye medregningsperioder skal benytte.
         *
         * @param kode medregningskoda for nye perioder
         * @return <code>this</code>
         */
        public Builder kode(final Medregningskode kode) {
            this.kode = requireNonNull(kode, "medregningskode er påkrevd, men var null");
            return this;
        }

        /**
         * Beløpet som nye periode skal indikere at medlemmet skal ha medregna innanfor perioda.
         *
         * @param beloep eit kronebeløp som skal medregnast medlemmet
         * @return <code>this</code>
         */
        public Builder beloep(final Medregning beloep) {
            this.beloep = requireNonNull(beloep, "beløp er påkrevd, men var null");
            return this;
        }

        /**
         * Beløpet som nye periode skal indikere at medlemmet skal ha medregna innanfor perioda.
         *
         * @param beloep eit kronebeløp som skal medregnast medlemmet
         * @return <code>this</code>
         */
        public Builder beloep(final Kroner beloep) {
            return beloep(new Medregning(beloep));
        }
    }
}
