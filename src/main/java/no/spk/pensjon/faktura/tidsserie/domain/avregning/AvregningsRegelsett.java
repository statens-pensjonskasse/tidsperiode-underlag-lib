package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsLengdeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AntallDagarRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErMedregningRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErPermisjonUtanLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErUnderMinstegrensaRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.LoennstilleggRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MedregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MinstegrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MinstegrenseRegelVersjon1;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MinstegrenseRegelVersjon2;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.OevreLoennsgrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelperiode;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelsett;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.TermintypeRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

/**
 * Genererer regelsettet som blir benytta n�r det skal byggast opp ein tidsserie for avregningsform�l.
 * <br>
 * Regelsettet blir generert p� ein slik m�te at ein skal kunne avregne premie�r 2015 og framover basert p� dei
 * reelle reglane som er eller var gjendalde p� eit kvart tidspunkt fr� 1. janua 2015 og fram til i dag.
 * <br>
 * �rstallet 2015 er valgt fordi SPK har valgt � ikkje avregne lenger tilbake enn til premie�ret 2015.
 */
public class AvregningsRegelsett implements Regelsett {
    /**
     * Regelperiodene med alle beregningsreglane som skal brukast ved generering av tidsserie
     * for avregningsform�l.
     *
     * @return ein straum med alle regelperiodene og tilh�yrande beregningsreglar for ein avregnings-tidsserie
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
                ),
                avregningsperiode(new ErUnderMinstegrensaRegel()),
                avregningsperiode(new ErPermisjonUtanLoennRegel()),
                avregningsperiode(new ErMedregningRegel())
        );
    }

    private Regelperiode<?> avregningsperiode(final BeregningsRegel<?> regel) {
        return new Regelperiode<>(fraOgMed(), empty(), regel);
    }

    @SuppressWarnings("rawtypes")
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
