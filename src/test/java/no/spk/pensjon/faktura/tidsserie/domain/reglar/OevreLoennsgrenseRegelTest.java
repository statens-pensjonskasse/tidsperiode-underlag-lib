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
     * Verifiserer at �vre l�nnsgrense er lik fulltidsgrensa dersom perioda er tilknytta medregning.
     * <p>
     * Merk at forventa oppf�rsel for �vre grense ved medregning ikkje er spesifisert nokon stad, regelsettet som
     * medregningar skal handterast med er sv�rt lite definert i den gamle l�ysinga. Vi har valgt � la �vre
     * l�nnsgrense vere gjeldande ogs� for medregning i tidsserien slik at vi har eit sikkerheitsnett i tilfelle med
     * lav datakvalitet p� medregningane.
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
     * Verifiserer at grenseverdien ikkje blir justert i henhold til �rsfaktor, det er �vre grense for totalt maskinelt
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
     * Ein alternativm�te � oppn� det som denne pr�ver � oppn� ville vere � ikkje deltidsjustere grensa og heller justere
     * l�nna og faste og variable tillegg opp til verdiar for ei 100% stilling, s� legge p� funksjonstillegg og samanlikne
     * med grensa. Men den metodikken f�r store problem med � handtere nedjusteringa i etterkant viss summen er over
     * �vre grense. Problemet skyldast at funksjonstillegget ikkje er og ikkje skal deltidsjusterast. Kva som d� skal
     * vere verdien av funksjonstillegg, faste- og variable-tillegg og l�nn etter nedjusteringa er ikkje definert.
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
     * Verifiserer at vi brukar kr 0 som �vre grense for ust�tta ordningar.
     * <p>
     * Dette er ikkje eit funksjonelt krav, kun ein teknisk forenkling for � unng� � m�tte feile i situasjonar der ust�tta
     * ordningar blir fors�kt behandla.
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