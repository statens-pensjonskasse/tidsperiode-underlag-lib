package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AntallDagar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link FakturerbareDagsverkYSKRegel} beregner fakturerbare dagsverk for et produkt som som:
 * faktureringsandel beregnet av regel * antall dager i perioden
 * @author Snorre E. Brekke - Computas
 * @see AntallDagarRegel
 * @see FaktureringsandelStatus
 */
class FakturerbareDagsverkRegel<T extends FaktureringsandelStatus> implements BeregningsRegel<FakturerbareDagsverk> {
    private final Class<? extends BeregningsRegel<T>> faktureringsandelRegel;

    public FakturerbareDagsverkRegel(Class<? extends BeregningsRegel<T>> faktureringsandelRegel) {
        this.faktureringsandelRegel = faktureringsandelRegel;
    }

    @Override
    public FakturerbareDagsverk beregn(final Beregningsperiode<?> periode) {
        final FaktureringsandelStatus faktureringsandel = periode.beregn(faktureringsandelRegel);
        final AntallDagar antallDager = periode.beregn(AntallDagarRegel.class);
        return new FakturerbareDagsverk(antallDager.verdi()).multiply(faktureringsandel.andel());
    }
}
