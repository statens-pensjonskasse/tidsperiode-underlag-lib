package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.UnderlagsperiodeBuilder;

import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java8.No;

/**
 * Stegdefinisjonar for å bygge opp underlagsperioder for bruk til avregning.
 * <br>
 * Hovedformålet med definisjonane er å gjere det mulig å verifisere {@link BeregningsRegel}ar sin oppførsel. Sidan
 * reglane er avhengige av underlagsperiodene sine annotasjonar, skal stegdefinisjonane i denne klassa tilrettelegge
 * for at ein kan populere underlagsperioder med grunnlagsdata som reglane er avhengig av.
 * <br>
 * Grunnlagsdata er ikkje det einaste reglane er avhengig av, dei kan og vere avhengig av andre beregningsreglar.
 * For å unngå at ein alltid må spesifisere opp alt av grunnlagsdata som alle involverte reglar treng, tilbyr
 * stegdefinisjonane muligheit for å hardkode verdiar som forskjellige reglar skal returnere.
 * <br>
 * <h6>Eksempel</h6>
 * I eit scenario ønskjer ein å verifisere oppførselen til regelen for beregning av pensjonspremie for ei underlagsperiode.
 * Beregningsregelen for pensjonspremie, er avhengig av beregningsregelen for pensjonsgivande lønn. For å kunne beregne
 * pensjonsgivande lønn, ville underlagsperioda måtte blitt populert med ein lang rekke grunnlagsdata som ikkje er direkte
 * relevante for beregningsreglen for pensjonspremie.
 * <br>
 * Ved å angi at pensjonsgivande lønn for perioda skal vere lik kr 10 000, slepp ein dermed å støye til scenariet med
 * masse oppsett som gjer det vanskelig å sjå essensen i kva scenariet sine eksempel prøver å spesifisere.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeDefinisjonar implements No {
    private final Map<String, Datatype<?>> mappers = new HashMap<>();

    private UnderlagsperiodeBuilder periode;

    public UnderlagsperiodeDefinisjonar() {
        // Alle datatyper som underlagsperioder må kunne annoteres med for å støtte beregningsregler for avregning
        Supports("Pensjonsgivende lønn", MaskineltGrunnlagRegel.class, KonverterFraTekst::pensjonsgivendeLoenn);
        Supports("Premiestatus", Premiestatus.class, Premiestatus::valueOf);
        Supports("Premiekategori", Premiekategori.class, Premiekategori::parse);
        Supports("Årsverk", AarsverkRegel.class, KonverterFraTekst::aarsverkRegel);
        Supports("Årsfaktor", AarsfaktorRegel.class, KonverterFraTekst::aarsfaktorRegel);

        // Språkdefinisjon for annotering av underlagsperioder
        Gitt("^en underlagsperiode med følgende innhold:$", (DataTable underlagsperioder) -> {
            assertThat(underlagsperioder.raw()).as("underlagsperioder").hasSize(2);
            assertThat(underlagsperioder.topCells()).as("underlagsperiode-kolonner").isNotEmpty();

            populerFra(underlagsperioder);
        });

    }

    @Before
    public void nyPeriode() {
        final Aarstall premieAar = new Aarstall(now().getYear());
        periode = new UnderlagsperiodeBuilder()
                .fraOgMed(premieAar.atStartOfYear())
                .tilOgMed(premieAar.atEndOfYear());
    }

    UnderlagsperiodeBuilder builder() {
        return periode;
    }

    private <T> void Supports(final String tittel, final Class<? extends T> annotasjonsType, final Function<String, T> mapper) {
        mappers.put(tittel.toLowerCase(), new Datatype<>(annotasjonsType, mapper));
    }

    private void populerFra(final DataTable underlagsperioder) {
        underlagsperioder
                .transpose()
                .asMap(String.class, String.class)
                .forEach(this::populerFraKolonne);
    }

    private void populerFraKolonne(final String tittel, final String verdi) {
        finnTypeForTittel(tittel).annoter(periode, verdi);
    }

    private Datatype<?> finnTypeForTittel(final String tittel) {
        return ofNullable(mappers.get(tittel.toLowerCase()))
                .orElseThrow(() -> ukjentDatatype(tittel));
    }

    private IllegalArgumentException ukjentDatatype(final String tittel) {
        return new IllegalArgumentException("'" + tittel + "'"
                + " ble ikke gjenkjent som en datatype en underlagsperiode kan inneholde.\n"
                + "En underlagsperiode kan inneholde følgende datatyper:\n"
                + mappers
                .keySet()
                .stream()
                .map(k -> "\t- " + k)
                .collect(joining("\n"))
        );
    }

    private static class Datatype<T> {
        private final Class<? extends T> annotasjon;
        private final Function<String, T> mapper;

        public Datatype(final Class<? extends T> annotasjon, final Function<String, T> mapper) {
            this.annotasjon = annotasjon;
            this.mapper = mapper;
        }

        public UnderlagsperiodeBuilder annoter(final UnderlagsperiodeBuilder builder, final String value) {
            return builder.med(annotasjon, mapper.apply(value));
        }
    }
}
