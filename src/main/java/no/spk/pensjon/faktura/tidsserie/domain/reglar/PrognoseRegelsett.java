package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

import java.util.stream.Stream;

/**
 * Genererer regelsettet som blir benytta når det skal byggast opp ein live- eller T1-tidsserie på stillingsforholdnivå
 * pr avtale pr premieår pr observasjonsdato.
 * <br>
 * Regelsettet blir generert på ein slik måte at ein skal kunne bruke gjeldande reglar i dag (2015) bakover
 * i til og med år 2000.
 * <p>
 * År 2000 er tilfeldig valgt basert på ein antagelse om at prognosene som tidsserien blir brukt på, ikkje
 * kjem til å ha behov for tidsseriar lenger enn dette.
 */
public class PrognoseRegelsett implements Regelsett {
    /**
     * Regelperiodene med alle beregningsreglane som skal brukast ved generering av aggregert tidsserie på
     * stillingsforholdnivå for bruk i FFF-prognose eller dashboard.
     *
     * @return ein straum med alle regelperiodene og tilhøyrande beregningsreglar for ein prognose-tidsserie
     */
    @Override
    public Stream<Regelperiode<?>> reglar() {
        return Stream.<Regelperiode<?>>of(
                new Regelperiode<>(dato("2000.01.01"), empty(), new MaskineltGrunnlagRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new AarsfaktorRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new DeltidsjustertLoennRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new AntallDagarRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new AarsLengdeRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new LoennstilleggRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new OevreLoennsgrenseRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new MedregningsRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new MinstegrenseRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), new AarsverkRegel())
        );
    }
}
