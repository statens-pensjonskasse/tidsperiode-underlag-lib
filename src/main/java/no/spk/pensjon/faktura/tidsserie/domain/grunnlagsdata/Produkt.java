package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.Arrays;

/**
 * @author Snorre E. Brekke - Computas
 */

public enum Produkt {

    AFP("AFP"),
    FTP("FTP"),
    GRU("GRU"), 
    PEN("PEN"),
    TIP("TIP"), 
    VAR("VAR"),
    VEN("VEN"), 
    YSK("YSK"),
    UKJ("UKJ");


    private final String kode;

    Produkt(final String kode) {
        this.kode = kode;
    }

    public String kode() {
        return kode;
    }

    @Override
    public String toString() {
        return "produkt " + kode;
    }

    public static Produkt fraKode(String kode) {
        return Arrays.stream(Produkt.values())
                .filter(p -> p.kode.equals(kode))
                .findFirst()
                .orElseGet(() -> UKJ);

    }

    private boolean harKode(String kode) {
        return this.kode.equals(kode);
    }
}
