package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

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
    private final StillingsforholdId stillingsforhold;
    private final AvtaleId avtale;
    private final Observasjonsdato observasjonsdato;

    /**
     * Totalt maskinelt grunnlag for stillingsforholdet p� avtalen i det aktuelle �ret.
     */
    public final Kroner maskineltGrunnlag;

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
     * observasjonsdato. Blir det sendt inn ein observasjon som tilh�yrer eit anna stillingsforhold, ein annan avtale
     * eller ein annan observasjonsdato vil det gi feil resultat men det vil ikkje feile. Det er derfor opp til klienten
     * � sikre at dei to observasjonane kan kombinerast.
     *
     * @param other den andre observasjonen som vi skal kombinerast saman med
     * @return ein ny observasjon som inneheld summen av gjeldande og den andre observasjonens m�lingar
     * @throws AssertionError dersom observasjonane ikkje har lik stillingsforhold, avtale og observasjonsdato
     */
    TidsserieObservasjon plus(final TidsserieObservasjon other) {
        assert ofNullable(stillingsforhold).equals(ofNullable(other.stillingsforhold))
                : "Tidsserieobservasjonane tilh�yrer forskjellige stillingsforhold (" + this + ", " + other + ")";
        assert ofNullable(avtale).equals(ofNullable(other.avtale))
                : "Tidsserieobservasjonane tilh�yrer forskjellige avtalar  (" + this + ", " + other + ")";
        assert ofNullable(observasjonsdato).equals(ofNullable(other.observasjonsdato))
                : "Tidsserieobservasjonane tilh�yrer forskjellige observasjondatoar (" + this + ", " + other + ")";

        return new TidsserieObservasjon(
                stillingsforhold,
                avtale,
                observasjonsdato,
                maskineltGrunnlag.plus(other.maskineltGrunnlag)
        );
    }

    @Override
    public String toString() {
        return "observasjon utf�rt "
                + observasjonsdato
                + " for stilling "
                + stillingsforhold
                + " p� "
                + avtale
                + " med maskinelt grunnlag  "
                + maskineltGrunnlag;
    }
}
