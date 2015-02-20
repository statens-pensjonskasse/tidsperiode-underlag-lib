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
 * Korvidt ei medregning er fakturerbar blir styrt av medregningskoda. Bistilling og lønn hos annan arbeidsgivar er
 * dei einaste fakturerbare medregningstypene.
 * <p>
 * Medregning blir forøvrig kun brukt i SPK-ordninga, i Apotekordninga er ikkje medregning i bruk.
 * <p>
 * Beregninga genererer alltid den årlige medregninga, den avkortar ikkje det genererte beløpet i henhold til
 * periodas årsfaktor. Det er klienten sitt ansvar å foreta avkortinga til periodas årsfaktor.
 *
 * @author Tarjei Skorgenes
 */
public class MedregningsRegel implements BeregningsRegel<Kroner> {
    /**
     * Returnerer medregningas beløp dersom den er ei av dei fakturerbare medregningstypene.
     *
     * @param periode underlagsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av beregningsregelen
     * @return medregningas beløp viss den er fakturerbar, ellers eit beløp lik kr 0
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
