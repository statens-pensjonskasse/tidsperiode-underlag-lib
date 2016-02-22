package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.valueOf;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst;
import no.spk.pensjon.faktura.tidsserie.domain.at.UnderlagsperiodeDefinisjonar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.IngenSats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Kroner;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Medlemsavtalar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Sats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

import cucumber.api.DataTable;
import cucumber.api.java8.No;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Snorre E. Brekke - Computas
 */
@ContextConfiguration(classes = RegelConfiguration.class)
public class AvtaleDefinisjonar implements No {

    @Autowired
    private UnderlagsperiodeDefinisjonar periode;

    private Map<StillingsforholdId, AvtaleId> medlemsavtaler = new HashMap<>();

    private Map<AvtaleId, Avtale> avtaler = new HashMap<>();

    public AvtaleDefinisjonar() {
        Gitt("^(?:at )?avtalen for underlagsperioden ikke har noen produkter", () -> {
            annoterAvtale(tomDatatable());
        });

        Gitt("^(?:at )?avtalen for underlagsperioden har følgende produkt:", this::annoterAvtale);

        Gitt("^(?:at )?avtale (.+) har følgende produkt:", this::registrerAvtale);

        Gitt("^(?:at )?stillingsforhold (.+) tilhører avtale (.+)", this::leggTilMedlemsavtale);

        Så("^(?:er underlagsperioden merket som )?([Ii]kke fakturerbar|[Ff]akturerbar) for (.+)$", (String kanskjeFakturerbar, String produktkode) -> {
            boolean erFakturerbar = kanskjeFakturerbar.matches("^[Ff]akturerbar$");
            Produkt produkt = Produkt.fraKode(produktkode);

            assertThat(
                    underlagsperiode().valgfriAnnotasjonFor(Avtale.class)
                            .orElseThrow(() -> new IllegalStateException("Underlagsperioden har ingen avtale - er avtalen definert for perioden?"))
                            .premiesatsFor(produkt)
                            .map(Premiesats::erFakturerbar)
                            .orElse(false)
            )
                    .as("Er fakturerbar for " + produktkode)
                    .isEqualTo(erFakturerbar);

        });

    }

    private DataTable tomDatatable() {
        return DataTable.create(singletonList(""), Locale.forLanguageTag("no"), "Ingen avtaler");
    }

    private void registrerAvtale(String avtaleId, DataTable avtaledata) {
        definerAvtale(AvtaleId.valueOf(avtaleId), avtaledata);
    }

    private Avtale definerAvtale(AvtaleId avtaleId, DataTable avtaledata) {
        Avtale avtale = lagAvtale(avtaleId, avtaledata).bygg();
        avtaler.computeIfPresent(avtale.id(), this::duplikatAvtale);
        avtaler.put(avtaleId, avtale);
        return avtale;
    }

    private AvtaleId avtaleid(String avtaleId) {
        return AvtaleId.valueOf(avtaleId);
    }


    private void leggTilMedlemsavtale(String stillingsforhold, String avtaleId) {
        final Avtale avtale = ofNullable(avtaler.get(avtaleid(avtaleId)))
                .orElseThrow(avtalenErIkkeDefinert(avtaleId));

        StillingsforholdId stillingsforholdId = valueOf(stillingsforhold);
        medlemsavtaler.computeIfPresent(stillingsforholdId, this::duplikatStillingsforhold);
        medlemsavtaler.put(stillingsforholdId, avtale.id());

        final Underlagsperiode underlagsperiode = underlagsperiode();

        final MedlemsavtalarPeriode.Builder medlemsavtalebuilder = medlemsavtalar()
                .fraOgMed(underlagsperiode.fraOgMed())
                .tilOgMed(underlagsperiode.tilOgMed());

        medlemsavtaler
                .entrySet()
                .forEach(entry -> medlemsavtalebuilder.addAvtale(
                        entry.getKey(),
                        avtaler.get(entry.getValue())
                        )
                );

        periode.builder().annoter(Medlemsavtalar.class, medlemsavtalebuilder.bygg());
    }

    private Supplier<IllegalStateException> avtalenErIkkeDefinert(String avtaleId) {
        return () -> new IllegalStateException(
                "Ingen avtale med avtaleId " +
                        avtaleId + " er definert. " +
                        "Avtaler må defineres før de kan kobles til stillingsforhold. " +
                        alleredeDefinerteAvtaler()
        );
    }

    private Avtale duplikatAvtale(AvtaleId id, Avtale avtale) {
        throw new IllegalStateException(
                "Avtale med id " + id + " er allerede registrert." +
                        "Forsøkte å registrere med avtale " + avtale + ". " +
                        alleredeDefinerteAvtaler()
        );
    }

    private String alleredeDefinerteAvtaler() {
        return "Avtaler som allerede er definert: " +
                avtaler
                        .values()
                        .stream()
                        .map(Object::toString)
                        .collect(joining("\n"));
    }

    private void annoterAvtale(DataTable avtaledata) {
        final AvtaleId avtaleId = underlagsperiode().valgfriAnnotasjonFor(AvtaleId.class)
                .orElseThrow(harIkkeAvtaleid());

        Avtale avtale = definerAvtale(avtaleId, avtaledata);
        periode.builder().annoter(Avtale.class, avtale);
    }

    private Supplier<IllegalStateException> harIkkeAvtaleid() {
        return () -> new IllegalStateException("AvtaleId er ikke satt for underlagsperioden." +
                "Avtaledefinisjoner kan kun benyttes for underlagsperioder som har annotert AvtaleId.");
    }

    private AvtaleBuilder lagAvtale(AvtaleId avtaleId, DataTable avtaledata) {
        AvtaleBuilder avtale = Avtale.avtale(avtaleId);

        avtaledata
                .asMaps(String.class, String.class)
                .stream()
                .flatMap(produktlinje -> verdi("Produkt", produktlinje)
                        .map(Produkt::fraKode)
                        .map(Premiesats::premiesats)
                        .map(builder -> populerPremiesats(produktlinje, builder))
                        .map(Premiesats.Builder::bygg)
                ).forEach(avtale::addPremiesats);
        return avtale;
    }

    private Underlagsperiode underlagsperiode() {
        return periode.builder().bygg();
    }

    private Premiesats.Builder populerPremiesats(Map<String, String> produktlinje, Premiesats.Builder builder) {
        final Satser<?> satser = new Satser<>(
                sats("Arbeidsgiverpremie", produktlinje),
                sats("Medlemspremie", produktlinje),
                sats("Administrasjonsgebyr", produktlinje)
        );

        final Stream<Satser<? extends Sats>> satserStream = Stream.of(
                satser.somKroner()
                        .filter(v -> sumKronesats(v) > 0),
                satser.somProsent()
                        .filter(v -> sumProsentsats(v) > 0)
        )
                .filter(Optional::isPresent)
                .map(Optional::get);
        final Satser<?> nullSumSatserSomIngenSatser = satserStream
                .findAny()
                .orElse(Satser.ingenSatser());

        builder.satser(nullSumSatserSomIngenSatser);

        verdi("Produktinfo", produktlinje)
                .map(KonverterFraTekst::produktinfo)
                .forEach(builder::produktinfo);
        return builder;
    }

    private double sumProsentsats(Satser<Prosent> v) {
        return v.arbeidsgiverpremie()
                .plus(v.medlemspremie())
                .plus(v.administrasjonsgebyr()
                ).toDouble();
    }

    private long sumKronesats(Satser<Kroner> v) {
        return v.arbeidsgiverpremie()
                .plus(v.medlemspremie())
                .plus(v.administrasjonsgebyr()
                ).verdi();
    }

    private Stream<String> verdi(String tittel, Map<String, String> produkt) {
        return ofNullable(produkt.get(tittel)).map(Stream::of).orElse(Stream.empty());
    }

    private Sats sats(String tittel, Map<String, String> produkt) {
        return verdi(tittel, produkt).map(
                s -> s.contains("kr") ?
                        KonverterFraTekst.beloep(s) :
                        Prosent.prosent(s)

        )
                .findFirst()
                .orElse(IngenSats.sats());
    }

    private AvtaleId duplikatStillingsforhold(StillingsforholdId id, AvtaleId avtaleId) {
        throw new IllegalStateException(
                "Forsøkte å koble stillingsforhold med id " + id +
                        " til avtaleid " + avtaleId +
                        ", stillingsforholdet er allerede koblet til avtale en avtale." +
                        "Allerede registrerte medlemsavtaler: " +
                        medlemsavtaler
                                .values()
                                .stream()
                                .map(Object::toString)
                                .collect(joining("\n"))
        );
    }
}
