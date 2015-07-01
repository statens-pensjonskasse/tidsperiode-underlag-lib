package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverrelatertperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtalerelatertperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Kundedataperiode;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

/**
 * Gitt en map med tidsperioder gruppert per klasse, organiserer StandardAvtaleInformasjonRepository tidsperiodene vha @{@link #periodeGrupper(Map)}
 * som er relevante for avtale- og arbeidsgiver-informasjon internt, og eksponerer disse via {@link #finn(AvtaleId)}.
 * @author Snorre E. Brekke - Computas
 */
public class StandardAvtaleInformasjonRepository implements AvtaleinformasjonRepository {
    private Optional<Map<AvtaleId, List<Avtalerelatertperiode<?>>>> avtalar = Optional.empty();
    private Optional<Map<ArbeidsgiverId, List<Arbeidsgiverrelatertperiode<?>>>> arbeidsgivere = Optional.empty();

    /**
     * Organiserer tidsperiodene som er relevante for avtale- og arbeidsgiver-informasjon internt, og eksponerer disse via {@link #finn(AvtaleId)}.
     *
     * @param perioder som skal organiseres
     */
    public void periodeGrupper(Map<Class<?>, List<Tidsperiode<?>>> perioder) {
        requireNonNull(perioder, "perioder kan ikke være null");
        avtalar.ifPresent(a -> throwAlreadyInitializedException());

        avtalar = Optional.of(grupperAvtaleperioder(perioder));
        arbeidsgivere = Optional.of(grupperArbeidsgiverperioder(perioder));
    }

    /**
     * Hent ut alle avtaledata for avtalen.
     *
     * @param avtale avtalen som avtaledata skal hentast ut for
     * @return alle tidsperiodiserte avtaledata som er tilknytta avtalen i grunnlagsdatane
     * @see AvtaleinformasjonRepository
     * @see Avtaleversjon
     * @see Avtaleprodukt
     * @see Kundedataperiode
     * @see Arbeidsgiverperiode
     */
    @Override
    public Stream<Tidsperiode<?>> finn(final AvtaleId avtale) {
        Stream<Tidsperiode<?>> avtaleStream = avtalar
                .orElseThrow(getNotInitializedException())
                .getOrDefault(avtale, emptyList()).stream().map((Tidsperiode<?> p) -> p);

        Stream<Tidsperiode<?>> arbeidsgiverStream = avtalar
                .orElseThrow(getNotInitializedException())
                .getOrDefault(avtale, emptyList())
                .stream()
                .filter(a -> a instanceof Avtaleperiode)
                .map(a -> ((Avtaleperiode) a).arbeidsgiverId())
                .map(id -> arbeidsgivere.get().getOrDefault(id, emptyList()).stream())
                .flatMap(identity());

        return Stream.of(avtaleStream, arbeidsgiverStream).flatMap(identity());
    }

    private Supplier<IllegalStateException> throwAlreadyInitializedException() {
        throw new IllegalStateException("periodeGrupper() er allerede blitt kalt, og kan bare kalles en gang.");
    }

    private Supplier<IllegalStateException> getNotInitializedException() {
        return () -> new IllegalStateException("periodeGrupper() er ikke blitt kalt, og må kalles før finn kan brukes.");
    }

    private Map<AvtaleId, List<Avtalerelatertperiode<?>>> grupperAvtaleperioder(Map<Class<?>, List<Tidsperiode<?>>> perioder) {
        final Stream<Avtaleversjon> versjoner = perioderAvType(perioder, Avtaleversjon.class);
        final Stream<Avtaleprodukt> produkter = perioderAvType(perioder, Avtaleprodukt.class);
        final Stream<Avtaleperiode> avtaleperioder = perioderAvType(perioder, Avtaleperiode.class);
        return Stream.of(versjoner, produkter, avtaleperioder)
                .flatMap(identity())
                .map((Avtalerelatertperiode<?> p) -> p)
                .collect(
                        groupingBy(Avtalerelatertperiode::avtale)
                );
    }

    private Map<ArbeidsgiverId, List<Arbeidsgiverrelatertperiode<?>>> grupperArbeidsgiverperioder(Map<Class<?>, List<Tidsperiode<?>>> perioder) {
        final Stream<Kundedataperiode> kundeperioder = perioderAvType(perioder, Kundedataperiode.class);
        final Stream<Arbeidsgiverperiode> arbeidsgiverperidoer = perioderAvType(perioder, Arbeidsgiverperiode.class);
        return Stream.of(arbeidsgiverperidoer, kundeperioder)
                .flatMap(identity())
                .collect(
                        groupingBy(Arbeidsgiverrelatertperiode::arbeidsgiver)
                );
    }

    <T> Stream<T> perioderAvType(Map<Class<?>, List<Tidsperiode<?>>> perioder, final Class<T> type) {
        return perioder.getOrDefault(type, emptyList()).stream().map(type::cast);
    }
}
