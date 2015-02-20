package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregningskode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import java.util.Optional;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;

/**
 * {@link MedregningsRegel} representerer algoritma som bestemmer korvidt ei medregning er fakturerbar.
 * <p>
 * Korvidt ei medregning er fakturerbar blir styrt av medregningskoda. Bistilling og l�nn hos annan arbeidsgivar er
 * dei einaste fakturerbare medregningstypene.
 * <p>
 * Medregning blir for�vrig kun brukt i SPK-ordninga, i Apotekordninga er ikkje medregning i bruk.
 * <p>
 * Beregninga genererer alltid den �rlige medregninga, den avkortar ikkje det genererte bel�pet i henhold til
 * periodas �rsfaktor. Det er klienten sitt ansvar � foreta avkortinga til periodas �rsfaktor.
 *
 * @author Tarjei Skorgenes
 */
public class MedregningsRegel implements BeregningsRegel<Kroner> {
    /**
     * Returnerer medregningas bel�p dersom den er ei av dei fakturerbare medregningstypene.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av beregningsregelen
     * @return medregningas bel�p viss den er fakturerbar, ellers eit bel�p lik kr 0
     */
    @Override
    public Kroner beregn(final Underlagsperiode periode) {
        final Optional<Medregning> medregning = periode.valgfriAnnotasjonFor(Medregning.class);
        return medregning.map(m -> {
            if (periode.annotasjonFor(Medregningskode.class).erFakturerbar()) {
                return m.beloep();
            }
            return kroner(0);
        }).orElse(kroner(0));
    }
}
