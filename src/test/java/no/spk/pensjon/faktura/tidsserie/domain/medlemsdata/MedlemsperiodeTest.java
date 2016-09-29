package no.spk.pensjon.faktura.tidsserie.domain.medlemsdata;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent.fulltid;

import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.Assertions.assertAnnotasjon;
import static org.assertj.core.api.Assertions.assertThat;

public class MedlemsperiodeTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private Medlemsperiode medlem;

    @Before
    public void _before() {
        medlem = new Medlemsperiode(dato("2015.01.01"), empty())
                .kobleTil(
                        Stream.of(
                                new StillingsforholdPeriode(dato("2015.01.01"), empty())
                                        .leggTilOverlappendeStillingsendringer(
                                                eiEndring()
                                                        .stillingsforhold(new StillingsforholdId(45L))
                                        )
                                ,
                                new StillingsforholdPeriode(dato("2015.01.01"), empty())
                                        .leggTilOverlappendeStillingsendringer(
                                                eiEndring()
                                                        .stillingsforhold(new StillingsforholdId(45L))
                                        )
                        )

                );
    }

    @Test
    public void skalIkkjeFeileHorribeltOmMedlemsperiodaManglarStillingsforholdperioder() {
        new Medlemsperiode(dato("2015.01.01"), empty()).aktiveStillingar();
    }

    @Test
    public void skalIkkjeAnnotereUnderlagsperiodaMedAktiveStillingarDersomMedlemmetIkkjeHarEiAktivStillingIMedlemsperioda() {
        final Underlagsperiode inaktivPeriode = eiPeriode();
        new Medlemsperiode(dato("2015.01.01"), empty()).annoter(inaktivPeriode);

        assertAnnotasjon(inaktivPeriode, AktiveStillingar.class).isEqualTo(empty());
    }

    @Test
    public void skalAnnotereUnderlagsperiodaMedAktiveStillingarDersomMedlemmetErIAktivStilling() {
        final Underlagsperiode periode = eiPeriode();
        medlem.annoter(periode);
        assertAnnotasjon(periode, AktiveStillingar.class).isNotEqualTo(empty());
    }

    @Test
    public void skalPopulereAktiveStillingarMedInformasjonOmKvarAktivStilling() {
        final AktiveStillingar stillingar = medlem.aktiveStillingar().get();
        assertThat(stillingar.stillingar().map(AktiveStillingar.AktivStilling::stillingsforhold).collect(toList())).containsOnlyElementsOf(
                medlem.stillingsforhold().map(StillingsforholdPeriode::stillingsforhold).collect(toList())
        );
    }

    private static Underlagsperiode eiPeriode() {
        return new Underlagsperiode(dato("2015.01.01"), dato("2015.01.31"));
    }

    private static Stillingsendring eiEndring() {
        return new Stillingsendring()
                .aksjonsdato(dato("2015.01.01"))
                .stillingsprosent(fulltid());
    }
}