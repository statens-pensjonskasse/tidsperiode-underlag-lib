package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSFORHOLDID;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSPROSENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.YSK;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;

import java.util.function.Predicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;

/**
 * Regel med strategien som styrer hvilken underlagsperioder som skal faktureres for yrkesskadeproduktet.
 * <p>
 * Yrkesskade er et forsikringspordukt der beregningene av premie m� skje per medlem, og ikke per stillingsforhold.
 * Yrkesskadepremie blir betalt basert p� antall �rsverk medlemmet er aktivt i l�pet av et �r, avkortet til maksimalt ett �rsverk.
 * </p>
 * <p>
 * Ettersom premien er p� medlemsniv� m� man ha en strategi for perioder hvor medlemmet har flere parallelle
 * stillinger, for � avgj�re hvilke stillingsforhold og avtaler som skal betale premie i en periode.
 * </p>
 * <p>
 * SPK har valgt � bruke stillingsst�rrelse som strategi for hvilke stillinger og avtaler som skal dekke premien.
 * Dersom stillingst�rrelsene er like store, brukes stillingsforholdid for � avgj�re rekkef�lgen, slik at resultatet
 * blir deterministisk mellom kj�ringer p� samme datasett.
 * <p>
 * Regelen er som f�lger:
 * <ol>
 * <li>Stillinger tilknyttet avtaler uten yrkesskadeprodukt hos SPK skal ikke betale yrkesskadepremie.
 * <li>Stillingar tilknytta medregning skal ikke betale yrkesskadepremie.</li>
 * <li>Stillingar some er ute i permisjon uten l�nn skal ikke ha yrkesskadepremie for perioden permisjonen gjelder.</li>
 * <li>Plukk stillingen med st�rst stillingsprosent. Avtalen skal betale tilsvarende andel av yrkesskadepremie.</li>
 * <li>Fortsett � plukke neste stilling med st�rst stillingsprosent fra resterende stillinger.</li>
 * <li>Dersom en plukket stilling f�rer til at samlet stillingsprosent overstiger 100%, skal stillingsprosenten til stillingen
 * avkortes slik at samlet stillingsprosent gir 100%.</li>
 * <li>Stillinger som plukkes etter at samlet stillingsprosent har n�dd 100% skal ikke betale yrkesskadepremie (avkortes til 0%,
 * implisitt gitt av regelen over).</li>
 * <li>Variant: Dersom flere stillinger har samme stillingsprosent i perioden,
 * plukkes stillingen med lavest stillingsforholdid f�rst.</li>
 * </ol>
 *
 * @author Tarjei Skorgenes
 */
public class YrkesskadefaktureringRegel implements BeregningsRegel<FaktureringsandelStatus> {

    /**
     * Beregnar om det skal trekkes yrkesskadepremie i underlagsperioden.
     * <p>
     * Yrkesskadepremie skal faktureres basert p� antall �rsverk medlemmet er aktivt i l�pet av et �r, avkortet til maksimalt ett �rsverk.<br>
     * Stillingene med st�rst stillingsprosent som tilsammen utgj�r mindre eller lik 100% stillingsprosent skal faktureres for
     * yrkesskadeproduktet. Stillinger som f�rer til at samlet stillingsprosent overstiger 100% skal avkortes slik at samlet prosent ikke
     * overstiger 100%.
     * </p>
     * <p>
     * Aktive stillinger som i perioden er ute i permisjon uten l�nn, og stillinger som er tilknyttet medregning blir ignorert.
     * Disse skal ikke betale yrkesskadepremie.
     * </p>
     *
     * @param periode beregningsperioden som inneholder alle verdier som skal benyttes av beregningsregelen
     * @return status p� hvorvidt avtalen periodens stillingsforhold er tilknytte skal betale yrkesskadepremie, og hvor stor andel den er p� den utgj�r.
     * @throws PaakrevdAnnotasjonManglarException dersom perioden ikke er annotert med {@link StillingsforholdId},
     * {@link Medlemsavtalar} eller {@link AktiveStillingar}
     */
    @Override
    public FaktureringsandelStatus beregn(final Beregningsperiode<?> periode) {
        final Medlemsavtalar avtalar = periode.annotasjonFor(Medlemsavtalar.class);
        final Predicate<AktivStilling> harYrkesskade = s -> avtalar.betalarTilSPKFor(s.stillingsforhold(), YSK);
        final Predicate<AktivStilling> permisjonUtanLoenn = s -> s
                .aksjonskode()
                .filter(PERMISJON_UTAN_LOENN::equals)
                .isPresent();

        final StillingsforholdId stilling = periode.annotasjonFor(StillingsforholdId.class);
        return new FaktureringsandelStatus(
                stilling,
                periode.annotasjonFor(AktiveStillingar.class)
                        .stillingar()
                        .filter(s -> !s.erMedregning())
                        .filter(harYrkesskade)
                        .filter(permisjonUtanLoenn.negate())
                        .sorted(SAMMENLIGN_STILLINGSPROSENT.reversed().thenComparing(SAMMENLIGN_STILLINGSFORHOLDID))
                        .reduce(
                                new Stillingsfordeling(),
                                Stillingsfordeling::leggTil,
                                Stillingsfordeling::kombinerIkkeStoettet
                        )
                        .andelFor(stilling)
                        .orElse(ZERO)
        );
    }

}
