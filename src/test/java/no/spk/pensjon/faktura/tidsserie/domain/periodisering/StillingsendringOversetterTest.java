package no.spk.pensjon.faktura.tidsserie.domain.periodisering;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static org.assertj.core.api.Assertions.assertThat;

public class StillingsendringOversetterTest {
    private final StillingsendringOversetter oversetter = new StillingsendringOversetter();

    /**
     * Verifiserer at oversetteren ikkje feilar dersom sybase datoar på formata
     * YYYY-MM-DD HH:mm:ss.S blir brukt som verdi på start- eller
     * sluttdatoane til avtalekoblingane.
     */
    @Test
    public void skalIkkjeFeilePaaSybaseDatoSomInkludererTid() {
        assertThat(
                oversetter.readDato(asList("1942-03-01 00:00:00.0"), 0)
        ).isEqualTo(of(dato("1942.03.01")));

        assertThat(
                oversetter.readDato(asList("1942-03-01 00:00:00.01"), 0)
        ).isEqualTo(of(dato("1942.03.01")));

        assertThat(
                oversetter.readDato(asList("1942-03-01 00:00:00.012"), 0)
        ).isEqualTo(of(dato("1942.03.01")));
    }


    /**
     * Verifiserer at oversetteren ikkje feilar dersom sybase datoar på formata
     * YYYY-MM-DD blir brukt som verdi på start- eller
     * sluttdatoane til avtalekoblingane.
     */
    @Test
    public void skalIkkjeFeilePaaSybaseDatoUtenTid() {
        assertThat(
                oversetter.readDato(asList("1942-03-01"), 0)
        ).isEqualTo(of(dato("1942.03.01")));
    }

    @Test
    public void skalIkkjeFeileDersomDatoVerdiErTom() {
        assertThat(
                oversetter.readDato(asList(""), 0)
        ).isEqualTo(empty());

        assertThat(
                oversetter.readDato(asList(" "), 0)
        ).isEqualTo(empty());
    }
}