package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

class Support {
    static UnderlagsperiodeBuilder periode(final String fra, final String til) {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato(fra))
                .tilOgMed(dato(til));
    }
}
