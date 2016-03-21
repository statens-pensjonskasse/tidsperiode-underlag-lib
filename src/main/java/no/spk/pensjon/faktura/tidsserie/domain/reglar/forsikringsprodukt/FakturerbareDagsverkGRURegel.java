package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;

/**
 * {@link FakturerbareDagsverkGRURegel} beregner fakturerbare dagsverk for GRU som:
 * faktureringsandel for GRU * antall dager i perioden
 * @author Snorre E. Brekke - Computas
 * @see BegrunnetGruppelivsfaktureringRegel
 * @see AntallDagarRegel
 */
public class FakturerbareDagsverkGRURegel extends FakturerbareDagsverkRegel<BegrunnetFaktureringsandel> {
    public FakturerbareDagsverkGRURegel() {
        super(BegrunnetGruppelivsfaktureringRegel.class);
    }
}
