package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

/**
 * {@link Fordelingsaarsak}
 * @author Snorre E. Brekke - Computas
 */
public enum Fordelingsaarsak {
    /**
     * Angir at stillingen er medregnet, og skal ikke faktureres.
     */
    ER_MEDREGNING("MED", false),
    /**
     * Angir at stillingen er permisjon uten lønn, og skal ikke faktureres.
     */
    ER_PERMISJON_UTEN_LOENN("PER", false),
    /**
     * Angir at avtalen stillingen tilhører ikke har angitt produkt (YSK eller GRU), og skal ikke faktureres.
     */
    AVTALE_IKKE_FAKTURERBAR_FOR_PRODUKT("IFP", false),

    /**
     * Angir at stillingen skal faktureres fullt. For GRU betyr det 100%, for YSK betyr det andel lik stillingsprosent.
     */
    ORDINAER("ORD", true),
    /**
     * Angir at stillingen ikke skal faktureres fullt. For GRU betyr det 0%, for YSK betyr det faktureringsandelen er mindre enn stillingsprosenten.
     */
    AVKORTET("AVK", true);

    private final boolean fakturerbar;
    private final String kode;

    Fordelingsaarsak(String kode, boolean fakturerbar) {
        this.kode = kode;
        this.fakturerbar = fakturerbar;
    }

    /**
     * Angir om arsaken skal føre til fakturering.
     * @return true dersom aarsaken skal føre til fakturering
     */
    public boolean fakturerbar(){
        return fakturerbar;
    }

    public String kode(){
        return kode;
    }
}
