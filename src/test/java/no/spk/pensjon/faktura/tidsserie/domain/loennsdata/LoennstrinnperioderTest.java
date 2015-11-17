package no.spk.pensjon.faktura.tidsserie.domain.loennsdata;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn.loennstrinn;
import static no.spk.pensjon.faktura.tidsserie.domain.loennsdata.Loennstrinnperioder.grupper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.GenerellTidsperiode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Enheitstestar for {@link Loennstrinnperioder}.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstrinnperioderTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at l�nnstrinnperioder som ikkje tilh�yrer ordninga vi grupperer for, blir filtrert bort.
     */
    @Test
    public void skalFiltrereBortLoennstrinnperioderForAndreOrdningarVedGruppering() {
        assertThat(
                Loennstrinnperioder.grupper(
                        Ordning.OPERA,
                        Stream.of(
                                new StatligLoennstrinnperiode(
                                        dato("1990.05.01"),
                                        empty(),
                                        new Loennstrinn(10),
                                        new Kroner(45_000)
                                )
                        )
                ).collect(toList())
        ).hasSize(0);
    }

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
                Ordning.SPK, Stream.of(
                        new StatligLoennstrinnperiode(fraOgMed, empty(), loennstrinn, new Kroner(1)),
                        new StatligLoennstrinnperiode(fraOgMed, empty(), loennstrinn, new Kroner(2))
                )
        ).findFirst().get().loennFor(loennstrinn, empty());
    }

    /**
     * Verifiserer at ein ikkje kallar {@link Loennstrinnperiode#harLoennFor(Loennstrinn, Optional)} for l�nnstrinnperioder som tilh�yrer andre l�nnstrinn
     * enn det som blir fors�kt sl�tt opp.
     * <p>
     * Intensjonen med dette er � sikre at ytelsen p� oppslaga av l�nn for l�nnstrinn ikkje blir forringa ettersom ytelsestuninga har vist at dette er
     * ein av hotspotane i beregningsprosessen.
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void skalIkkjeSjekkeLoennForPerioderTilhoeyrandeAndreLoennstrinn() {
        final Loennstrinnperiode annaLoennstrinn = mock(Loennstrinnperiode.class, "l�nnstrinnperiode for l�nnstrinn 2");
        when(annaLoennstrinn.trinn()).thenReturn(loennstrinn(2));

        final Loennstrinn trinn = loennstrinn(1);
        assertThat(
                new Loennstrinnperioder(
                        new GenerellTidsperiode(dato("2014.05.01"), empty()),
                        asList(
                                new StatligLoennstrinnperiode(dato("2014.05.01"), empty(), trinn, new Kroner(1)),
                                annaLoennstrinn
                        ),
                        Ordning.SPK
                ).harLoennFor(trinn, empty())
        ).isTrue();

        verify(annaLoennstrinn, never()).harLoennFor(any(Loennstrinn.class), any(Optional.class));
    }
}