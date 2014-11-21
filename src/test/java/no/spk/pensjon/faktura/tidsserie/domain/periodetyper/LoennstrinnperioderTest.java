package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder.grupper;

/**
 * Enheitstestar for {@link Loennstrinnperioder}.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstrinnperioderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at oppslag av l�nn feilar viss det eksisterer meir enn ei l�nnstrinnperiode
     * pr l�nnstrinn i grupperinga.
     */
    @Test
    public void skalFeileUnderOppslagIGrupperingVissDetEksistererMeirEnnEiPeriodeMedLiktLoennstrinnInnanforGrupperinga() {
        final Loennstrinn loennstrinn = new Loennstrinn(1);
        final LocalDate fraOgMed = dato("2000.09.11");

        e.expect(IllegalStateException.class);
        e.expectMessage("Det er oppdaga fleire l�nnstrinnperioder for");
        e.expectMessage(loennstrinn.toString());
        e.expectMessage("som overlappar perioda");
        e.expectMessage(fraOgMed.toString());
        e.expectMessage("->");
        e.expectMessage("oppslag av l�nn for l�nnstrinn krever at det kun eksisterer 1 overlappande l�nnstrinnperiode pr tidsperiode");

        grupper(
                Stream.of(
                        new StatligLoennstrinnperiode(fraOgMed, empty(), loennstrinn, new Kroner(1)),
                        new StatligLoennstrinnperiode(fraOgMed, empty(), loennstrinn, new Kroner(2))
                )
        ).findFirst().get().loennFor(loennstrinn);
    }
}