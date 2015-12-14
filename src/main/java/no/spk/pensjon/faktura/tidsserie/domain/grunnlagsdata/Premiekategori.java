package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Arrays.asList;

import java.util.Optional;

/**
 * {@link Premiekategori} er en kategorisering som indikerer hvilken faktureringsmetodikk
 * som benyttes ved fakturering av en avtale.
 * <br>
 * Premiekategorien representeres utad som en 3-sifret kodeverdi.
 *
 * @author Tarjei Skorgenes
 */
public enum Premiekategori {
    /**
     * Avtalen blir fakturert via fastsats fakturering.
     */
    FASTSATS("FAS"),
    /**
     * Avtalen blir fakturert via fastsats fakturering.
     */
    FASTSATS_AARLIG_OPPFOELGING("FSA"),
    /**
     * Avtalen blir fakturert hendelsesbasert.
     */
    HENDELSESBASERT("LOP");

    private final String kode;

    Premiekategori(final String kode) {
        this.kode = kode;
    }

    /**
     * Premiekategoriens kodeverdi.
     *
     * @return kodeverdien til premiekategorien
     */
    public String kode() {
        return kode;
    }

    /**
     * Konverterer <code>kode</code> til kodens tilhørende premiekategori.
     *
     * @param kode en <code>String</code> som inneholder en kodeverdi som skal konverteres til en premiekategori
     * @return kodens tilhørende premiekategori, eller {@link java.util.Optional#empty()} viss det ikke eksisterer noen
     * premiekategori som har den angitte kodeverdien
     */
    public static Optional<Premiekategori> parse(final String kode) {
        return asList(values())
                .stream()
                .filter(p -> p.kode.equals(kode))
                .findFirst();
    }
}
