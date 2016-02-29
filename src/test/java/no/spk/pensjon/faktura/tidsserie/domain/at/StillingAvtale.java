package no.spk.pensjon.faktura.tidsserie.domain.at;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

/**
 * Hjelpe-klasse for å koble stillingsid til avtaleid i tester
 * @author Snorre E. Brekke - Computas
 */
public class StillingAvtale {
    private final StillingsforholdId stillingsforholdId;
    private final AvtaleId avtaleId;

    public StillingAvtale(StillingsforholdId stillingsforholdId, AvtaleId avtaleId) {
        this.stillingsforholdId = stillingsforholdId;
        this.avtaleId = avtaleId;
    }

    public StillingsforholdId stillingsforholdId() {
        return stillingsforholdId;
    }

    public AvtaleId avtaleId() {
        return avtaleId;
    }
}
