package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.at.KonverterFraTekst;
import no.spk.pensjon.faktura.tidsserie.domain.at.UnderlagsperiodeDefinisjonar;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.AvtaleBuilder;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.IngenSats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiesats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Sats;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Satser;
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

    public AvtaleDefinisjonar() {
        Gitt("^(?:at )?avtalen for underlagsperioden ikke har noen produkter", () -> {
            annoterAvtale(tomDatatable());
        });

        Gitt("^(?:at )?avtalen for underlagsperioden har følgende produkt:", this::annoterAvtale);

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

    private void annoterAvtale(DataTable avtaledata) {
        final AvtaleId avtaleId = underlagsperiode().valgfriAnnotasjonFor(AvtaleId.class)
                .orElseThrow(() -> new IllegalStateException("AvtaleId er ikke satt for underlagsperioden." +
                        "Avtaledefinisjoner kan kun benyttes for underlagsperioder som har annotert AvtaleId."));

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

        periode.builder().annoter(Avtale.class, avtale.bygg());
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

        builder.satser(satser);

        verdi("Produktinfo", produktlinje)
                .map(KonverterFraTekst::produktinfo)
                .forEach(builder::produktinfo);
        return builder;
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
