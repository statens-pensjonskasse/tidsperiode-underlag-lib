package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;

public class YrkesskadefaktureringStatus {
    private final StillingsforholdId stillingsforhold;
    private final Prosent andel;

    public YrkesskadefaktureringStatus(final StillingsforholdId stillingsforhold, final Prosent andel) {
        this.stillingsforhold = stillingsforhold;
        this.andel = andel;
    }

    public Prosent andel() {
        return andel;
    }

    public StillingsforholdId stillingsforhold() {
        return stillingsforhold;
    }
}
