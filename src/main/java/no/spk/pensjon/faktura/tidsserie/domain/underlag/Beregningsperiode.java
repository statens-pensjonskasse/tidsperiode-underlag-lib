package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * Rolle-interface for tidsperioder som er tilrettelagt for � kunne bli k�yrt premieberegning p�.
 * <p>
 * Ei beregningsperiode best�r av ei tidsperiode og eit sett med annotasjonar for verdiane som er gyldige innanfor
 * tidsperioda. Beregningsperioda koblar tidsuavhengige grunnlagsdata fr� alle periodetyper som premieberegning treng
 * informasjon fr�, til perioda desse verdiane er gyldige i. Periodiseringa som bygger opp beregningsperiodene
 * sikrar at {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel beregningsreglane} som utf�rer
 * beregningar, ikkje treng � ta hensyn til om verdiane dei benyttar er gyldige eller ikkje, om det er +/- 1 dag for
 * beregningar som ang�r fr� og med- eller til og med-datoar, om det er mulig � ha gap eller overlapp mellom periodene
 * dei skal hente verdiar fr� osv osv.
 *
 * @param <T> periodetypa som implementerer beregningsperiode
 * @author Tarjei Skorgenes
 */
public interface Beregningsperiode<T extends Tidsperiode<T>> extends Tidsperiode<T>, HarAnnotasjonar {
    /**
     * Sl�r opp ein beregningsregel av ei bestemt type og brukar den for � gjere ei bestemt type beregning
     * ut fr� underlagsperiodas annoterte fakta.
     *
     * @param regelType kva type beregningsregel som skal brukast
     * @return resultatet fr� beregningsregelen basert p� underlagsperiodas tilstand
     * @param <T> typen p� resultatet av beregningen
     * @throws PaakrevdAnnotasjonManglarException dersom det ikkje eksisterer nokon beregningsregel
     *                                            av den angitte typa som er gyldig innanfor beregningsperioda
     */
    <T> T beregn(Class<? extends BeregningsRegel<T>> regelType) throws PaakrevdAnnotasjonManglarException;
}
