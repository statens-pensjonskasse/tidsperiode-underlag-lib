package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avregning.AFPPremieRegel;
import no.spk.pensjon.faktura.tidsserie.domain.avregning.GRUPremieRegel;
import no.spk.pensjon.faktura.tidsserie.domain.avregning.PENPremieRegel;
import no.spk.pensjon.faktura.tidsserie.domain.avregning.TIPPremieRegel;
import no.spk.pensjon.faktura.tidsserie.domain.avregning.YSKPremieRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

/**
 * Genererer regelsettet som blir benytta når det skal byggast opp ein tidsserie for avregningsformål.
 * <br>
 * Regelsettet blir generert på ein slik måte at ein skal kunne avregne premieår 2015 og framover basert på dei
 * reelle reglane som er eller var gjendalde på eit kvart tidspunkt frå 1. janua 2015 og fram til i dag.
 * <br>
 * Årstallet 2015 er valgt fordi SPK har valgt å ikkje avregne lenger tilbake enn til premieåret 2015.
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
                avregningsperiode(new AarsverkRegel()),
                avregningsperiode(new YrkesskadefaktureringRegel()),
                avregningsperiode(new GruppelivsfaktureringRegel()),
                avregningsperiode(new TermintypeRegel()),
                avregningsperiode(new PENPremieRegel()),
                avregningsperiode(new AFPPremieRegel()),
                avregningsperiode(new TIPPremieRegel()),
                avregningsperiode(new GRUPremieRegel()),
                avregningsperiode(new YSKPremieRegel()),

                avregningsperiode(
                        new MinstegrenseRegelVersjon1(),
                        MinstegrenseRegel.class,
                        fraOgMed(),
                        of(startAar().atEndOfYear())),
                avregningsperiode(
                        new MinstegrenseRegelVersjon2(),
                        MinstegrenseRegel.class,
                        startAar().neste().atStartOfYear(),
                        empty()
                )
        );
    }

    private Regelperiode<?> avregningsperiode(final BeregningsRegel<?> regel) {
        return new Regelperiode<>(fraOgMed(), empty(), regel);
    }

    private <T> Regelperiode<T> avregningsperiode(final BeregningsRegel<? extends T> gjeldandeRegel, final Class<? extends BeregningsRegel> regelType, final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed) {
        return new Regelperiode<>(fraOgMed, tilOgMed, regelType, gjeldandeRegel);
    }

    private LocalDate fraOgMed() {
        return startAar().atStartOfYear();
    }

    private Aarstall startAar() {
        return new Aarstall(2015);
    }
}
