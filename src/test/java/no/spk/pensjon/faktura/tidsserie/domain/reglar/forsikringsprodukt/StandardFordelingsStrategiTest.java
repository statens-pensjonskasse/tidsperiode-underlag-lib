package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import static java.util.Optional.of;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.ENDRINGSMELDING;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.AVKORTET;
import static no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt.Fordelingsaarsak.ORDINAER;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class StandardFordelingsStrategiTest {

    private StandardFordelingsStrategi strategi;

    @Before
    public void setUp() throws Exception {
        strategi = new StandardFordelingsStrategi();

    }

    @Test
    public void skal_gi_ordinaer_andel_for_100_prosent_stilling_naar_maksimal_andel_er_100_prosent() throws Exception {
        final BegrunnetFaktureringsandel andel = strategi.begrunnetAndelFor(
                enAktivStilling(prosent("100%")),
                prosent("100%")
        );

        assertThat(andel.fordelingsaarsak()).isEqualTo(ORDINAER);
        assertThat(andel.andel().equals(prosent("100%"), 2)).isTrue();
    }

    @Test
    public void skal_gi_ordinaer_andel_for_99_prosent_stilling_naar_maksimal_andel_er_100_prosent() throws Exception {
        final BegrunnetFaktureringsandel andel = strategi.begrunnetAndelFor(
                enAktivStilling(prosent("99%")),
                prosent("100%")
        );

        assertThat(andel.fordelingsaarsak()).isEqualTo(ORDINAER);
        assertThat(andel.andel().equals(prosent("99%"), 2)).isTrue();
    }

    @Test
    public void skal_gi_avkrotet_andel_for_100_prosent_stilling_naar_maksimal_andel_er_99_prosent() throws Exception {
        final BegrunnetFaktureringsandel andel = strategi.begrunnetAndelFor(
                enAktivStilling(prosent("100%")),
                prosent("99%")
        );

        assertThat(andel.fordelingsaarsak()).isEqualTo(AVKORTET);
        assertThat(andel.andel().equals(prosent("99%"), 2)).isTrue();
    }

    private AktiveStillingar.AktivStilling enAktivStilling(Prosent stillingsprosent) {
        return new AktiveStillingar.AktivStilling(
                stillingsforhold(1),
                of(stillingsprosent),
                of(ENDRINGSMELDING)
                );
    }
}