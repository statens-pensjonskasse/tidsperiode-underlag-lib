package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.time.LocalDate.now;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning.SPK;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class AvtalekoblingsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void skalKreveStillingsforholdVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("stillingsforhold");
        e.expectMessage("er påkrevd, men var null");

        new Avtalekoblingsperiode(now(), of(now()), null, new AvtaleId(1L), SPK);
    }

    @Test
    public void skalKreveAvtaleVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("avtale");
        e.expectMessage("er påkrevd, men var null");

        new Avtalekoblingsperiode(now(), of(now()), new StillingsforholdId(1L), null, SPK);
    }

    @Test
    public void skalKreveOrdningVedKonstruksjon() {
        e.expect(NullPointerException.class);
        e.expectMessage("ordning");
        e.expectMessage("er påkrevd, men var null");

        new Avtalekoblingsperiode(now(), of(now()), new StillingsforholdId(1L), new AvtaleId(1L), null);
    }
}