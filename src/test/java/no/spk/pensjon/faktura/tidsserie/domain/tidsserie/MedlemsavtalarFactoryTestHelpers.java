package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId.avtaleId;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId.stillingsforhold;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Produkt;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.medlemsdata.Avtalekoblingsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Observasjonsperiode;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractObjectAssert;

class MedlemsavtalarFactoryTestHelpers {
    private final Map<AvtaleId, List<Tidsperiode<?>>> avtaleinformasjon = new HashMap<>();

    static AbstractBooleanAssert<?> assertBetalarTilSPKFor(
            final MedlemsavtalarPeriode periode, final StillingsforholdId stilling, final Produkt produkt) {
        return assertThat(periode.betalarTilSPKFor(stilling, produkt))
                .as("skal " + stilling + " betale premie for " + produkt + " til SPK?\nPeriode:\n" + periode);
    }

    static AbstractObjectAssert<?, Premiestatus> assertPremiestatus(
            final MedlemsavtalarPeriode periode, final StillingsforholdId stilling) {
        final Avtale avtale = periode.avtaleFor(stilling);
        return assertThat(avtale.premiestatus())
                .as("premiestatus for avtale tilknytta " + stilling + "\nPeriode:\n" + periode);
    }

    static Avtalekoblingsperiode eiAvtalekobling(final StillingsforholdId stilling) {
        return new Avtalekoblingsperiode(
                tidenesMorgen(),
                empty(),
                stilling,
                enAvtale(),
                eiOrdning()
        );
    }

    List<MedlemsavtalarPeriode> periodiser(final Collection<Avtalekoblingsperiode> koblingar) {
        return new MedlemsavtalarFactory()
                .overstyr(this::finnAvtale)
                .periodiser(koblingar.stream(), observasjonsperiode())
                .collect(toList());
    }

    private Stream<Tidsperiode<?>> finnAvtale(final AvtaleId avtale) {
        return avtaleperioderFor(avtale).stream();
    }

    List<Tidsperiode<?>> avtaleperioderFor(AvtaleId avtale) {
        final List<Tidsperiode<?>> info = avtaleinformasjon.getOrDefault(
                avtale,
                new ArrayList<>()
        );
        avtaleinformasjon.put(avtale, info);
        return info;
    }

    static StillingsforholdId eiStilling() {
        return stillingsforhold(1L);
    }

    static AvtaleId enAvtale() {
        return avtaleId(123456L);
    }

    static Ordning eiOrdning() {
        return Ordning.SPK;
    }

    static Observasjonsperiode observasjonsperiode() {
        return new Observasjonsperiode(
                tidenesMorgen(),
                endOfTime()
        );
    }

    static LocalDate endOfTime() {
        return dato("2099.12.31");
    }

    static LocalDate tidenesMorgen() {
        return dato("1917.01.01");
    }
}
