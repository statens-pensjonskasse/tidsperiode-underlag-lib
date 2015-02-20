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
 * {@link TidsserieObservasjon} representerer ein observasjon som inngår som eit innslag
 * i ein {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie} generert ut frå alle stillingsforhold
 * tilknytta {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata} for eit medlem.
 * <p>
 * Kvar observasjon blir generert basert på eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag observasjonsunderlag}. Observasjonen
 * representerer eit totalresultat generert for alle underlagsperioder i observasjonsunderlaget som er tilknytta
 * samme stillingsforhold og avtale som observasjonen. Dette impliserer at obserasjonsunderlag for årstall der
 * eit stillingsforhold har bytta avtale, vil føre til at det blir generert to observasjonar tilknytta samme
 * stillingsforhold, men tilknytta forskjellige avtalar.
 * <p>
 * Kvar observasjon består av to delar, ein del som identifiserer eller grupperer observasjonen basert på
 * informasjon om observasjonsdato, stillinga eller avtalen, og ein annan del som består av ei eller fleire målingar
 * som er gjort basert på alle underlagsperioder som inngår i observasjonsdatoens observasjonsunderlag.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieObservasjon {
    private final HashMap<Class<?>, Object> maalingar = new HashMap<>(6);

    /**
     * Stillingsforholdet observasjonen er utført av og for.
     */
    public final StillingsforholdId stillingsforhold;

    private final AvtaleId avtale;

    /**
     * Datoen observasjonen er simulert utført på.
     */
    public final Observasjonsdato observasjonsdato;

    /**
     * Totalt maskinelt grunnlag for stillingsforholdet på avtalen i det aktuelle året.
     */
    public final Kroner maskineltGrunnlag;

    private final Optional<Premiestatus> premiestatus;

    /**
     * Konstruerer ein ny observasjon av totalt maskinelt grunnlag for eit stillingsforhold på ein bestemt avtale
     * der observasjonen er utført på eit observasjonsunderlag for den angitte observasjonsdatoen.
     *
     * @param stillingsforhold  stillingsforholdet obserasjonen er utført for
     * @param avtale            avtalen som stillingsforholdet har vore eller er aktivt på
     * @param observasjonsdato  datoen observasjonen er simulert utført
     * @param maskineltGrunnlag det maskinelle grunnlaget for alle periodene stillingsforholdet har vore aktivt på
     *                          avtalen innanfor premieåret observasjonen er utført for
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
     * Avtalen stillingsforholdet har vore tilknytta i alle perioder observasjonen er utført på.
     *
     * @return stillingsforholdets avtale
     */
    public AvtaleId avtale() {
        return avtale;
    }

    /**
     * Avtalens premiestatus pr siste periode i observasjonsunderlaget.
     * <p>
     * Merk at verdien er valgfri sidan avtalane ikkje er påkrevd å alltid ha ein premiestatus.
     */
    public Optional<Premiestatus> premiestatus() {
        return premiestatus;
    }

    /**
     * Er observasjonen tilknytta det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet observasjonen skal sjekkast mot
     * @return <code>true</code> dersom observasjonen tilhøyrer stillingsforholdet, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
    }

    /**
     * Er observasjonen tilknytta den angitte avtalen?
     *
     * @param avtale avtalen observasjonen skal sjekkast mot
     * @return <code>true</code> dersom observasjonen tilhøyrer den angite avtalen, <code>false</code> ellers
     */
    public boolean tilhoeyrer(AvtaleId avtale) {
        return this.avtale.equals(avtale);
    }

    /**
     * Ligg observasjondatoen innanfor det angitte året?
     *
     * @param aarstall årstallet som observasjondatoen skal sjekkast mot
     * @return <code>true</code> dersom observasjonsdatoen sitt årstall er lik <code>aarstall</code>
     * <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Aarstall aarstall) {
        return observasjonsdato.tilhoeyrer(aarstall);
    }


    /**
     * Ligg observasjonsdatoen innanfor den angitte månaden?
     *
     * @param month månaden observasjonsdatoen skal sjekkast opp mot
     * @return <code>true</code> dersom observasjonsdatoen ligg innanfor den aktuelle månaden, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final Month month) {
        return observasjonsdato.tilhoeyrer(month);
    }

    /**
     * Legger saman målingane frå gjeldande observasjon med målingane frå den andre observasjonen og dannar ein ny
     * observasjon med summen av kvar måling.
     * <p>
     * Kontrakta for denne operasjonen forutsetter at begge observasjonane tilhøyrer samme stillingsforhold, avtale og
     * observasjonsdato.
     *
     * @param other den andre observasjonen som vi skal kombinerast saman med
     * @return ein ny observasjon som inneheld summen av gjeldande og den andre observasjonens målingar
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
                + " på "
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
        return "Tidsserieobservasjonane tilhøyrer forskjellige "
                + reason
                + ".\n"
                + "Observasjon 1: " + this + "\n"
                + "Observasjon 2: " + other + "\n";
    }

    /**
     * Hentar ut eller genererer ei måling av den angitte datatypen frå observasjonen.
     *
     * @param <T>  datatypen som det skal hentast ut ei måling av
     * @param type datatypen som det skal hentast ut ei måling av
     * @return verdien av den angitte målingstypen, eller {@link Optional#empty() ingenting} dersom observasjonen ikkje støttar
     * målingar av den angitte typen
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> maaling(final Class<T> type) {
        return ofNullable((T) maalingar.get(type));
    }

    /**
     * Registrerer / legger til verdien av ei måling som er utført basert på observasjonens observasjonsunderlag.
     *
     * @param <T>          kva type måling som har blitt utført
     * @param <V>          verditypen til målinga
     * @param maalingsType kva type måling som har blitt utført
     * @param verdi        verdien av målinga
     * @return <code>this</code>
     */
    public <T, V extends T> TidsserieObservasjon registrerMaaling(final Class<T> maalingsType, final V verdi) {
        maalingar.put(maalingsType, verdi);
        return this;
    }
}
