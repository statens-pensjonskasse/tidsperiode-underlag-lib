package no.spk.pensjon.faktura.tidsserie.domain.avtaledata;

public class Termintype {

    private final String kode;

    public String kode() {
        return kode;
    }

    /**
     * Termintype for perioder med ordning lik 3010/3035, premiestatus ulik IPB og premiekategori ulik hendelsesbasert.
     */
    public static final Termintype SPK = new Termintype("SPK");

    /**
     * Termintype for perioder med ordning lik 3060, premiestatus ulik IPB og premiekategori ulik hendelsesbasert.
     */
    public static final Termintype POA = new Termintype("POA");

    /**
     * Termintype for perioder med ordning ulik 3010/3035/3060 eller premiestatus ulik IPB eller premiekategori ulik hendelsesbasert.
     */
    public static final Termintype ANDRE = new Termintype("AND");

    /**
     * Termintype for perioder som ikke er annotert (ukjent) med minst en av disse: ordning, premiestatus og premiekategori.
     */
    public static Termintype UKJENT = new Termintype("UKJ");

    public Termintype(final String kode) {
        this.kode = kode;
    }
}
