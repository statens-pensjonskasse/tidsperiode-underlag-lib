package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

/**
 * Genererer regelsettet som blir benytta når det skal byggast opp ein tidsserie for avregningsformål.
 * <br>
 * Regelsettet blir generert på ein slik måte at ein skal kunne bruke gjeldande reglar i dag (2015) bakover
 * i til og med år 2000.
 * <p>
 * År 2007 er tilfeldig valgt ettersom vi ikkje kan hente ut data lenger tilbake via uttrekksbatchen.
 */
public class AvregningsRegelsett implements Regelsett {
    /**
     * Regelperiodene med alle beregningsreglane som skal brukast ved generering av tidsserie
     * for avregningsformål.
     *
     * @return ein straum med alle regelperiodene og tilhøyrande beregningsreglar for ein avregnings-tidsserie
     */
    @Override
    public Stream<Regelperiode<?>> reglar() {
        return Stream.of(
                avregningsperiode(new MaskineltGrunnlagRegel()),
                avregningsperiode(new AarsfaktorRegel()),
                avregningsperiode(new DeltidsjustertLoennRegel()),
                avregningsperiode(new AntallDagarRegel()),
                avregningsperiode(new AarsLengdeRegel()),
                avregningsperiode(new LoennstilleggRegel()),
                avregningsperiode(new OevreLoennsgrenseRegel()),
                avregningsperiode(new MedregningsRegel()),
                new Regelperiode<>(dato("2007.01.01"), of(dato("2015.12.31")), MinstegrenseRegel.class, new MinstegrenseRegelVersjon1()),
                new Regelperiode<>(dato("2016.01.01"), empty(),  MinstegrenseRegel.class, new MinstegrenseRegelVersjon2()),
                avregningsperiode(new AarsverkRegel()),
                avregningsperiode(new YrkesskadefaktureringRegel()),
                avregningsperiode(new GruppelivsfaktureringRegel()),
                avregningsperiode(new TermintypeRegel())
        );
    }

    private Regelperiode<?> avregningsperiode(final BeregningsRegel<?> regel) {
        return new Regelperiode<>(dato("2007.01.01"), empty(), regel);
    }
}
