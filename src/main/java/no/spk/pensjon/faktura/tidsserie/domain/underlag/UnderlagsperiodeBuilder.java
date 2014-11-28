package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Tidsperiode;

import java.time.LocalDate;

/**
 * Builder for {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode}.
 *
 * @author Tarjei Skorgenes
 */
public class UnderlagsperiodeBuilder {
    private final Annotasjonar annotasjonar = new Annotasjonar();

    private final Koblingar koblingar = new Koblingar();

    private LocalDate fraOgMed;

    private LocalDate tilOgMed;

    /**
     * Konstruerer ein ny builder med tom tilstand.
     */
    public UnderlagsperiodeBuilder() {
    }

    /**
     * Konstruerer ein ny builder utan frå og med- og til og med-dato, som
     * med ein kopi av dei angitte koblingane og annotasjonane.
     *
     * @param koblingar    koblingane som builderen skal populerast med
     * @param annotasjonar annotasjonane som builderen skal populerast med
     */
    public UnderlagsperiodeBuilder(final Koblingar koblingar, final Annotasjonar annotasjonar) {
        this.annotasjonar.addAll(annotasjonar);
        this.koblingar.addAll(koblingar);
    }

    /**
     * Konstruerer ein ny builder med ein kopi av tilstanden til <code>source</code>.
     *
     * @param source builderen som tilstanda skal kopierast frå
     */
    public UnderlagsperiodeBuilder(final UnderlagsperiodeBuilder source) {
        this(source.koblingar, source.annotasjonar);
        fraOgMed(source.fraOgMed).tilOgMed(source.tilOgMed);
    }

    /**
     * Konstruerer ei ny underlagsperiode og populerer den med frå og med- og til og med-dato
     * frå builderen, perioda blir så annotert med alle annotasjonar som er lagt inn i builderen.
     *
     * @return ei ny underlagsperiode populert med tilstand frå builderen
     */
    public Underlagsperiode bygg() {
        final Underlagsperiode periode = new Underlagsperiode(fraOgMed, tilOgMed);
        annotasjonar.annoter(periode);
        koblingar.kobleTil(periode);
        return periode;
    }

    /**
     * Frå og med-datoen som underlagsperioder bygd av builderen skal benytte.
     *
     * @param dato den nye frå og med-datoen
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder fraOgMed(final LocalDate dato) {
        fraOgMed = dato;
        return this;
    }

    /**
     * Til og med-datoen som underlagsperioder bygd av builderen skal benytte.
     *
     * @param dato den nye til og med-datoen
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder tilOgMed(final LocalDate dato) {
        tilOgMed = dato;
        return this;
    }

    /**
     * Annoterer underlagsperioda med den angitte verdien. Annotasjonen blir registrert under
     * {@link Object#getClass() typen} til verdien som ein snarvei for følgjande snutt:
     * <code>
     * periode.annoter(verdi.getClass(), verdi);
     * </code>
     *
     * @param annotasjon verdien som periodene skal annoterast med
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder med(final Object annotasjon) {
        annotasjonar.registrer(annotasjon.getClass(), annotasjon);
        return this;
    }

    /**
     * Fjernar annotasjonen for den angitte annotasjonstypen slik at perioder bygd seinare av builderen ikkje
     * blir annotert med denne typen.
     *
     * @param type typen på annotasjonen som skal fjernast frå builderen
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder uten(final Class<?> type) {
        annotasjonar.remove(type);
        return this;
    }

    /**
     * Legger til ei kobling som perioder bygd seinare av builderen skal bli kobla til.
     *
     * @param kobling ei tidsperiode som framtidige bygde underlagsperioder skal koblast til
     * @return <code>this</code>
     */
    public UnderlagsperiodeBuilder medKobling(final Tidsperiode<?> kobling) {
        koblingar.add(kobling);
        return this;
    }

    /**
     * Lagar ein kopi av builderen som ein kan endre tilstand på utan å påvirke builderen kopien vart laga frå.
     *
     * @return ein ny builder med som inneheld ein kopi av gjeldande tilstand på builderen
     */
    public UnderlagsperiodeBuilder kopi() {
        return new UnderlagsperiodeBuilder(this);
    }
}
