package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.util.Arrays;

/**
 * @author Snorre E. Brekke - Computas
 */
public final class Produktinfo {
    public static final int GRU_35  = 35;
    public static final int GRU_36  = 36;
    public static final int YSK_79  = 79;

    private final int kode;

    public Produktinfo(int kode) {
        this.kode = kode;
    }

    public static boolean erEnAv(Produktinfo produktinfoSomSjekkes, int... erEnAvKoder) {
        return Arrays.stream(erEnAvKoder)
                .filter(p -> p == produktinfoSomSjekkes.kode)
                .findFirst()
                .isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produktinfo)){
            return false;
        }

        Produktinfo that = (Produktinfo) o;
        return kode == that.kode;

    }

    @Override
    public int hashCode() {
        return kode;
    }

    @Override
    public String toString() {
        return String.valueOf(kode);
    }
}
