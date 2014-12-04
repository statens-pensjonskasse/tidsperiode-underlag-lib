package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Kodeverk for Stillingskode er definert som en konstantklasse ettersom antall verdier er begrenset, men ikke kjent. Verdi skal ligge
 * mellom 0 og 10000, mapper til TORT015.
 * <p>
 * Skamlaust kopiert frå pensjon-database-kodeverk-lib 49.1.0
 *
 * @author Per Otto Bergum Christensen
 */
public final class Stillingskode {
    public final static Stillingskode K_STIL_APO_APOTEKER = new Stillingskode(1);
    public final static Stillingskode K_STIL_APO_PROVISOR = new Stillingskode(2);
    public final static Stillingskode K_STIL_APO_FARMASOYT = new Stillingskode(3);
    public final static Stillingskode K_STIL_APO_RESEPTAR = new Stillingskode(4);
    public final static Stillingskode K_STIL_APO_LABORANT = new Stillingskode(5);
    public final static Stillingskode K_STIL_APO_TEKNIKER_U_FAG = new Stillingskode(60);
    public final static Stillingskode K_STIL_APO_TEKNIKER_M_FAG = new Stillingskode(61);
    public final static Stillingskode K_STIL_APO_KONTOR_ANSATT = new Stillingskode(7);
    public final static Stillingskode K_STIL_APO_BUD = new Stillingskode(8);
    public final static Stillingskode K_STIL_APO_RENGJ = new Stillingskode(9);
    public final static Stillingskode K_STIL_APO_BESTYRER = new Stillingskode(10);
    public final static Stillingskode K_STIL_APO_DRIFTSKONSESJONER = new Stillingskode(11);
    public final static Stillingskode K_STIL_APO_KONSULENT = new Stillingskode(12);
    public final static Stillingskode K_STIL_APO_RADGIVER = new Stillingskode(13);
    public final static Stillingskode K_STIL_APO_GENERALSEKRETER = new Stillingskode(14);

    /**
     * Disse stillingskoder (1,10,11,12,13,14) mappes ikke i TORT12 fordi de skal ikke ha lønn oppgitt som lønnstrinn
     */
    private final static Set<Stillingskode> APOTEKERSTILLINGER = new HashSet<>(Arrays.asList(
            K_STIL_APO_APOTEKER,
            K_STIL_APO_BESTYRER,
            K_STIL_APO_DRIFTSKONSESJONER,
            K_STIL_APO_KONSULENT,
            K_STIL_APO_RADGIVER,
            K_STIL_APO_GENERALSEKRETER)
    );

    /**
     * Disse mappes til K_STIL_APO_TEKNIKER_U_FAG i TORT012
     */
    private final static Set<Stillingskode> STILLLINGSGRUPPE_APO_TEKNIKER_U_FAG = new HashSet<>(Arrays.asList(
            K_STIL_APO_LABORANT,
            K_STIL_APO_KONTOR_ANSATT,
            K_STIL_APO_BUD,
            K_STIL_APO_RENGJ,
            K_STIL_APO_TEKNIKER_U_FAG,
            K_STIL_APO_TEKNIKER_M_FAG));

    /**
     * Disse mappes til K_STIL_APO_RESEPTAR i TORT012
     */
    private final static Set<Stillingskode> STILLLINGSGRUPPE_APO_RESEPTAR = new HashSet<>(Arrays.asList(
            K_STIL_APO_FARMASOYT,
            K_STIL_APO_RESEPTAR));

    /**
     * Disse stillingskoder (1,2,3,4,10,11) er farmasøyter, dvs har 39 timer per uke og egen minstegrense for medlemskap
     */
    private final static Set<Stillingskode> FARMASOYTER = new HashSet<>(Arrays.asList(
            K_STIL_APO_APOTEKER,
            K_STIL_APO_PROVISOR,
            K_STIL_APO_FARMASOYT,
            K_STIL_APO_RESEPTAR,
            K_STIL_APO_BESTYRER,
            K_STIL_APO_DRIFTSKONSESJONER)
    );

    private final Integer kode;

    private Stillingskode(int kode) {
        this.kode = kode;
    }

    /**
     * Factory metode som må implementeres for at ClassMapper (spk-mapping) skal kunne konvertere en en string verdi til Kodeverk av denne
     * typen.
     *
     * @param kode som skal parses til riktig Kodeverk
     * @return instans av Kodeverk
     */
    public static Stillingskode parse(String kode) {
        return kode != null ? new Stillingskode(Integer.parseInt(kode)) : null;
    }

    /**
     * Factory metode som må implementeres for at ClassMapper (spk-mapping) skal kunne konvertere en en heltallsvverdi til Kodeverk av denne
     * typen.
     *
     * @param kode som skal parses til riktig Kodeverk
     * @return instans av Kodeverk
     */
    public static Stillingskode parse(Integer kode) {
        return kode != null ? new Stillingskode(kode) : null;
    }

    public String getKode() {
        return kode.toString();
    }

    public Integer getKodeSomInteger() {
        return kode;
    }

    /**
     * Sjekker om stillingskoden er apoteker (fom 10 tom 14)
     *
     * @return true hvis apoteker
     */
    public boolean erApotekerstilling() {
        return APOTEKERSTILLINGER.contains(this);
    }

    /**
     * Sjekker om stillingskoden er farmasøyt (1,2,3,4,10,11)
     *
     * @return true hvis farmasøyt
     */
    public boolean erFarmasoyt() {
        return FARMASOYTER.contains(this);
    }

    /**
     * Stillingskode 5, 7, 8, 9, 60 og 61 i tort016 mappes til 60 i tort012</br> Stillingskode 3 og 4 i tort016 mappes til 4 i tort012</br>
     * Stillingskode 2 i tort016 mappes til 2 i tort012</br>
     *
     * @return stillingskode i TORT12
     */
    public Stillingskode getGruppertStillingskode() {
        if (STILLLINGSGRUPPE_APO_TEKNIKER_U_FAG.contains(this)) {
            return K_STIL_APO_TEKNIKER_U_FAG;
        }
        if (STILLLINGSGRUPPE_APO_RESEPTAR.contains(this)) {
            return K_STIL_APO_RESEPTAR;
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Stillingskode) && (((Stillingskode) obj).kode.equals(kode));
    }

    @Override
    public int hashCode() {
        return kode.hashCode();
    }

    @Override
    public String toString() {
        return "stillingskode " + kode;
    }
}
