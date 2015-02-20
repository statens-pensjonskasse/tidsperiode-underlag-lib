package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

/**
 * {@link MinstegrenseRegel} implementerer algoritma som bestemmer kva som er minste stillingsst�rrelse som skal kunne
 * fakturerast.
 * <p>
 * Minstegrenseregelsettet har vore gjennom mange revisjonar over tid og algoritma som blir implementert her er
 * resultatet av siste revisjon av regelsettet, utf�rt v�ren 2014.
 * <p>
 * Hovedintensjonen med dette siste forenkla regelsettet er � unng� � vere avhengig av stillingsendringanes
 * stillingstype for � bestemme kva som er gjeldande minstegrense pr stilling. Datakvaliteten p� stillingstype har vist
 * seg over tid � vere s� d�rlig og up�litelig at ei forenkling som lar minstegrensa for SPK-ordninga vere basert p�
 * premiestatus, er � foretrekke. Den blir foretrukken sj�lv om denne ogs� vil bomme p� visse typer stillingar innanfor
 * premiestatusane som med den forenkla varianten f�r 35% eller 37.33% for alle ansatte innanfor avtalar med den angitte
 * premiestatusen.
 * <p>
 * For Apotekordninga og Opera-ordninga er det ikkje gjort nokon forenklingar, dei blir som f�r med Apotekordningas
 * minstegrenser styrt av stillingskode og ei flat 50% minstegrense for alle ansatte tilknytta Opera-ordninga.
 *
 * @author Tarjei Skorgenes
 */
public class MinstegrenseRegel implements BeregningsRegel<Minstegrense> {
    private static final Minstegrense MINSTEGRENSE_OPERA = new Minstegrense(new Prosent("50%"));

    private static final Minstegrense MINSTEGRENSE_APOTEK_GENERELL = new Minstegrense(new Prosent("37.5%"));
    private static final Minstegrense MINSTEGRENSE_APOTEK_FARMASOEYT = new Minstegrense(new Prosent("38.46%"));

    private static final Minstegrense MINSTEGRENSE_SPK_PEDAGOG = new Minstegrense(new Prosent("35%"));
    private static final Minstegrense MINSTEGRENSE_SPK_GENERELL = new Minstegrense(new Prosent("37.33%"));

    /**
     * Beregnar gjeldande minstegrense basert p� gjeldande minstegrenseregel for kvar av dei st�tta ordningane.
     * <p>
     * For opera-ordninga er minstegrensa lik 50% for alle stillingar.
     * <p>
     * For apotekordninga er minstegrensa lik 38.46% for alle farmas�yt-stillingar, for alle andre stillingar er den
     * 37.5%.
     * <p>
     * For SPK-ordninga er minstegrensa 35% for avtalar med premiestatus AAO-01 eller AAO-02, for alle andre
     * premiestatusar er minstegrensa 37.33%.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av beregningsregelen
     * @return gjeldande minstegrense for ordninga perioda er annotert med
     * @throws IllegalStateException dersom perioda er tilknytta ei anna ordning enn SPK-, Apotek- eller Opera-ordningane
     */
    @Override
    public Minstegrense beregn(final Underlagsperiode periode) {
        final Ordning ordning = periode.annotasjonFor(Ordning.class);
        if (Ordning.OPERA.equals(ordning)) {
            return MINSTEGRENSE_OPERA;
        } else if (Ordning.POA.equals(ordning)) {
            return minstegrenseForApotek(periode);
        } else if (Ordning.SPK.equals(ordning)) {
            return minstegrenseForStatligeStillingar(periode);
        }
        throw new IllegalStateException("Minstegrense er ikkje definert for " + ordning + ", minstegrensereglane er kun definert for SPK-, POA- og Opera-ordningane");
    }

    private Minstegrense minstegrenseForStatligeStillingar(final Underlagsperiode periode) {
        final Premiestatus premiestatus = periode.annotasjonFor(Premiestatus.class);
        if (premiestatus.equals(Premiestatus.AAO_01)) {
            return MINSTEGRENSE_SPK_PEDAGOG;
        }
        if (premiestatus.equals(Premiestatus.AAO_02)) {
            return MINSTEGRENSE_SPK_PEDAGOG;
        }
        return MINSTEGRENSE_SPK_GENERELL;
    }

    private Minstegrense minstegrenseForApotek(final Underlagsperiode periode) {
        final Stillingskode stillingskode = periode.annotasjonFor(Stillingskode.class);
        if (stillingskode.erFarmasoyt()) {
            return MINSTEGRENSE_APOTEK_FARMASOEYT;
        }
        return MINSTEGRENSE_APOTEK_GENERELL;
    }
}
