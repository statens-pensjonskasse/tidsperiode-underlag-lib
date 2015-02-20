package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.stream.Stream;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Enheitstestar for {@link Tidsserie}.
 *
 * @author Tarjei Skorgenes
 */
@RunWith(MockitoJUnitRunner.class)
public class TidsserieTest {
    private final Tidsserie tidsserie = new Tidsserie();

    @Mock
    private Feilhandtering feilhandtering;

    @Mock
    private Observasjonspublikator publikator;

    @Test
    public void skalDelegereFeilhandteringTilEgenStrategi() {
        final StillingsforholdId id = new StillingsforholdId(1L);
        tidsserie.overstyr(feilhandtering);

        final StillingsforholdUnderlagCallback callback = tidsserie.lagObservator(publikator);
        callback.prosesser(id,
                new Underlag(
                        Stream.of(
                                new UnderlagsperiodeBuilder()
                                        .fraOgMed(dato("2005.08.15"))
                                        .tilOgMed(dato("2005.08.31"))
                        )
                                .map(UnderlagsperiodeBuilder::bygg)
                )
        );
        verify(feilhandtering, times(1))
                .handterFeil(eq(id), any(Underlag.class), isA(PaakrevdAnnotasjonManglarException.class));
    }
}