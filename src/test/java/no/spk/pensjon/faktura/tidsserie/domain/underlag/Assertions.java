package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.Aarstall;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Stillingsendring;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;
import org.assertj.core.api.AbstractListAssert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class Assertions {
    /**
     * @see #assertUnderlagsperioder(java.util.Collection, java.util.function.Predicate)
     * @see #harKobling(Class)
     * @see java.util.function.Predicate#negate()
     */
    public static AbstractListAssert<?, ? extends List<Underlagsperiode>, Underlagsperiode> assertUnderlagsperioderUtanKoblingTil(
            final Map<StillingsforholdId, Underlag> underlagene, final Class<? extends Tidsperiode> type) {
        return assertUnderlagsperioder(underlagene.values(), harKobling(type).negate())
                .as("underlagsperioder utan kobling til " + type.getSimpleName());
    }

    /**
     * Opprettar ein ny assertion som opererer på samlinga av alle underlagsperioder i underlaga som blir matcha av predikatet.
     *
     * @param underlag ei samling underlag som underlagsperiodene blir henta frå
     * @param predikat predikatet som styrer kva for nokon underlagsperioder som blir henta ut og lagt inn i asserten
     * @return ein ny assert med alle underlagsperiodene som matchar predikatet
     */
    public static AbstractListAssert<?, ? extends List<Underlagsperiode>, Underlagsperiode> assertUnderlagsperioder(
            final Collection<Underlag> underlag, final Predicate<Underlagsperiode> predikat) {
        return assertThat(
                underlag
                        .stream()
                        .flatMap(Underlag::stream)
                        .filter(predikat)
                        .collect(toList())
        );
    }

    /**
     * Opprettar ein ny assertion som opererer på samlinga av verdiar henta av <code>mapper</code> frå
     * underlagsperiodene på <code>underlag</code>.
     *
     * @param <T>      verditypen som det skal verifiserast på
     * @param underlag ei samling med underlag som underlagsperiodene som <code>mapper</code> opererer på skal
     *                 hentast frå
     * @param mapper   ein funksjon som hentar ut verdien som asserten skal operere på frå underlagsperiodene
     *                 i <code>underlag</code>
     * @param predikater
     * @return ein ny asserter med verdiar henta frå alle underlagsperiodene
     */
    public static <T> AbstractListAssert<?, ? extends List<T>, T> assertVerdiFraUnderlagsperioder(
            final Collection<Underlag> underlag, final Function<Underlagsperiode, T> mapper,
            final Predicate<Underlagsperiode>... predikater) {
        return assertThat(
                underlag
                        .stream()
                        .flatMap(Underlag::stream)
                        .filter(and(predikater))
                        .map(mapper)
                        .collect(toList())
        );
    }

    /**
     * Lagar ein ny assertion som opererer på alle unike annotasjonsverdiar av angitt type, henta frå alle
     * underlagsperiodene i <code>underlag</code>.
     *
     * @param underlag        underlaget som periodene blir henta frå
     * @param annotasjonstype annotasjonstypen som verdiar skal hentast ut for
     * @return ein assertion som opererer på alle unike annotasjonar av angitt type frå underlagsets perioder
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#annotasjonFor(Class)
     */
    public static <T> AbstractListAssert<?, ? extends List<T>, T> assertUnikeAnnotasjonsverdiar(
            final Underlag underlag, final Class<T> annotasjonstype) {
        return assertThat(
                underlag
                        .stream()
                        .map((Underlagsperiode p) -> p.annotasjonFor(annotasjonstype))
                        .distinct()
                        .collect(toList())
        ).as("unike verdiar for annotasjon " + annotasjonstype.getSimpleName() + " i underlag " + underlag);
    }

    /**
     * Underlagsperioder som er kobla til tidsperioder av den angitte typen.
     *
     * @param type periodetypen som underlagsperiodene må vere kobla til
     * @return eit predikat som matchar underlagsperioder som er tilkobla tidsperioder av den angitte typen
     */
    public static Predicate<Underlagsperiode> harKobling(final Class<? extends Tidsperiode> type) {
        return p -> p.koblingAvType(type).isPresent();
    }

    /**
     * Underlagsperioder som er annotert med ein verdi av den angitte typen.
     *
     * @param type annotasjonstypen som underlagsperioda er annotert med ein verdi for
     * @return eit predikat som matchar underlagsperioder som er annotert med ein verdi av den angitte typen
     */
    public static Predicate<Underlagsperiode> harAnnotasjon(final Class<?> type) {
        return periode -> periode.valgfriAnnotasjonFor(type).isPresent();
    }

    /**
     * Et composite predikat som returnerer <code>true</code> dersom alle predikatene i <code>predikater</code>
     * returnerer true, eller dersom <code>predikater</code> er tom.
     *
     * @param predikater en samling predikater som en skal slå til et samlet AND-separert predikat
     * @return et nytt predikat som returnerer <code>true</code> dersom alle predikatene returnerer true
     * eller <code>predikater</code> er tom, <code>false</code> ellers
     */
    public static <T> Predicate<T> and(final Predicate<T>... predikater) {
        return input -> Stream
                .of(predikater)
                .reduce(Predicate::and)
                .orElse(t -> true)
                .test(input);
    }

    /**
     * Hentar ut verdien for ein påkrevd annotasjon av angitt type.
     *
     * @param <T>  verditypen til annotasjonen som skal hentast ut
     * @param type annotasjonstypen som verdien skal hentast ut for
     * @return ein funksjon som hentar ut verdien som underlagsperioda er annotert med for den angitte typen
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode#annotasjonFor(Class)
     */
    public static <T> Function<Underlagsperiode, T> paakrevdAnnotasjon(Class<T> type) {
        return periode -> periode.annotasjonFor(type);
    }
}
