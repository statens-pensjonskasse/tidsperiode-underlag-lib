package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;

import java.time.Month;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * {@link TidsserieObservasjon} representerer ein observasjon som inng�r som eit innslag
 * i ein {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie} generert ut fr� alle stillingsforhold
 * tilknytta {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata} for eit medlem.
 * <p>
 * Kvar observasjon blir generert basert p� eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag observasjonsunderlag}. Observasjonen
 * representerer eit totalresultat generert for alle underlagsperioder i observasjonsunderlaget som er tilknytta
 * samme stillingsforhold og avtale som observasjonen. Dette impliserer at obserasjonsunderlag for �rstall der
 * eit stillingsforhold har bytta avtale, vil f�re til at det blir generert to observasjonar tilknytta samme
 * stillingsforhold, men tilknytta forskjellige avtalar.
 * <p>
 * Kvar observasjon best�r av to delar, ein del som identifiserer eller grupperer observasjonen basert p�
 * informasjon om observasjonsdato, stillinga eller avtalen, og ein annan del som best�r av ei eller fleire m�lingar
 * som er gjort basert p� alle underlagsperioder som inng�r i observasjonsdatoens observasjonsunderlag.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieObservasjon {
    private final HashMap<Class<?>, Object> maalingar = new HashMap<>(6);

    /**
     * Stillingsforholdet observasjonen er utf�rt av og for.
     */
    public final StillingsforholdId stillingsforhold;

    private final AvtaleId avtale;

    /**
     * Datoen observasjonen er simulert utf�rt p�.
     */
    public final Observasjonsdato observasjonsdato;

    /**
     * Totalt maskinelt grunnlag for stillingsforholdet p� avtalen i det aktuelle �ret.
     */
    public final Kroner maskineltGrunnlag;

    private final Optional<Premiestatus> premiestatus;

    /**
     * Konstruerer ein ny observasjon av totalt maskinelt grunnlag for eit stillingsforhold p� ein bestemt avtale
     * der observasjonen er utf�rt p� eit observasjonsunderlag for den angitte observasjonsdatoen.
     *
     * @param stillingsforhold  stillingsforholdet obserasjonen er utf�rt for
     * @param avtale            avtalen som stillingsforholdet har vore eller er aktivt p�
     * @param observasjonsdato  datoen observasjonen er simulert utf�rt
     * @param maskineltGrunnlag det maskinelle grunnlaget for alle periodene stillingsforholdet har vore aktivt p�
     *                          avtalen innanfor premie�ret observasjonen er utf�rt for
     * @param premiestatus      avtalens premiestatus
     * @throws NullPointerException dersom nokon av parameterverdiane er <code>null</code>
     */
    public TidsserieObservasjon(final StillingsforholdId stillingsforhold, final AvtaleId avtale,
                                final Observasjonsdato observasjonsdato, final Kroner maskineltGrunnlag,
                                final Optional<Premiestatus> premiestatus) {
        this.stillingsforhold = requireNonNull(stillingsforhold);
        this.avtale = requireNonNull(avtale);
        this.observasjonsdato = requireNonNull(observasjonsdato);
        this.maskineltGrunnlag = requireNonNull(maskineltGrunnlag);
        this.premiestatus = requireNonNull(premiestatus);
    }

    /**
     * Avtalen stillingsforholdet har vore tilknytta i alle perioder observasjonen er utf�rt p�.
     *
     * @return stillingsforholdets avtale
     */
    public AvtaleId avtale() {
        return avtale;
    }

    /**
     * Avtalens premiestatus pr siste periode i observasjonsunderlaget.
     * <p>
     * Merk at verdien er valgfri sidan avtalane ikkje er p�krevd � alltid ha ein premiestatus.
     */
    public Optional<Premiestatus> premiestatus() {
        return premiestatus;
    }

    /**
     * Er observasjonen tilknytta det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet observasjonen skal sjekkast mot
     * @return <code>true</code> dersom observasjonen tilh�yrer stillingsforholdet, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Er observasjonen tilknytta den angitte avtalen?
     *
     * @param avtale avtalen observasjonen skal sjekkast mot
     * @return <code>true</code> dersom observasjonen tilh�yrer den angite avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(AvtaleId avtale) {
        return this.avtale.equals(avtale);
    }

    /**
     * Ligg observasjondatoen innanfor det angitte �ret?
     *
     * @param aarstall �rstallet som observasjondatoen skal sjekkast mot
     * @return <code>true</code> dersom observasjonsdatoen sitt �rstall er lik <code>aarstall</code>
     * <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Aarstall aarstall) {
        return observasjonsdato.tilhoeyrer(aarstall);
    }


    /**
     * Ligg observasjonsdatoen innanfor den angitte m�naden?
     *
     * @param month m�naden observasjonsdatoen skal sjekkast opp mot
     * @return <code>true</code> dersom observasjonsdatoen ligg innanfor den aktuelle m�naden, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Month month) {
        return observasjonsdato.tilhoeyrer(month);
    }

    /**
     * Legger saman m�lingane fr� gjeldande observasjon med m�lingane fr� den andre observasjonen og dannar ein ny
     * observasjon med summen av kvar m�ling.
     * <p>
     * Kontrakta for denne operasjonen forutsetter at begge observasjonane tilh�yrer samme stillingsforhold, avtale og
     * observasjonsdato.
     *
     * @param other den andre observasjonen som vi skal kombinerast saman med
     * @return ein ny observasjon som inneheld summen av gjeldande og den andre observasjonens m�lingar
     * @throws IllegalArgumentException dersom observasjonane ikkje har lik stillingsforhold, avtale og observasjonsdato
     */
    TidsserieObservasjon plus(final TidsserieObservasjon other) {
        valider(
                other,
                that -> ofNullable(this.stillingsforhold).equals(ofNullable(that.stillingsforhold)),
                () -> feilmeldingForskjelligVerdi(other, "stillingsforhold")
        );
        valider(
                other,
                that -> ofNullable(this.avtale).equals(ofNullable(that.avtale)),
                () -> feilmeldingForskjelligVerdi(other, "avtalar")
        );
        valider(
                other,
                that -> ofNullable(this.observasjonsdato).equals(ofNullable(that.observasjonsdato)),
                () -> feilmeldingForskjelligVerdi(other, "observasjonsdatoar")
        );
        valider(
                other,
                that -> premiestatus.equals(that.premiestatus),
                () -> feilmeldingForskjelligVerdi(other, "premiestatus")
        );
        return new TidsserieObservasjon(
                stillingsforhold,
                avtale,
                observasjonsdato,
                maskineltGrunnlag.plus(other.maskineltGrunnlag),
                premiestatus
        )
                .registrerMaaling(
                        Aarsverk.class,
                        aarsverk().plus(other.aarsverk())
                );
    }

    private Aarsverk aarsverk() {
        return maaling(Aarsverk.class).orElse(Aarsverk.ZERO);
    }

    @Override
    public String toString() {
        return observasjonsdato
                + " for stilling "
                + stillingsforhold
                + " p� "
                + avtale
                + " med maskinelt grunnlag  "
                + maskineltGrunnlag;
    }

    private static void valider(final TidsserieObservasjon other, final Predicate<TidsserieObservasjon> predikat,
                                final Supplier<String> feilmelding) {
        if (!predikat.test(other)) {
            throw new IllegalArgumentException(feilmelding.get());
        }
    }

    private String feilmeldingForskjelligVerdi(final TidsserieObservasjon other, final String reason) {
        return "Tidsserieobservasjonane tilh�yrer forskjellige "
                + reason
                + ".\n"
                + "Observasjon 1: " + this + "\n"
                + "Observasjon 2: " + other + "\n";
    }

    /**
     * Hentar ut eller genererer ei m�ling av den angitte datatypen fr� observasjonen.
     *
     * @param <T>  datatypen som det skal hentast ut ei m�ling av
     * @param type datatypen som det skal hentast ut ei m�ling av
     * @return verdien av den angitte m�lingstypen, eller {@link Optional#empty() ingenting} dersom observasjonen ikkje st�ttar
     * m�lingar av den angitte typen
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> maaling(final Class<T> type) {
        return ofNullable((T) maalingar.get(type));
    }

    /**
     * Registrerer / legger til verdien av ei m�ling som er utf�rt basert p� observasjonens observasjonsunderlag.
     *
     * @param <T>          kva type m�ling som har blitt utf�rt
     * @param <V>          verditypen til m�linga
     * @param maalingsType kva type m�ling som har blitt utf�rt
     * @param verdi        verdien av m�linga
     * @return <code>this</code>
     */
    public <T, V extends T> TidsserieObservasjon registrerMaaling(final Class<T> maalingsType, final V verdi) {
        maalingar.put(maalingsType, verdi);
        return this;
    }
}
