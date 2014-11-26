package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * {@link TidsserieObservasjon} representerer ein observasjon som inng�r som eit innslag
 * i ein {@link no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Tidsserie} generert ut fr� alle stillingsforhold tilknytta {@link no.spk.pensjon.faktura.tidsserie.domain.periodisering.Medlemsdata} for ein
 * medlem.
 * <p>
 * Kvar observasjon blir generert basert p� eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag observasjonsunderlag}. Observasjonen
 * representerer eit totalresultat generert for alle underlagsperioder i observasjonsunderlaget som er tilknytta
 * samme stillingsforhold og avtale som observasjonen. Dette impliserer at obserasjonsunderlag for �rstall der
 * eit stillingsforhold har bytta avtale, vil f�re til at det blir generert to observasjonar tilknytta samme
 * stillingsforhold, men tilknytta forskjellige avtalar.
 *
 * @author Tarjei Skorgenes
 */
public class TidsserieObservasjon {
    /**
     * Stillingsforholdet observasjonen er utf�rt av og for.
     */
    public final StillingsforholdId stillingsforhold;

    /**
     * Avtalen stillingsforholdet har vore tilknytta i alle perioder observasjonen er utf�rt p�.
     */
    public final AvtaleId avtale;

    /**
     * Datoen observasjonen er simulert utf�rt p�.
     */
    public final Observasjonsdato observasjonsdato;

    /**
     * Totalt maskinelt grunnlag for stillingsforholdet p� avtalen i det aktuelle �ret.
     */
    public final Kroner maskineltGrunnlag;

    /**
     * Konstruerer ein ny observasjon av totalt maskinelt grunnlag for eit stillingsforhold p� ein bestemt avtale
     * der observasjonen er utf�rt p� eit observasjonsunderlag for den angitte observasjonsdatoen.
     *
     * @param stillingsforhold  stillingsforholdet obserasjonen er utf�rt for
     * @param avtale            avtalen som stillingsforholdet har vore eller er aktivt p�
     * @param observasjonsdato  datoen observasjonen er simulert utf�rt
     * @param maskineltGrunnlag det maskinelle grunnlaget for alle periodene stillingsforholdet har vore aktivt p�
     *                          avtalen innanfor premie�ret observasjonen er utf�rt for
     * @throws NullPointerException dersom nokon av parameterverdiane er <code>null</code>
     */
    public TidsserieObservasjon(final StillingsforholdId stillingsforhold, final AvtaleId avtale,
                                final Observasjonsdato observasjonsdato, final Kroner maskineltGrunnlag) {
        this.stillingsforhold = requireNonNull(stillingsforhold);
        this.avtale = requireNonNull(avtale);
        this.observasjonsdato = requireNonNull(observasjonsdato);
        this.maskineltGrunnlag = requireNonNull(maskineltGrunnlag);
    }

    /**
     * Er observasjonen tilknytta det angitte stillingsforholdet?
     *
     * @param stillingsforhold stillingsforholdet obserasjonen skal sjekkast mot
     * @return <code>true</code> dersom observasjonen tilh�yrer stillingsforholdet, <code>false</code> ellers
     */
    public boolean tilhoeyrer(final StillingsforholdId stillingsforhold) {
        return this.stillingsforhold.equals(stillingsforhold);
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
        return new TidsserieObservasjon(
                stillingsforhold,
                avtale,
                observasjonsdato,
                maskineltGrunnlag.plus(other.maskineltGrunnlag)
        );
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
}
