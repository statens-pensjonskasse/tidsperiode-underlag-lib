package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.util.HashSet;
import java.util.Set;

public class Medregningskode {
    /**
     * Intern cache som tar vare på alle medregningskoder som er oppretta.
     * <p>
     * Avgrensa oppover til maksimalt 100 element for å unngå minnelekkasjar i situasjonar der valueOf blir kalla med
     * eit høgt antall forskjellige verdiar.
     */
    private static final Set<Medregningskode> VALUES = new HashSet<>();

    /**
     * Der hvor innehaver av en stilling forpliktes til å i tilllegg inneha en annen stilling, kalles denne en
     * bistilling.
     * <p>
     * Feks. en lege ansatt i et sykehus kan forpliktes til å ha en opplæringsbistilling.
     */
    public static final Medregningskode BISTILLING = new Medregningskode("12");

    /**
     * Pensjonsgivende tillegg fra annen arbeidsgiver.
     */
    public static final Medregningskode TILLEGG_ANNEN_ARBGIV = new Medregningskode("14");

    private final String kode;

    private Medregningskode(final String kode) {
        this.kode = kode;
        if (VALUES.size() < 100) {
            VALUES.add(this);
        }
    }

    /**
     * Slår opp eller opprettar ei ny medregningskode for den angitte verdien.
     *
     * @param kode kodeverdien som skal konverterast til ei medregningskode
     * @return den pre-eksisterande medregningskoda med samme verdi, eller ei ny medregningskode viss det ikkje
     * eksisterer ei predefinert kode for verdien
     */
    public static Medregningskode valueOf(final String kode) {
        return VALUES.stream().filter(e -> e.harKode(kode)).findFirst().orElse(new Medregningskode(kode));
    }

    /**
     * @see #valueOf(String)
     */
    public static Medregningskode valueOf(final int kode) {
        return valueOf(Integer.toString(kode));
    }

    /**
     * Den numeriske verdien til medregningskoda.
     *
     * @return den numeriske verdien til medregningskoda
     */
    public String kode() {
        return kode;
    }

    /**
     * @see #BISTILLING
     */
    public boolean erBistilling() {
        return BISTILLING.equals(this);
    }

    /**
     * @see #TILLEGG_ANNEN_ARBGIV
     */
    public boolean erTilleggAnnenArbeidsgiver() {
        return TILLEGG_ANNEN_ARBGIV.equals(this);
    }

    /**
     * Sjekkar om medregningstypen er fakturerbar i henhold til fastsats regelverket?
     * <p>
     * Det er kun bistillingar og lønn hos annan arbeidsgivar som kan fakturerast via fastsats.
     *
     * @return <code>true</code> viss medregningskoda er ei bistilling eller lønn hos annan arbeidagivar,
     * <code>false</code> ellers
     * @see #BISTILLING
     * @see #TILLEGG_ANNEN_ARBGIV
     */
    public boolean erFakturerbar() {
        return erBistilling() || erTilleggAnnenArbeidsgiver();
    }

    @Override
    public int hashCode() {
        return kode.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final Medregningskode other = (Medregningskode) obj;
        return other.kode.equals(kode);
    }

    @Override
    public String toString() {
        return "medregningskode " + kode;
    }

    private boolean harKode(final String kode) {
        return this.kode.equals(kode);
    }
}
