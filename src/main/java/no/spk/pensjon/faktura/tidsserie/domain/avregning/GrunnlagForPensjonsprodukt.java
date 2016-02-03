package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Objects.requireNonNull;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;

/**
 * Premie for et produkt beregnes ved å multiplisere grunnlaget for produktet med premiesatsen for produktet.
 * Grunnlaget for pensjonsproduktene er pensjonsgivende årslønn.
 *
 * {@link GrunnlagForPensjonsprodukt} representerer pensjonsgivende årslønn for en periode som skal multipliseres med en premiesats for et
 * pensjonsprodukt for å gi premie for perioden.
 * @author Snorre E. Brekke - Computas
 */
public class GrunnlagForPensjonsprodukt {
    private Kroner verdi;

    /**
     * Lager et nytt grunnlag for et pensjonsprodukt basert på et kronebeløp som representerer pensjonsgivende årslønn.
     * @param beloep pensjonsgivende årslønn som er grunnlaget for et pensjonsprodukt
     */
    public GrunnlagForPensjonsprodukt(Kroner beloep) {
        this.verdi = requireNonNull(beloep, "beloep for grunnlag for et pensjonsprodukt kan ikke være null");
    }

    /**
     * Lager et nytt grunnlag for et pensjonsprodukt basert på et kronebeløp som representerer pensjonsgivende årslønn.
     * @param beloep pensjonsgivende årslønn som er grunnlaget for et pensjonsprodukt
     * @return et nytt grunnlag for et pensjonsprodukt
     */
    public static GrunnlagForPensjonsprodukt grunnlagForPensjonsprodukt(Kroner beloep) {
        return new GrunnlagForPensjonsprodukt(beloep);
    }

    /**
     * Lager et nytt grunnlag for et pensjonsprodukt basert på en verdi som representerer pensjonsgivende årslønn.
     * @param beloep pensjonsgivende årslønn som er grunnlaget for et pensjonsprodukt
     * @return et nytt grunnlag for et pensjonsprodukt
     */
    public static GrunnlagForPensjonsprodukt grunnlagForPensjonsprodukt(int beloep) {
        return new GrunnlagForPensjonsprodukt(new Kroner(beloep));
    }


    /**
     * @return kroneverdien grunnlaget representerer
     */
    Kroner kroneverdi(){
        return verdi;
    }
}
