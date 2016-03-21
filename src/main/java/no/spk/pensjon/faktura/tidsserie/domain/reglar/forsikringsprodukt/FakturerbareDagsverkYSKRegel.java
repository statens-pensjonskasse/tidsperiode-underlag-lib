package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;

/**
 * {@link FakturerbareDagsverkYSKRegel} beregner fakturerbare dagsverk for GRU som:
 * faktureringsandel for YSK * antall dager i perioden
 * @author Snorre E. Brekke - Computas
 * @see BegrunnetYrkesskadefaktureringRegel
 * @see AntallDagarRegel
 */
public class FakturerbareDagsverkYSKRegel extends FakturerbareDagsverkRegel<BegrunnetFaktureringsandel> {
    public FakturerbareDagsverkYSKRegel() {
        super(BegrunnetYrkesskadefaktureringRegel.class);
    }
}
