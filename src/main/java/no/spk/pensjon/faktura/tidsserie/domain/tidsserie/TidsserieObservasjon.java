package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aarsverk;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;

import java.time.Month;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * {@link TidsserieObservasjon} representerer ein observasjon som inng�r som eit innslag
 * i ein {@link TidsserieFacade} generert ut fr� alle stillingsforhold
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
    public Kroner maskineltGrunnlag = Kroner.ZERO;

    private Aarsverk aarsverk = Aarsverk.ZERO;

    private final Optional<Premiestatus> premiestatus;

    /**
     * Konstruerer ein ny observasjon av totalt maskinelt grunnlag for eit stillingsforhold p� ein bestemt avtale
     * der observasjonen er utf�rt p� eit observasjonsunderlag for den angitte observasjonsdatoen.
     *
     * @param stillingsforhold stillingsforholdet obserasjonen er utf�rt for
     * @param avtale           avtalen som stillingsforholdet har vore eller er aktivt p�
     * @param observasjonsdato datoen observasjonen er simulert utf�rt
     * @param premiestatus     avtalens premiestatus
     * @throws NullPointerException dersom nokon av parameterverdiane er <code>null</code>
     */
    public TidsserieObservasjon(final StillingsforholdId stillingsforhold, final AvtaleId avtale,
                                final Observasjonsdato observasjonsdato,
                                final Optional<Premiestatus> premiestatus) {
        this.stillingsforhold = requireNonNull(stillingsforhold);
        this.avtale = requireNonNull(avtale);
        this.observasjonsdato = requireNonNull(observasjonsdato);
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
     * @return premiestatus for observasjonen dersom den finnes
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
        return new TidsserieObservasjon(
                stillingsforhold,
                avtale,
                observasjonsdato,
                premiestatus
        )
                .aarsverk(aarsverk().plus(other.aarsverk()))
                .maskineltGrunnlag(maskineltGrunnlag().plus(other.maskineltGrunnlag()))
                ;
    }

    public TidsserieObservasjon maskineltGrunnlag(final Kroner value) {
        this.maskineltGrunnlag = value;
        return this;
    }

    public Kroner maskineltGrunnlag() {
        return maskineltGrunnlag;
    }

    public TidsserieObservasjon aarsverk(final Aarsverk value) {
        this.aarsverk = value;
        return this;
    }

    public Aarsverk aarsverk() {
        return aarsverk;
    }

    @Deprecated
    public Optional<Aarsverk> maaling(final Class<? extends Aarsverk> ignored) {
        return of(aarsverk);
    }

    @Override
    public String toString() {
        return observasjonsdato
                + " for stilling "
                + stillingsforhold
                + " p� "
                + avtale
                + " med maskinelt grunnlag  "
                + maskineltGrunnlag
                + " og "
                + aarsverk
                ;
    }
}
