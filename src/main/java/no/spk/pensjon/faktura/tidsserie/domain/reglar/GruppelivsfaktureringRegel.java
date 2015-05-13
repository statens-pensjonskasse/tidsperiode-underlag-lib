package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode.PERMISJON_UTAN_LOENN;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSFORHOLDID;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling.SAMMENLIGN_STILLINGSPROSENT;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt.GRU;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.ZERO;

import java.util.function.Function;
import java.util.function.Predicate;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar.AktivStilling;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.PaakrevdAnnotasjonManglarException;

/**
 * Regel med strategien som styrer kva underlagsperioder som skal fakturerast for gruppelivsproduktet.
 * <p>
 * Gruppeliv er eit forsikringsprodukt der beregningane av premie m� skje pr medlem, ikkje pr stillingsforhold.
 * Gruppelivspremien blir betalt basert p� antall dagar medlemmet er aktivt i l�pet av eit �r.
 * <p>
 * Sidan premien er p� medlemsniv� m� ein for perioder der medlemmet har fleire parallelle stillingar, ha ein
 * strategi for kva for eit stillingsforhold og avtale som skal betale premien for dagane i perioda.
 * <p>
 * SPK har valgt � bruke stillingsst�rrelse som strategi for kva stillinga og avtale som skal dekke inn premien.
 * Dersom stillingene er like store, brukes stillingsforholdid for � bestemme hvilken stilling som skal brukes,
 * slik at det blir deterministisk oppf�rsel mellom kj�ringer p� samme datasett,
 * <p>
 * Regelen er som f�lger:
 * <ol>
 * <li>Stillingar tilknytta avtalar utan gruppelivsprodukt hos SPK skal aldri betale gruppelivspremie.</li>
 * <li>Stillingar tilknytta medregning skal ikke ha gruppelivspremie.</li>
 * <li>Stillingar some er ute i permisjon uten l�nn skal ikke ha gruppelivspremie for perioden permisjonen gjelder.</li>
 * <li>Stillinga med st�rst stillingsprosent og der avtalen har gruppelivsprodukt, skal vere ansvarlig for periodas
 * gruppelivspremie</li>
 * <li>Variant: I f�rre punkt, viss fleire stillingar har stillingsprosent lik medlemmet si st�rste stilling i perioda,
 * blir stillingen med lavest stillingsforholdid valgt.</li>
 * </ol>
 *
 * @author Tarjei Skorgenes
 */
public class GruppelivsfaktureringRegel implements BeregningsRegel<FaktureringsandelStatus> {
    private static final Prosent FULLTID = new Prosent("100%");

    /**
     * Beregnar korvidt det skal trekkast inn gruppelivspremie for underlagsperioda.
     * <p>
     * Gjeldande strategi for fakturering av gruppelivspremie seier at kun ei stilling skal fakturerast pr periode,
     * dette blir oppn�dd ved � justere alle aktive stillingar opp til 100%. Dermed vil stillingsfordelinga ende opp
     * med � avkorte alle stillingar etter f�rste f�rste stilling til 0%. Sidan aktive stillingar er sortert p�
     * reell stillingsst�rrelse i forkant av denne oppjusteringa, medf�rer det at stillinga med den
     * h�gaste reelle stillingsst�rrelsen endar opp med 100% av fordelinga og blir einaste fakturerbare stilling for
     * perioda.
     * <p>
     * Aktive stillingar som i perioda er ute i permisjon utan l�nn, blir ignorert sidan dei ikkje skal betale
     * gruppelivspremie for perioda.
     *
     * @param periode beregningsperioda som inneheld alle verdiar eller p�krevde reglar som skal benyttast av
     *                beregningsregelen
     * @return status p� korvidt avtalen periodas stillingsforhold er tilknytta, skal betale gruppelivspremien
     * @throws PaakrevdAnnotasjonManglarException viss perioda ikkje er annotert med {@link StillingsforholdId},
     *                                            {@link Medlemsavtalar} eller {@link AktiveStillingar}
     */
    @Override
    public FaktureringsandelStatus beregn(final Beregningsperiode<?> periode) throws PaakrevdAnnotasjonManglarException {
        final Medlemsavtalar avtalar = periode.annotasjonFor(Medlemsavtalar.class);
        final Predicate<AktivStilling> harGruppeliv = s -> avtalar.betalarTilSPKFor(s.stillingsforhold(), GRU);
        final Predicate<AktivStilling> permisjonUtanLoenn = s -> s
                .aksjonskode()
                .filter(PERMISJON_UTAN_LOENN::equals)
                .isPresent();


        final StillingsforholdId stilling = periode.annotasjonFor(StillingsforholdId.class);
        Function<AktivStilling, AktivStilling> oppjusterTilFulltid = this::oppjusterTilFulltid;
        return new FaktureringsandelStatus(
                stilling,
                periode.annotasjonFor(AktiveStillingar.class)
                        .stillingar()
                        .filter(s -> !s.erMedregning())
                        .filter(harGruppeliv)
                        .filter(permisjonUtanLoenn.negate())
                        .sorted(SAMMENLIGN_STILLINGSPROSENT.reversed().thenComparing(SAMMENLIGN_STILLINGSFORHOLDID))
                        .map(oppjusterTilFulltid)
                        .reduce(
                                new Stillingsfordeling(),
                                Stillingsfordeling::leggTil,
                                Stillingsfordeling::kombinerIkkeStoettet
                        )
                        .andelFor(stilling)
                        .orElse(ZERO)
        );
    }

    private AktivStilling oppjusterTilFulltid(final AktivStilling stilling) {
        return stilling.juster(FULLTID);
    }
}
