package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Enheitstestar for {@link TidsserieFacade}.
 *
 * @author Tarjei Skorgenes
 */
@RunWith(MockitoJUnitRunner.class)
public class TidsserieFacadeTest {
    private final TidsserieFacade tidsserie = new TidsserieFacade();

    @Mock
    private Feilhandtering feilhandtering;

    @Test
    public void skalDelegereFeilhandteringTilEgenStrategi() {
        final StillingsforholdId id = new StillingsforholdId(1L);
        tidsserie.overstyr(feilhandtering);

        // Enkelt consumer som slurpar til seg alle underlaga slik at den forventa feilen i årsunderlag-genereringa blir trigga
        final Observasjonspublikator consumer = s -> s.toArray();
        final StillingsforholdUnderlagCallback callback = tidsserie.lagObservasjonsunderlagGenerator(consumer);
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