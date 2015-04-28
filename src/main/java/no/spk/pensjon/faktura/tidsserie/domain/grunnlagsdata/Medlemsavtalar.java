package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * Medlemsavtalar representerer tilstanda til alle avtalar eit medlem er tilknytta via sine aktive stillingar
 * på eit gitt tidspunkt i tid.
 *
 * @author Tarjei Skorgenes
 */
public interface Medlemsavtalar {
    /**
     * Er stillinga tilknytta ein avtale som betalar premie til SPK for det aktuelle produktet?
     * <p>
     * Det primære use-caset for denne sjekken er å finne ut om ei stilling på eit gitt tidspunkt i tid, er tilknytta
     * ein avtale som betalar gruppelivs- eller yrkesskade-premie til SPK.
     *
     * @param stilling stillingsforholdnummeret som identifiserer stillinga vi skal sjekke mot avtalen sine produkt på
     * @param produkt  produktet som vi skal sjekke om stillinga sin avtale skal betale premie til SPK for
     * @return <code>true</code> dersom avtalen skal betale premie til SPK for produktet, <code>false</code> ellers
     * @throws IllegalArgumentException dersom medlemmet ikkje har ei avtalekobling for den angitte stillinga
     *                                  på tidspunktet medlemsavtalane er generert for
     */
    boolean betalarTilSPKFor(final StillingsforholdId stilling, Produkt produkt);

    /**
     * Hentar opp informasjon om gjeldande tilstand for avtalen stillinga er tilknytta.
     *
     * @param stilling stillingsforholdnummeret som identifiserer stillinga vi skal hente ut informasjon om avtalen for
     * @return avtalen stillinga er tilknytta
     * @throws IllegalArgumentException dersom medlemmet ikkje har ei avtalekobling for den angitte stillinga
     *                                  på tidspunktet medlemsavtalane er generert for
     */
    Avtale avtaleFor(StillingsforholdId stilling);
}
