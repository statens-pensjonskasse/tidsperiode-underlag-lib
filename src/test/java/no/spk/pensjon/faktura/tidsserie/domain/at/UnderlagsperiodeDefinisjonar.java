package no.spk.pensjon.faktura.tidsserie.domain.at;

import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent.prosent;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avregning.AvregningsRegelsett;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Aksjonskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AktiveStillingar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.DeltidsjustertLoenn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Fastetillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Funksjonstillegg;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Grunnbeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Loennstrinn;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.LoennstrinnBeloep;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingskode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsprosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Variabletillegg;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsfaktorRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.AarsverkRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.DeltidsjustertLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErMedregningRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErPermisjonUtanLoennRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.ErUnderMinstegrensaRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.FaktureringsandelStatus;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.GruppelivsfaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.LoennstilleggRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MaskineltGrunnlagRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.MedregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.OevreLoennsgrenseRegel;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.PrognoseRegelsett;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.Regelsett;
import no.spk.pensjon.faktura.tidsserie.domain.reglar.YrkesskadefaktureringRegel;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;
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

    private Stream<String> verdi(String kolonnenavn, Map<String, String> map) {
        return ofNullable(map.get(kolonnenavn)).map(Stream::of).orElse(Stream.empty());
    }

    public UnderlagsperiodeDefinisjonar() {

        Supports("Premiestatus", Premiestatus.class, Premiestatus::valueOf);
        Supports("Premiekategori", Premiekategori.class, Premiekategori::parse);
        Supports("Stillingskode", Stillingskode.class, this::stillingskode);
        Supports("Årsverk", AarsverkRegel.class, KonverterFraTekst::aarsverkRegel);
        Supports("Årsfaktor", AarsfaktorRegel.class, KonverterFraTekst::aarsfaktorRegel);
        Supports("Stillingsprosent", Stillingsprosent.class, KonverterFraTekst::stillingsprosent);
        Supports("Ordning", Ordning.class, KonverterFraTekst::ordning);
        Supports("Yrkesskadeandel", YrkesskadefaktureringRegel.class, KonverterFraTekst::yrkesskadeandel);
        Supports("Gruppelivandel", GruppelivsfaktureringRegel.class, KonverterFraTekst::gruppelivsandel);
        Supports("Deltidsjustert lønn", DeltidsjustertLoenn.class, KonverterFraTekst::deltidsjustertLoenn);
        Supports("Lønnstrinn beløp", LoennstrinnBeloep.class, KonverterFraTekst::loennstrinnBeloep);
        Supports("Lønnstrinn", Loennstrinn.class, Loennstrinn::new);
        Supports("Grunnbeløp", Grunnbeloep.class, KonverterFraTekst::grunnbeloep);
        Supports("Avtale", AvtaleId.class, AvtaleId::valueOf);
        Supports("Stillingsforhold", StillingsforholdId.class, StillingsforholdId::valueOf);
        Supports("Faste tillegg", Fastetillegg.class, KonverterFraTekst::fasteTillegg);
        Supports("Variable tillegg", Variabletillegg.class, KonverterFraTekst::variableTillegg);
        Supports("Funksjonstillegg", Funksjonstillegg.class, KonverterFraTekst::funksjonsTillegg);

        SupportsBoolean("Er under minstegrensen", ErUnderMinstegrensaRegel.class);
        SupportsBoolean("Er medregning", ErMedregningRegel.class);
        SupportsBoolean("Er permisjon uten lønn", ErPermisjonUtanLoennRegel.class);

        SupportsBeloep("Pensjonsgivende lønn", MaskineltGrunnlagRegel.class);
        SupportsBeloep("Regel deltidsjustert lønn", DeltidsjustertLoennRegel.class);
        SupportsBeloep("Lønnstillegg", LoennstilleggRegel.class);
        SupportsBeloep("Medregning", MedregningsRegel.class);
        SupportsBeloep("Øvre lønnsgrense", OevreLoennsgrenseRegel.class);

        // Språkdefinisjon for annotering av underlagsperioder
        Gitt("^en underlagsperiode med følgende innhold:$", (DataTable underlagsperioder) -> {
            assertThat(underlagsperioder.raw()).as("underlagsperioder").hasSize(2);
            assertThat(underlagsperioder.topCells()).as("underlagsperiode-kolonner").isNotEmpty();

            populerFra(underlagsperioder);
        });
        Gitt("^underlagsperioden sin fra og med-dato er ([0-9\\.]{10})$", this::fraOgMed);
        Gitt("^underlagsperioden sin til og med-dato er ([0-9\\.]{10})$", this::tilOgMed);
        Gitt("^underlagsperioden benytter regler for avregning$", this::avregningsreglar);
        Gitt("^underlagsperioden benytter regler for prognose$", this::prognoseregler);
        Gitt("^underlagsperioden er koblet til følgende aktive stillinger:$", this::aktiveStillinger);
        Så("^er stillingen (.+) minstegrensen.?$", this::assertErOverEllerUnderMinstegrensen);
        Så("^har stillingsforhold (.+) faktureringsandel for YSK lik (.+) i perioden$", this::assertFaktureringsandelForYSK);
        Så("^har stillingsforhold (.+) faktureringsandel for GRU lik (.+) i perioden$", this::assertFaktureringsandelForGRU);
    }

    @Before
    public void nyPeriode() {
        final Aarstall premieAar = new Aarstall(now().getYear());
        periode = new UnderlagsperiodeBuilder()
                .fraOgMed(premieAar.atStartOfYear())
                .tilOgMed(premieAar.atEndOfYear());
    }

    public UnderlagsperiodeBuilder builder() {
        return periode;
    }

    private <T> void Supports(final String tittel, final Class<? extends T> annotasjonsType, final Function<String, T> mapper) {
        mappers.put(tittel.toLowerCase(), new Datatype<>(annotasjonsType, mapper));
    }

    private <T extends BeregningsRegel<Boolean>> void SupportsBoolean(final String tittel, final Class<T> annotasjonsType) {
        Supports(tittel, annotasjonsType, KonverterFraTekst.booleanRegel(annotasjonsType));
    }

    private <T extends BeregningsRegel<Kroner>> void SupportsBeloep(final String tittel, final Class<T> annotasjonsType) {
        Supports(tittel, annotasjonsType, KonverterFraTekst.beloepRegel(annotasjonsType));
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

    private Stillingskode stillingskode(final String stillingskode) {
        switch (stillingskode.toLowerCase()) {
        case "farmasøyt":
            return Stillingskode.K_STIL_APO_APOTEKER;
        case "annen":
            return Stillingskode.K_STIL_APO_BUD;
        default:
            try {
                return Stillingskode.parse(stillingskode);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(stillingskode + " er ikkje ei gyldig verdi  lovlige verdiar er: farmasøyt, annen eller et tall.");
            }
        }
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

    private void assertErOverEllerUnderMinstegrensen(final String resultat) {
        final Underlagsperiode periode = this.periode.bygg();
        assertThat(periode.beregn(ErUnderMinstegrensaRegel.class) ? "under" : "over")
                .as(
                        "er stillingen under minstegrensen i perioden "
                                + periode
                                + " når stillingsstørrelsen er "
                                + periode.valgfriAnnotasjonFor(Stillingsprosent.class).map(Object::toString).orElse("ukjent") + "?"
                                + resultat
                )
                .isEqualTo(overEllerUnder(resultat));
    }

    private void assertFaktureringsandelForYSK(final String stillingsforhold, final String andel) {
        assertFaktureringsandel(YrkesskadefaktureringRegel.class, "YSK", stillingsforhold, andel);
    }

    private void assertFaktureringsandelForGRU(final String stillingsforhold, final String andel) {
        assertFaktureringsandel(GruppelivsfaktureringRegel.class, "GRU", stillingsforhold, andel);
    }

    private void assertFaktureringsandel(
            Class<? extends BeregningsRegel<FaktureringsandelStatus>> regel,
            final String produkt,
            final String stillingsforhold,
            final String andel) {
        final Underlagsperiode periode;
        try {
            periode = this.periode
                    .annoter(regel, regel.newInstance())
                    .bygg();
            final FaktureringsandelStatus fordeling = periode.beregn(regel);

            assertThat(fordeling.stillingsforhold())
                    .as(
                            "forventet stillingsforhold for grunnlag for YSK"
                    )
                    .isEqualTo(StillingsforholdId.valueOf(stillingsforhold));

            final Prosent forventetAndel = prosent(andel);
            assertThat(fordeling.andel().equals(forventetAndel, 3))
                    .as(
                            "grunnlag for %s for underlagsperioden er forventet å være %s men var %s",
                            produkt,
                            forventetAndel.toString(),
                            fordeling.andel().toString()
                    )
                    .isTrue();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private String overEllerUnder(final String resultat) {
        switch (resultat.toLowerCase()) {
        case "over":
        case "under":
            return resultat.toLowerCase();
        default:
            throw new IllegalArgumentException(resultat + " er ikkje ei gyldig verdi når du skal sjekke om ein er over eller under minstegrensa, lovlige verdiar er: over, under");
        }
    }

    private void fraOgMed(final String fraOgMed) {
        periode.fraOgMed(dato(fraOgMed));
    }

    private void tilOgMed(final String tilOgMed) {
        periode.tilOgMed(dato(tilOgMed));
    }

    private void aktiveStillinger(DataTable stillingsdefinisjoner) {
        List<AktiveStillingar.AktivStilling> aktiveStillinger = mapAktiveStillinger(
                stillingsdefinisjoner,
                (stillingsid, stilling) -> new AktiveStillingar.AktivStilling(
                        StillingsforholdId.valueOf(stillingsid),
                        verdi("Stillingsprosent", stilling).map(Prosent::new).findAny(),
                        verdi("Aksjonskode", stilling).map(Aksjonskode::valueOf).findAny()
                ))
                .collect(toList());

        List<StillingAvtale> stillingAvtaler = mapAktiveStillinger(
                stillingsdefinisjoner,
                (stillingsid, stilling) -> new StillingAvtale(
                        StillingsforholdId.valueOf(stillingsid),
                        verdi("Avtale", stilling).map(AvtaleId::valueOf).findAny().get()
                ))
                .collect(toList());

        periode.annoter(AktiveStillingar.class, aktiveStillinger::stream);
        periode.annoter(StillingAvtaler.class, stillingAvtaler::stream);
    }

    private <T> Stream<T> mapAktiveStillinger(DataTable aktiveStillinger, BiFunction<String, Map<String, String>, T> mapStilling) {
        return aktiveStillinger
                .asMaps(String.class, String.class)
                .stream()
                .flatMap(stilling -> verdi("Stillingsforhold", stilling)
                        .map(stillingsid -> mapStilling.apply(stillingsid, stilling))
                );
    }

    private void avregningsreglar() {
        final AvregningsRegelsett regler = new AvregningsRegelsett();
        annoterRegler(regler);
    }

    private void prognoseregler() {
        final PrognoseRegelsett regler = new PrognoseRegelsett();
        annoterRegler(regler);
    }

    private void annoterRegler(Regelsett regler) {
        regler.reglar().filter(r -> r.overlapper(periode)).forEach(r -> r.annoter(periode));
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
