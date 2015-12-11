package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

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
        return Stream.of(
                prognoseperiode(new MaskineltGrunnlagRegel()),
                prognoseperiode(new AarsfaktorRegel()),
                prognoseperiode(new DeltidsjustertLoennRegel()),
                prognoseperiode(new AntallDagarRegel()),
                prognoseperiode(new AarsLengdeRegel()),
                prognoseperiode(new LoennstilleggRegel()),
                prognoseperiode(new OevreLoennsgrenseRegel()),
                prognoseperiode(new MedregningsRegel()),
                new Regelperiode<>(dato("2000.01.01"), empty(), MinstegrenseRegel.class, new MinstegrenseRegelVersjon2()),
                prognoseperiode(new AarsverkRegel()),
                prognoseperiode(new ErUnderMinstegrensaRegel()),
                prognoseperiode(new ErPermisjonUtanLoennRegel()),
                prognoseperiode(new ErMedregningRegel())
        );
    }

    private Regelperiode<?> prognoseperiode(final BeregningsRegel<?> regel) {
        return new Regelperiode<>(dato("2000.01.01"), empty(), regel);
    }
}
