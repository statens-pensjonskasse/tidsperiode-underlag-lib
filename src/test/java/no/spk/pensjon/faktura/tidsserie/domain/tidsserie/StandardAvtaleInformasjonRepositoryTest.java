package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.spk.pensjon.faktura.tidsserie.Datoar.dato;
import static no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleperiode.avtaleperiode;
import static no.spk.pensjon.faktura.tidsserie.domain.testdata.ObjectMother.enAvtaleversjon;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverdataperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Arbeidsgiverperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.ArbeidsgiverId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Orgnummer;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import org.junit.Test;

/**
 * @author Snorre E. Brekke - Computas
 */
public class StandardAvtaleInformasjonRepositoryTest {
    StandardAvtaleInformasjonRepository repository;
    Map<Class<?>, List<Tidsperiode<?>>> perioder = new HashMap<>();

    @Test
    public void tomtAvtalerepositySkalIkkeFinneAvtale() throws Exception {
        repository = new StandardAvtaleInformasjonRepository(perioder);
        assertThat(repository.finn(new AvtaleId(1L)).count()).isZero();
    }

    @Test
    public void avtaleRepositoryInneholderOverlappendeArbeidsgiverperioder() throws Exception {
        AvtaleId avtaleId = new AvtaleId(1L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(10L);
        Orgnummer orgnummer = new Orgnummer(123456789L);
        LocalDate fraDato = dato("2010.01.01");
        Optional<LocalDate> tildato = empty();

        Avtaleversjon avtaleversjon = enAvtaleversjon(avtaleId).fraOgMed(fraDato).tilOgMed(tildato).bygg();
        Avtaleperiode avtaleperiode = avtaleperiode(avtaleId)
                .fraOgMed(fraDato)
                .tilOgMed(tildato)
                .arbeidsgiverId(arbeidsgiverId)
                .bygg();
        Arbeidsgiverperiode arbeidsgiverperiode = new Arbeidsgiverperiode(fraDato, tildato, arbeidsgiverId);
        Arbeidsgiverdataperiode arbeidsgiverdataperiode = new Arbeidsgiverdataperiode(fraDato, tildato, orgnummer, arbeidsgiverId);

        put(perioder, avtaleversjon, avtaleperiode, arbeidsgiverperiode, arbeidsgiverdataperiode);

        repository = new StandardAvtaleInformasjonRepository(perioder);
        List<Tidsperiode<?>> fantPerioder = repository.finn(avtaleId).collect(toList());

        assertThat(fantPerioder).containsExactly(avtaleversjon, avtaleperiode, arbeidsgiverperiode, arbeidsgiverdataperiode);
    }

    @Test
    public void avtaleRepositoryUtelaterArbeidsgiverperioderSomIkkHarRiktigArbeidsgiverId() throws Exception {
        AvtaleId avtaleId = new AvtaleId(1L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(10L);
        LocalDate fraDato = dato("2010.01.01");
        Optional<LocalDate> tildato = empty();

        Avtaleversjon avtaleversjon = enAvtaleversjon(avtaleId).fraOgMed(fraDato).tilOgMed(tildato).bygg();
        Avtaleperiode avtaleperiode = avtaleperiode(avtaleId)
                .fraOgMed(fraDato)
                .tilOgMed(tildato)
                .arbeidsgiverId(arbeidsgiverId)
                .bygg();
        Arbeidsgiverperiode arbeidsgiverperiode = new Arbeidsgiverperiode(fraDato, empty(), new ArbeidsgiverId(11L));

        put(perioder, avtaleversjon, avtaleperiode, arbeidsgiverperiode);

        repository = new StandardAvtaleInformasjonRepository(perioder);
        List<Tidsperiode<?>> fantPerioder = repository.finn(avtaleId).collect(toList());

        assertThat(fantPerioder).containsExactly(avtaleversjon, avtaleperiode);
    }

    @Test
    public void avtaleRepositoryUtelaterArbeidsgiverperioderDersomAvtaleperiodeMangler() throws Exception {
        AvtaleId avtaleId = new AvtaleId(1L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(10L);
        LocalDate fraDato = dato("2010.01.01");
        Optional<LocalDate> tildato = empty();

        Avtaleversjon avtaleversjon = enAvtaleversjon(avtaleId).fraOgMed(fraDato).tilOgMed(tildato).bygg();
        Arbeidsgiverperiode arbeidsgiverperiode = new Arbeidsgiverperiode(fraDato, empty(), arbeidsgiverId);

        put(perioder, avtaleversjon, arbeidsgiverperiode);

        repository = new StandardAvtaleInformasjonRepository(perioder);
        List<Tidsperiode<?>> fantPerioder = repository.finn(avtaleId).collect(toList());

        assertThat(fantPerioder).containsExactly(avtaleversjon);
    }

    @Test
    public void avtaleRepositoryUtelaterPerioderSomIkkeErKnyttetTilAngittAvtale() throws Exception {
        AvtaleId avtaleId = new AvtaleId(1L);
        ArbeidsgiverId arbeidsgiverId = new ArbeidsgiverId(10L);
        LocalDate fraDato = dato("2010.01.01");
        Optional<LocalDate> tildato = empty();

        Avtaleversjon avtaleversjon = enAvtaleversjon(avtaleId).fraOgMed(fraDato).tilOgMed(tildato).bygg();
        Avtaleperiode avtaleperiode = avtaleperiode(avtaleId)
                .fraOgMed(fraDato)
                .tilOgMed(tildato)
                .arbeidsgiverId(arbeidsgiverId)
                .bygg();
        Arbeidsgiverperiode arbeidsgiverperiode = new Arbeidsgiverperiode(fraDato, tildato, arbeidsgiverId);

        AvtaleId utelattAvtaleId = new AvtaleId(2L);
        ArbeidsgiverId utelattArbeidsgiverId = new ArbeidsgiverId(20L);
        Avtaleversjon avtaleversjonUtelatt = enAvtaleversjon(utelattAvtaleId).fraOgMed(fraDato).tilOgMed(tildato).bygg();
        Avtaleperiode avtaleperiodeUtelatt = avtaleperiode(utelattAvtaleId)
                .fraOgMed(fraDato)
                .tilOgMed(tildato)
                .arbeidsgiverId(arbeidsgiverId)
                .bygg();
        Arbeidsgiverperiode arbeidsgiverperiodeUtelatt = new Arbeidsgiverperiode(fraDato, tildato, utelattArbeidsgiverId);

        put(perioder, avtaleversjon, avtaleperiode, arbeidsgiverperiode,
                avtaleversjonUtelatt, avtaleperiodeUtelatt, arbeidsgiverperiodeUtelatt);

        repository = new StandardAvtaleInformasjonRepository(perioder);
        List<Tidsperiode<?>> fantPerioder = repository.finn(avtaleId).collect(toList());

        assertThat(fantPerioder).containsExactly(avtaleversjon, avtaleperiode, arbeidsgiverperiode);
    }

    private void put(Map<Class<?>, List<Tidsperiode<?>>> perioder, Tidsperiode<?>... leggTilPerioder) {
        perioder.putAll(Arrays.stream(leggTilPerioder)
                .collect(Collectors.groupingBy(Object::getClass)));
    }
}