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
 * Gruppeliv er eit forsikringsprodukt der beregningane av premie må skje pr medlem, ikkje pr stillingsforhold.
 * Gruppelivspremien blir betalt basert på antall dagar medlemmet er aktivt i løpet av eit år.
 * <p>
 * Sidan premien er på medlemsnivå må ein for perioder der medlemmet har fleire parallelle stillingar, ha ein
 * strategi for kva for eit stillingsforhold og avtale som skal betale premien for dagane i perioda.
 * <p>
 * SPK har valgt å bruke stillingsstørrelse som strategi for kva stillinga og avtale som skal dekke inn premien.
 * Dersom stillingene er like store, brukes stillingsforholdid for å bestemme hvilken stilling som skal brukes,
 * slik at det blir deterministisk oppførsel mellom kjøringer på samme datasett,
 * <p>
 * Regelen er som følger:
 * <ol>
 * <li>Stillingar tilknytta avtalar utan gruppelivsprodukt hos SPK skal aldri betale gruppelivspremie.</li>
 * <li>Stillingar tilknytta medregning skal ikke ha gruppelivspremie.</li>
 * <li>Stillingar some er ute i permisjon uten lønn skal ikke ha gruppelivspremie for perioden permisjonen gjelder.</li>
 * <li>Stillinga med størst stillingsprosent og der avtalen har gruppelivsprodukt, skal vere ansvarlig for periodas
 * gruppelivspremie</li>
 * <li>Variant: I førre punkt, viss fleire stillingar har stillingsprosent lik medlemmet si største stilling i perioda,
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
     * dette blir oppnådd ved å justere alle aktive stillingar opp til 100%. Dermed vil stillingsfordelinga ende opp
     * med å avkorte alle stillingar etter første første stilling til 0%. Sidan aktive stillingar er sortert på
     * reell stillingsstørrelse i forkant av denne oppjusteringa, medfører det at stillinga med den
     * høgaste reelle stillingsstørrelsen endar opp med 100% av fordelinga og blir einaste fakturerbare stilling for
     * perioda.
     * <p>
     * Aktive stillingar som i perioda er ute i permisjon utan lønn, blir ignorert sidan dei ikkje skal betale
     * gruppelivspremie for perioda.
     *
     * @param periode beregningsperioda som inneheld alle verdiar eller påkrevde reglar som skal benyttast av
     *                beregningsregelen
     * @return status på korvidt avtalen periodas stillingsforhold er tilknytta, skal betale gruppelivspremien
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
