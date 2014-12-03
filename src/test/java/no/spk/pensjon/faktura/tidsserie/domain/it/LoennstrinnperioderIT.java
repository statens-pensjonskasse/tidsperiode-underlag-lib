package no.spk.pensjon.faktura.tidsserie.domain.it;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StatligLoennstrinnperiode;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.StatligLoennstrinnperiodeOversetter;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder.grupper;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrasjonstestar av lønnstrinngrupperinga.
 *
 * @author Tarjei Skorgenes
 */
public class LoennstrinnperioderIT {
    @ClassRule
    public final static EksempelDataForLoennstrinn data = new EksempelDataForLoennstrinn();

    private final StatligLoennstrinnperiodeOversetter oversetter = new StatligLoennstrinnperiodeOversetter();

    /**
     * Verifiserer at grupperinga ser på alle
     * {@link StatligLoennstrinnperiode statlige lønnstrinnperioder} sine tidsperioder
     * og slår alle med samme frå og med- og til og med-dato saman til ein ny instans av
     * {@link no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Loennstrinnperioder}.
     */
    @Test
    public void skalGruppereOgGenerereEinInstansAvLoennstrinnperioderPrUnikTidsperiode() {
        final List<Loennstrinnperioder> grupper = grupper(statligeLoennstrinn()).collect(toList());
        assertThat(grupper)
                .as("statlige lønnstrinnperioder gruppert på tidsperiode")
                .hasSize(69); // Antall unike perioder i CSV-fila
        assertThat(grupper.get(0).fraOgMed()).isEqualTo(dato("1948.01.01"));
        assertThat(grupper.get(0).tilOgMed()).isEqualTo(of(dato("1952.03.31")));
        assertThat(grupper.get(0).size()).isEqualTo(21);

        assertThat(grupper.get(68).fraOgMed()).isEqualTo(dato("2014.05.01"));
        assertThat(grupper.get(68).size()).isEqualTo(83);
    }

    /**
     * Verifiserer at det ikkje blir kasta nokon feil viss oppslag av lønnstrinn frå ei gruppering, ikkje finn
     * ei lønnstrinnperiode for lønnstrinnet.
     */
    @Test
    public void skalIkkjeFeileDersomDetIkkjeEksistererNokonLoennstrinnperiodeForEitLoennstrinnVedOppslagAvLoenn() {
        assertLoenn(999, dato("2000.01.17")).isEqualTo(empty());
        assertLoenn(0, dato("2099.01.17")).isEqualTo(empty());
        assertLoenn(-42, dato("2123.01.17")).isEqualTo(empty());
    }

    /**
     * Verifiserer at korrekt lønn blir slått opp frå grupperinga for eit par tilfeldig valgte lønnstrinn.
     */
    @Test
    public void skalFinneOenskaPeriodeVedOppslagPaaLoennstrinn() {
        assertLoenn(53, dato("2005.08.15")).isEqualTo(loenn(372_000));
        assertLoenn(56, dato("2006.04.31")).isEqualTo(loenn(392_100));
        assertLoenn(18, dato("2014.04.31")).isEqualTo(loenn(260_000));
    }

    /**
     * Grupperer lønnstrinna pr periode og forsøker å finne lønn for det angitte lønnstrinnet
     * frå grupperinga som overlappar <code>dato</code> og genererer ein assertion for lønnstrinnbeløpet.
     *
     * @param loennstrinn lønnstrinnet som lønn skal slått opp for
     * @param dato        datoen lønna skal bli slått opp for
     * @return ein ny assertion for lønna for lønnstrinnet på den aktuelle datoen
     */
    private AbstractObjectAssert<?, Optional<LoennstrinnBeloep>> assertLoenn(
            final int loennstrinn, final LocalDate dato) {
        return assertThat(
                grupper(statligeLoennstrinn())
                        .filter(p -> p.overlapper(dato))
                        .findFirst()
                        .map(p -> p.loennFor(new Loennstrinn(loennstrinn)))
                        .get()
        ).as("gjeldande lønn for lønnstrinn " + loennstrinn + " pr " + dato);
    }

    private Stream<StatligLoennstrinnperiode> statligeLoennstrinn() {
        return data
                .stream()
                .filter(oversetter::supports)
                .map(oversetter::oversett);
    }

    private static Optional<LoennstrinnBeloep> loenn(int beloep) {
        return of(new LoennstrinnBeloep(new Kroner(beloep)));
    }
}
