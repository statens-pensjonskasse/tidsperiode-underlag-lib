package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Medlemsdata;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Enheitstestar for {@link StillingsforholdunderlagFactory}.
 *
 * @author Tarjei Skorgenes
 */
public class StillingsforholdunderlagFactoryTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    /**
     * Verifiserer at prosessering av medlemsdata for medlemmar som kun har avtalekoblingar, feilar.
     * <p>
     * Denne situasjonen er typisk ein indikasjon på dårlig datakvalitet, oftast på grunn av sletta stillingsforhold
     * der alle medregningar eller stillingsendringar er sletta utan å rydde opp og fjerne stillinganes avtalekoblingar.
     */
    @Test
    public void skalFeileDersomMedlemsdataKunInneheldAvtalekoblingar() {
        e.expect(IllegalStateException.class);
        e.expectMessage("Eit medlem må ha minst ei stillingsendring eller medregning");
        e.expectMessage("det kan ikkje kun ha avtalekoblingar");

        final Medlemsdata medlem = mock(Medlemsdata.class);
        when(medlem.periodiser()).thenReturn(Optional.empty());

        final StillingsforholdunderlagFactory factory = new StillingsforholdunderlagFactory();
        factory.prosesser(
                medlem,
                (a, b) -> {
                },
                new Observasjonsperiode(
                        new Aarstall(2000).atStartOfYear(),
                        new Aarstall(2000).atEndOfYear()
                )
        );
    }
}
