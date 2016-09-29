package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premiebeloep.premiebeloep;
import static no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.premier;

import no.spk.pensjon.faktura.tidsserie.domain.avregning.Premier.PremierBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * Enheitstestsuite for {@link Premier}.
 *
 * @author Tarjei Skorgenes
 */
public class PremierTest {
    private PremierBuilder builder;

    @Before
    public void _before() {
        builder = premier();
    }

    @Test
    public void skalDefaultePremieTilKroner0VissIkkjeOverstyrt() {
        final Premier premier = builder.bygg();
        final Premiebeloep expected = premiebeloep("kr 0");
        Assertions.assertArbeidsgiverpremie(premier).isEqualTo(expected);
        Assertions.assertMedlemspremie(premier).isEqualTo(expected);
        Assertions.assertAdministrasjonsgebyr(premier).isEqualTo(expected);
    }

    @Test
    public void skalPopulereMedlemspremie() {
        final Premiebeloep expected = premiebeloep("kr 100");
        Assertions.assertMedlemspremie(builder.medlem(expected).bygg()).isEqualTo(expected);
    }

    @Test
    public void skalPopulereArbeidsgiverpremie() {
        final Premiebeloep expected = premiebeloep("kr 100");
        Assertions.assertArbeidsgiverpremie(builder.arbeidsgiver(expected).bygg()).isEqualTo(expected);
    }

    @Test
    public void skalPopulereAdministrasjonsgebyr() {
        final Premiebeloep expected = premiebeloep("kr 100");
        Assertions.assertAdministrasjonsgebyr(builder.administrasjonsgebyr(expected).bygg()).isEqualTo(expected);
    }
}