package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

import java.util.Arrays;

/**
 * @author Snorre E. Brekke - Computas
 */
public final class Produktinfo {
    public static final Produktinfo GRU_35  = new Produktinfo(35);
    public static final Produktinfo GRU_36  = new Produktinfo(36);
    public static final Produktinfo YSK_79  = new Produktinfo(79);

    private final int kode;

    public Produktinfo(int kode) {
        this.kode = kode;
    }

    public static boolean erEnAv(Produktinfo produktinfoSomSjekkes, Produktinfo... erEnAv) {
        return Arrays.stream(erEnAv)
                .filter(p -> p.equals(produktinfoSomSjekkes))
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
