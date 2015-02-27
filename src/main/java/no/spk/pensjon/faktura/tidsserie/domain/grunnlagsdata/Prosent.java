package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;

import static java.lang.Double.parseDouble;
import static java.util.Objects.requireNonNull;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Prosent} representerer ein prosentsats.
 *
 * @author Tarjei Skorgenes
 */
public class Prosent {
    /**
     * 0%.
     */
    public static final Prosent ZERO = new Prosent(0d);

    private static final Supplier<String> VALIDER_TEKST = () -> "prosentsatsen er påkrevd, men var null";

    private final double verdi;

    /**
     * Konstruerer ein ny prosentsats ut frå den tekstlige representasjonen av satsen.
     * <p>
     * <code>tekst</code> blir strippa for mellomrom og %-tegn og konvertert direkte til ein prosentsats
     * <br>
     * Prosentsatsen støttar spesifisering av prosentsatsar med desimalar, både komma og punktum blir handtert.
     *
     * @param tekst ein <code>String</code> som inneheld ein prosentsats, formatert på forma <code>123%</code>
     * @throws java.lang.NullPointerException  viss <code>tekst</code> er <code>null</code>
     * @throws java.lang.NumberFormatException viss <code>tekst</code> ikkje kan konverterast til ein prosentsats
     *                                         fordi den inneheld andre tegn enn tall, %-tegn eller mellomrom.
     */
    public Prosent(final String tekst) {
        requireNonNull(tekst, VALIDER_TEKST);
        this.verdi = parseDouble(
                tekst
                        .replaceAll(",", ".")
                        .replaceAll(" ", "")
                        .replaceAll("%", "")
        ) / 100d;
    }

    /**
     * Konstruerer ein ny prosentsats ut frå den numeriske representasjonen av satsen.
     * <br>
     * Merk at verdien blir tolka som ein skalert prosentsats av samme type som returnert av {@link #toDouble()},
     * den blir ikkje delt på 100 eller tilsvarande slik som den tekstlige representasjonen blir ved parsing. Ergo vil ein verdi på 100
     * bli tolka som ein prosentsats på 10 000%.
     *
     * @param verdi ein <code>double</code> som inneheld ein ferdig vekta numerisk representasjon av ein prosentsat
     */
    public Prosent(final double verdi) {
        this.verdi = verdi;
    }

    /**
     * Legger saman verdien av dei to prosentverdiane.
     *
     * @param other prosenten som vi skal legge saman verdien med
     * @return ein ny prosent som inneheld summen av dei to prosentane
     */
    public Prosent plus(final Prosent other) {
        return new Prosent(
                verdi + other.verdi
        );
    }

    /**
     * Multipliserer saman dei to prosentane.
     *
     * @param other den andre prosenten som vi skal multipliserast med
     * @return ein ny prosent som inneheld resultatet av multiplikasjonen
     */
    public Prosent multiply(final Prosent other) {
        return new Prosent(
                this.verdi * other.verdi
        );
    }

    /**
     * Konverterer prosentsatsen til en numerisk representasjon som et tall mellom 0.0 og 1.0.
     * <br>
     * Eksempel:
     * <br>
     * 100% => 1.0
     * <br>
     * 250% => 2.5
     * <br>
     * -50% => -0.5
     *
     * @return prosentsatsen representert som eit desimaltall i form av prosentsatsen delt på 100
     */
    public double toDouble() {
        return verdi;
    }

    /**
     * Listar ut prosentsatsen som tekst med 3 desimalar, utan tusen-separator og med % som postfix.
     * <br>
     * NB: Dette er ein potensielt dyr operasjon då formateringa og avrundinga blir utført via ein ny
     * {@link java.text.NumberFormat} pr kall.
     */
    @Override
    public String toString() {
        final NumberFormat format = NumberFormat.getPercentInstance(new Locale("no", "NO"));
        format.setMaximumFractionDigits(3);
        format.setGroupingUsed(false);
        return format.format(verdi);
    }

    /**
     * Avrundar prosentane til det angitte antall desimalar og sjekkar om verdiane er like.
     *
     * @param other           den andre prosenten vi skal samanlikne mot
     * @param antallDesimalar antall desimalar vi skal ta runde av til og ta hensyn til i samanlikninga
     * @return <code>true</code> viss dei to prosentane er like etter avrunding, <code>false</code> ellers
     */
    public boolean equals(final Prosent other, final int antallDesimalar) {
        return avrund(this, antallDesimalar) == avrund(other, antallDesimalar);
    }

    /**
     * @see #Prosent(String)
     */
    public static Prosent prosent(final String text) {
        return new Prosent(text);
    }

    private long avrund(final Prosent other, final int antallDesimalar) {
        return Math.round(100d * other.verdi * Math.pow(10, antallDesimalar));
    }
}