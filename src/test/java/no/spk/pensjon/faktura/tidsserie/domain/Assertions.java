package no.spk.pensjon.faktura.tidsserie.domain;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractObjectAssert;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hjelpemetoder for � verifisere tilstanden til forskjellige typer domene- og verdiobjekt i domenemodellen til
 * tidsseriegenereringa.
 */
public final class Assertions {
    /**
     * Assertion for {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode#tilOgMed()}.
     *
     * @param periode underlagsperioda som til og med-dato skal hentast fr�
     * @return ein ny asserter for til og med-datoen til perioda
     */
    public static AbstractObjectAssert<?, Optional<LocalDate>> assertTilOgMed(final Tidsperiode<?> periode) {
        return assertThat(periode.tilOgMed()).as("til og med-dato for underlagsperiode " + periode);
    }

    /**
     * Assertion for {@link no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode#fraOgMed()}.
     *
     * @param periode underlagsperioda som fra og med-dato skal hentast fr�
     * @return ein ny asserter for fra og med-datoen til perioda
     */
    public static AbstractComparableAssert<?, LocalDate> assertFraOgMed(final Tidsperiode<?> periode) {
        return assertThat(periode.fraOgMed()).as("fra og med-dato for tidsperiode " + periode);
    }

    /**
     * Assertion for fra og med-dato til underlagsperiode nr <code>index + 1</code>.
     * <br>
     * NB: Sidan til og med-dato er valgfri assertar vi her p� ein <code>Optional&lt;LocalDate&gt;</code>, ikkje
     * <code>LocalDate</code> direkte.
     *
     * @param underlag underlaget peridoa skal hentast ut fr�
     * @param index    den 0-baserte indeksen som underlagsperioda som skal hentast ligg plassert p� i underlaget
     * @return ein ny asserter for fra og med-datoen til perioda
     */
    public static AbstractObjectAssert<?, Optional<LocalDate>> assertTilOgMed(Underlag underlag, int index) {
        return assertTilOgMed(underlag.toList().get(index));
    }

    /**
     * Assertion for til og med-dato til underlagsperiode nr <code>index + 1</code>.
     *
     * @param underlag underlaget peridoa skal hentast ut fr�
     * @param index    den 0-baserte indeksen som underlagsperioda som skal hentast ligg plassert p� i underlaget
     * @return ein ny asserter for til og med-datoen til perioda
     */
    public static AbstractComparableAssert<?, LocalDate> assertFraOgMed(Underlag underlag, int index) {
        return assertFraOgMed(underlag.toList().get(index));
    }
}
