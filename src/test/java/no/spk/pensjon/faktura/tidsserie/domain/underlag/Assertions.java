package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.StillingsforholdId;
import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.Tidsperiode;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractListAssert;

public class Assertions {
    /**
     * Opprettar ein ny assertion som opererer på periodas unike identifikator.
     *
     * @param periode underlagsperioda UUIDen skal hentast frå
     * @return ein ny assert med UUIDen til perioda
     * @see Underlagsperiode#id()
     */
    public static AbstractComparableAssert<?, UUID> assertUUID(final Underlagsperiode periode) {
        return assertThat(periode.id()).as("unik ID for periode " + periode);
    }

    /**
     * Opprettar ein ny assertion som opererer på samlinga av alle koblingar av angitt type frå underlagsperioda.
     *
     * @param periode underlagsperioda koblingane skal hentast frå
     * @param type    kva type koblingar som skal hentast ut frå underlagsperioda
     * @return ein ny assert med ei liste som inneheld alle koblingane av den angitte typen
     */
    @SuppressWarnings("rawtypes")
    public static AbstractListAssert assertKoblingarAvType(final Underlagsperiode periode, Class<? extends Tidsperiode<?>> type) {
        return (AbstractListAssert) assertThat(periode.koblingarAvType(type).<Tidsperiode<?>>map(k -> k).collect(toList()));
    }

    /**
     * @see #assertUnderlagsperioder(java.util.Collection, java.util.function.Predicate)
     * @see #harKobling(Class)
     * @see java.util.function.Predicate#negate()
     */
    public static AbstractListAssert<?, ? extends List<Underlagsperiode>, Underlagsperiode> assertUnderlagsperioderUtanKoblingTil(
            final Map<StillingsforholdId, Underlag> underlagene, final Class<? extends Tidsperiode<?>> type) {
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
        return (AbstractListAssert<?, ? extends List<Underlagsperiode>, Underlagsperiode>) assertThat(
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
     * @param <T>        verditypen som det skal verifiserast på
     * @param underlag   ei samling med underlag som underlagsperiodene som <code>mapper</code> opererer på skal
     *                   hentast frå
     * @param mapper     ein funksjon som hentar ut verdien som asserten skal operere på frå underlagsperiodene
     *                   i <code>underlag</code>
     * @param predikater eit valgfritt sett med predikat som alle må slå til for at underlagsperioda skal bli
     *                   tatt med i asserten
     * @return ein ny asserter med verdiar henta frå alle underlagsperiodene
     */
    @SafeVarargs
    public static <T> AbstractListAssert<?, ? extends List<T>, T> assertVerdiFraUnderlagsperioder(
            final Collection<Underlag> underlag, final Function<Underlagsperiode, T> mapper,
            final Predicate<Underlagsperiode>... predikater) {
        return (AbstractListAssert<?, ? extends List<T>, T>) assertThat(
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
    public static <T> AbstractListAssert<?, ? extends List<? extends T>, T> assertUnikeAnnotasjonsverdiar(
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Predicate<Underlagsperiode> harKobling(final Class<? extends Tidsperiode> type) {
        return p -> p.koblingAvType(type).isPresent();
    }

    /**
     * Annoterbare objekt som er annotert med ein verdi av den angitte typen.
     *
     * @param type annotasjonstypen som objektet er annotert med ein verdi for
     * @return eit predikat som matchar objektet som er annotert med ein verdi av den angitte typen
     */
    public static <S extends HarAnnotasjonar> Predicate<S> harAnnotasjon(final Class<?> type) {
        return objekt -> objekt.valgfriAnnotasjonFor(type).isPresent();
    }

    /**
     * Et composite predikat som returnerer <code>true</code> dersom alle predikatene i <code>predikater</code>
     * returnerer true, eller dersom <code>predikater</code> er tom.
     *
     * @param predikater en samling predikater som en skal slås sammen til et samlet AND-separert predikat
     * @return et nytt predikat som returnerer <code>true</code> dersom alle predikatene returnerer true
     * eller <code>predikater</code> er tom, <code>false</code> ellers
     */
    @SafeVarargs
    public static <T> Predicate<T> and(final Predicate<T>... predikater) {
        return input -> Stream
                .of(predikater)
                .reduce(Predicate::and)
                .orElse(t -> true)
                .test(input);
    }

    /**
     * Et composite predikat som returnerer <code>true</code> dersom minst et av predikatene i <code>predikater</code>
     * returnerer true, eller dersom <code>predikater</code> er tom.
     *
     * @param predikater en samling predikater som en skal slås sammen til et samlet OR-separert predikat
     * @return et nytt predikat som returnerer <code>true</code> dersom et av predikatene returnerer true
     * eller <code>predikater</code> er tom, <code>false</code> ellers
     */
    @SafeVarargs
    public static <T> Predicate<T> or(final Predicate<T>... predikater) {
        return input -> Stream
                .of(predikater)
                .reduce(Predicate::or)
                .orElse(t -> true)
                .test(input);
    }

    /**
     * Hentar ut verdien for ein påkrevd annotasjon av angitt type.
     *
     * @param <T>  verditypen til annotasjonen som skal hentast ut
     * @param type annotasjonstypen som verdien skal hentast ut for
     * @return ein funksjon som hentar ut verdien som det annoterbare objektet er annotert med for den angitte typen
     * @see no.spk.pensjon.faktura.tidsserie.domain.underlag.HarAnnotasjonar#annotasjonFor(Class)
     */
    public static <T, S extends HarAnnotasjonar> Function<S, T> paakrevdAnnotasjon(final Class<T> type) {
        return objekt -> objekt.annotasjonFor(type);
    }
}
