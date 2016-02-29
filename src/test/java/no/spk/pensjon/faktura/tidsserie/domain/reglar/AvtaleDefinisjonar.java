package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst.sannhetsverdi;
import static no.spk.pensjon.faktura.tidsserie.domain.tidsserie.MedlemsavtalarPeriode.medlemsavtalar;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst;
import no.spk.pensjon.faktura.tidsserie.domain.at.StillingAvtaler;
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

    private Map<AvtaleId, Avtale> avtaler = new HashMap<>();

    public AvtaleDefinisjonar() {
        Gitt("^(?:at )?avtalen for underlagsperioden ikke har noen produkter", () -> {
            annoterAvtale(tomDatatable());
        });
        Gitt("^(?:at )?avtalen for underlagsperioden har følgende produkt:", this::annoterAvtale);
        Gitt("^(?:at )?avtale (.+) har følgende produkt:", this::definerAvtale);
        Så("^er underlagsperioden fakturerbar for følgende produkt:$", this::assertErFakturerbar);
    }

    private void assertErFakturerbar(DataTable fakturerbareProdukt) {
        fakturerbareProdukt.asMaps(String.class, String.class)
                .stream()
                .forEach(m -> {
                    boolean erFakturerbar = sannhetsverdi(m.get("Er produktet fakturerbart?"));
                    Produkt produkt = Produkt.fraKode(m.get("Produkt"));

                    assertThat(
                            underlagsperiode().valgfriAnnotasjonFor(Avtale.class)
                                    .orElseThrow(() -> new IllegalStateException("Underlagsperioden har ingen avtale - er avtalen definert for perioden?"))
                                    .premiesatsFor(produkt)
                                    .map(Premiesats::erFakturerbar)
                                    .orElse(false)
                    )
                            .as("Er fakturerbar for " + produkt)
                            .isEqualTo(erFakturerbar);
                });
    }

    private DataTable tomDatatable() {
        return DataTable.create(singletonList(""), Locale.forLanguageTag("no"), "Ingen avtaler");
    }

    private Avtale definerAvtale(String avtalenummer, DataTable avtaledata) {
        final AvtaleId avtaleId = AvtaleId.valueOf(avtalenummer);
        Avtale avtale = lagAvtale(avtaleId, avtaledata).bygg();
        avtaler.computeIfPresent(avtale.id(), this::duplikatAvtale);
        avtaler.put(avtaleId, avtale);
        oppdaterMedlemsavtaler();
        return avtale;
    }

    private void oppdaterMedlemsavtaler() {
        final Underlagsperiode underlagsperiode = underlagsperiode();

        final MedlemsavtalarPeriode.Builder medlemsavtalebuilder = medlemsavtalar()
                .fraOgMed(underlagsperiode.fraOgMed())
                .tilOgMed(underlagsperiode.tilOgMed());

        periode.builder()
                .bygg()
                .valgfriAnnotasjonFor(StillingAvtaler.class)
                .map(StillingAvtaler::stillinger)
                .orElse(Stream.empty())
                .filter(sa -> avtaler.containsKey(sa.avtaleId()))
                .forEach(stillingsavtale ->
                        medlemsavtalebuilder.addAvtale(
                                stillingsavtale.stillingsforholdId(),
                                avtale(stillingsavtale.avtaleId())
                        )
                );

        periode.builder().annoter(Medlemsavtalar.class, medlemsavtalebuilder.bygg());
    }

    private Avtale avtale(AvtaleId avtaleId) {
        return ofNullable(avtaler.get(avtaleId))
                .orElseThrow(avtalenErIkkeDefinert(avtaleId));
    }

    private Supplier<IllegalStateException> avtalenErIkkeDefinert(AvtaleId avtaleId) {
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

        Avtale avtale = definerAvtale(String.valueOf(avtaleId.id()), avtaledata);
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


        //Jalla 1.8.0_11 type inferrence bug = if-else helvete.
        final boolean erKroner = satser.somKroner()
                .filter(v -> sumKronesats(v) > 0).isPresent();
        final boolean erProsent = satser.somProsent()
                .filter(v -> sumProsentsats(v) > 0).isPresent();

        if (erKroner) {
            builder.satser(satser.somKroner().get());
        } else if (erProsent) {
            builder.satser(satser.somProsent().get());
        } else {
            builder.satser(Satser.ingenSatser());
        }

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
}
