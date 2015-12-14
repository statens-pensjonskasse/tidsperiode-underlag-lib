package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medregning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner.kroner;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;
import static org.assertj.core.api.Assertions.assertThat;

public class OevreLoennsgrenseRegelTest {
    /**
     * Verifiserer at øvre lønnsgrense er lik fulltidsgrensa dersom perioda er tilknytta medregning.
     * <p>
     * Merk at forventa oppførsel for øvre grense ved medregning ikkje er spesifisert nokon stad, regelsettet som
     * medregningar skal handterast med er svært lite definert i den gamle løysinga. Vi har valgt å la øvre
     * lønnsgrense vere gjeldande også for medregning i tidsserien slik at vi har eit sikkerheitsnett i tilfelle med
     * lav datakvalitet på medregningane.
     */
    @Test
    public void skalBrukeFulltidsgrensaTilOrdningaForMedregningar() {
        assertThat(
                eiPeriode()
                        .med(new Grunnbeloep(kroner(20_000)))
                        .med(Ordning.SPK)
                        .med(new Medregning(kroner(5_000_000)))
                        .bygg()
                        .beregn(OevreLoennsgrenseRegel.class)
        ).isEqualTo(
                kroner(12 * 20_000)
        );
    }

    /**
     * Verifiserer at grenseverdien ikkje blir justert i henhold til årsfaktor, det er øvre grense for totalt maskinelt
     * grunnlag som skal bli returnert.
     */
    @Test
    public void skalIkkjeAvgrenseOevreGrenseIHenholdTilAarsfaktor() {
        assertThat(
                eiPeriode()
                        .fraOgMed(dato("2007.01.01"))
                        .tilOgMed(dato("2007.01.31"))
                        .med(new Grunnbeloep(kroner(20_000)))
                        .med(Ordning.SPK)
                        .med(fulltid())
                        .bygg()
                        .beregn(OevreLoennsgrenseRegel.class)
        ).isEqualTo(
                kroner(240_000)
        );
    }

    /**
     * Verifiserer at grenseverdien blir deltidsjustert slik at grensa blir lavare enn fulle 12G/10G
     * dersom det er ei deltidssstilling.
     * <p>
     * Ein alternativmåte å oppnå det som denne prøver å oppnå ville vere å ikkje deltidsjustere grensa og heller justere
     * lønna og faste og variable tillegg opp til verdiar for ei 100% stilling, så legge på funksjonstillegg og samanlikne
     * med grensa. Men den metodikken får store problem med å handtere nedjusteringa i etterkant viss summen er over
     * øvre grense. Problemet skyldast at funksjonstillegget ikkje er og ikkje skal deltidsjusterast. Kva som då skal
     * vere verdien av funksjonstillegg, faste- og variable-tillegg og lønn etter nedjusteringa er ikkje definert.
     */
    @Test
    public void skalDeltidsjustereGrenseverdien() {
        assertThat(
                eiPeriode()
                        .med(new Grunnbeloep(kroner(100_000)))
                        .med(Ordning.SPK)
                        .med(new Stillingsprosent(new Prosent("1%")))
                        .bygg()
                        .beregn(OevreLoennsgrenseRegel.class)
        ).isEqualTo(
                kroner(12_000)
        );
    }

    /**
     * Verifiserer at vi brukar kr 0 som øvre grense for ustøtta ordningar.
     * <p>
     * Dette er ikkje eit funksjonelt krav, kun ein teknisk forenkling for å unngå å måtte feile i situasjonar der ustøtta
     * ordningar blir forsøkt behandla.
     */
    @Test
    public void skalBruke0SomGrenseForUstoettaOrdningar() {
        assertThat(
                eiPeriode()
                        .med(new Grunnbeloep(kroner(150_000)))
                        .med(Ordning.valueOf(3030))
                        .med(new Stillingsprosent(new Prosent("100%")))
                        .bygg()
                        .beregn(OevreLoennsgrenseRegel.class)
        ).isEqualTo(
                kroner(0)
        );
    }

    private static UnderlagsperiodeBuilder eiPeriode() {
        return new UnderlagsperiodeBuilder()
                .fraOgMed(dato("2007.01.01"))
                .tilOgMed(dato("2007.12.31"))
                .med(new OevreLoennsgrenseRegel())
                .med(new ErMedregningRegel())
                .med(new ErPermisjonUtanLoennRegel())
                ;
    }
}