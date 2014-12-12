package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning.POA;

/**
 * {@link ApotekLoennstrinnperiode} representerer kva som er gjeldande lønn for ei 100% stilling tilknytta
 * Apotekordninga og som har eit bestemt lønnstrinn innanfor ei bestemt tidsperiode.
 * <p>
 * Apotekordninga har forskjellige definisjonar av lønn i 100% stilling basert på kva stillingskode-gruppering
 * stillingas stillingskode tilhøyrer. Det eksisterer derfor som regel 2 eller 3 lønnstrinnperioder for samme lønnstrinn
 * på samme tid, men med forskjellig stillingskode og lønnstrinnbeløp.
 *
 * @author Tarjei Skorgenes
 */
public class ApotekLoennstrinnperiode extends AbstractTidsperiode<ApotekLoennstrinnperiode>
        implements Loennstrinnperiode<ApotekLoennstrinnperiode> {
    private final Loennstrinn loennstrinn;
    private final Stillingskode stillingskode;
    private final LoennstrinnBeloep beloep;

    /**
     * Konstruerer ei ny lønnstrinnperiode som styrer kva som er gjeldande lønn i ei 100% stilling for eit aktuelt
     * lønnstrinn, for alle stillingskoder som er lik eller som deler samme gruppering som den angitte stillingskoda.
     *
     * @param fraOgMed      første dag i tidsperioda
     * @param tilOgMed      ein valgfri til og med-dato som er siste dag lønnsnivået for lønnstrinnet og stillingskoda
     *                      er som angitt
     * @param loennstrinn   lønnstrinnet som lønnstrinnbeløpet gjeld for
     * @param stillingskode den grupperte stillingskoda som lønnstrinnbeløpet gjeld for
     * @param beloep        lønn i 100% stilling for det aktuelle lønnstrinnet og stillingskoda innanfor tidsperiode
     * @throws NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public ApotekLoennstrinnperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed,
                                    final Loennstrinn loennstrinn, final Stillingskode stillingskode,
                                    final LoennstrinnBeloep beloep) {
        super(fraOgMed, tilOgMed);
        this.loennstrinn = requireNonNull(loennstrinn, () -> "lønnstrinn er påkrevd, men var null");
        this.stillingskode = requireNonNull(stillingskode, () -> "stillingskode er påkrevd, men var null");
        this.beloep = requireNonNull(beloep, () -> "beløp for " + loennstrinn + " og " + stillingskode + " er påkrevd, men manglar");
    }

    /**
     * {@inheritDoc}
     *
     * @see Ordning#POA
     */
    @Override
    public boolean tilhoeyrer(final Ordning ordning) {
        return POA.equals(ordning);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException dersom <code>stillingskode</code> er {@link java.util.Optional#empty()},
     *                                  stillingskode er påkrevd ved oppslag av lønn for apotekordninga
     */
    @Override
    public boolean harLoennFor(final Loennstrinn loennstrinn, final Optional<Stillingskode> stillingskode) {
        final Stillingskode kode = stillingskode.orElseThrow(() -> new IllegalArgumentException(
                        "stillingskode er påkrevd ved oppslag av lønn for Apotekordningas " + loennstrinn)
        );
        return this.loennstrinn.equals(loennstrinn) && this.stillingskode.equals(kode.getGruppertStillingskode());
    }

    @Override
    public LoennstrinnBeloep beloep() {
        return beloep;
    }

    public Stillingskode stillingskode() {
        return stillingskode;
    }

    public Loennstrinn trinn() {
        return loennstrinn;
    }

    @Override
    public String toString() {
        return "POA_LTR[" + fraOgMed() + "->" + tilOgMed().map(LocalDate::toString).orElse("") + "," + loennstrinn + "," + stillingskode + "," + beloep + "]";
    }
}
