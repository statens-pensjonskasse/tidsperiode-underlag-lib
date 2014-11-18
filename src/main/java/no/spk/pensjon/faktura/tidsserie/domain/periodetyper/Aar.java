package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Feilmeldingar.AARSTALL_PAAKREVD;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Aar} representerer ei tidsperiode
 * som strekker seg frå 1. januar til 31. desember i eit bestemt årstall og som igjen er bygd
 * opp av 12 månedsperioder.
 *
 * @author Tarjei Skorgenes
 */
public class Aar implements Tidsperiode<Aar> {
    private final ArrayList<Maaned> perioder = new ArrayList<>(12);

    private final Aarstall aar;

    /**
     * Konstruerer eit nytt år.
     *
     * @param aar årstallet som året er tilknytta
     * @throws NullPointerException viss <code>aar</code> er <code>null</code>
     */
    public Aar(final Aarstall aar) {
        this.aar = aar;
        requireNonNull(aar, AARSTALL_PAAKREVD);
        asList(Month.values())
                .stream()
                .map(m -> new Maaned(aar, m))
                .collect(() -> perioder, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Årstallet som årsperioda representerer.
     *
     * @return årets årstall
     */
    public Aarstall aarstall() {
        return aar;
    }

    /**
     * Returnerer 1. januar i det aktuelle året.
     *
     * @return 1. januar
     */
    @Override
    public LocalDate fraOgMed() {
        return perioder.stream().findFirst().get().fraOgMed();
    }

    /**
     * Returnerer 31. desember i det aktuelle året.
     *
     * @return 31. desember
     */
    @Override
    public Optional<LocalDate> tilOgMed() {
        return perioder.stream().filter(m -> m.tilhoeyrer(Month.DECEMBER)).findAny().get().tilOgMed();
    }

    /**
     * Månadane som året periodisert og splittet opp i.
     *
     * @return ein straum med alle månedane i året
     */
    public Stream<Maaned> maaneder() {
        return perioder.stream();
    }

    @Override
    public String toString() {
        return aar.toString();
    }
}
