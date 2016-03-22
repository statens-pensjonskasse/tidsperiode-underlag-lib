package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class BegrunnetFaktureringsandelTest {

    @Test
    public void er_ikke_fakturerbar_dersom_fordelingsandel_er_0() throws Exception {
        Stream.of(Fordelingsaarsak.values()).forEach(aarsak ->
                assertThat(
                        faktureringsandel(aarsak, ZERO).erFakturerbar()
                ).isFalse()
        );
    }

    @Test
    public void er_fakturerbar_dersom_fordelingsandel_er_stoerre_enn_0_og_aarsak_er_fakturerbar() throws Exception {
        Stream.of(Fordelingsaarsak.values())
                .filter(Fordelingsaarsak::fakturerbar)
                .forEach(aarsak ->
                        assertThat(
                                faktureringsandel(aarsak, prosent("1%")).erFakturerbar()
                        ).isTrue()
                );
    }

    @Test
    public void er_ikke_fakturerbar_dersom_fordelingsandel_er_stoerre_enn_0_og_aarsak_er_ikke_fakturerbar() throws Exception {
        Stream.of(Fordelingsaarsak.values())
                .filter(a -> !a.fakturerbar())
                .forEach(aarsak ->
                        assertThat(
                                faktureringsandel(aarsak, prosent("1%")).erFakturerbar()
                        ).isFalse()
                );
    }

    private BegrunnetFaktureringsandel faktureringsandel(Fordelingsaarsak aarsak, Prosent zero) {
        return new BegrunnetFaktureringsandel(
                stillingsforhold(1),
                zero,
                aarsak
        );
    }
}