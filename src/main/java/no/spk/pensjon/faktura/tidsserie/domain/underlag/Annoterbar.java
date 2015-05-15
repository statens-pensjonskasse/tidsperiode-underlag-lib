package no.spk.pensjon.faktura.tidsserie.domain.underlag;

import java.util.Optional;

/**
 * {@link Annoterbar} representerer eit objekt som kan annoterast med p�krevde og valgfrie verdiar.
 * <p>
 * Annotasjonar kan p� mange m�tar tenkast p� som ein dynamisk skjema som gjer ein i stand til � utvide
 * forskjellige instansar av objekt av samme type, med forskjellige felt fr� instans til instans.
 *
 * @param <S> typen til objektet som implementerer grensesnittet (aka. <code>this</code>).
 * @author Tarjei Skorgenes
 */
public interface Annoterbar<S extends Annoterbar<S>> {
    /**
     * Annoterer objektet med den angitte typen og verdien.
     * <p>
     * Dersom <code>verdi</code> er av type {@link java.util.Optional}, er det den valgfrie, wrappa verdien som blir
     * registrert, viss den wrappa verdien ikkje eksisterer blir annotasjonsverdien som objektet potensielt sett kan
     * vere annotert med fr� tidligare.
     *
     * @param <T>   annotasjonstypen
     * @param type  annotasjonstypen
     * @param verdi verdien som skal vere tilknytta annotasjonstypen
     * @return <code>this</code>
     * @throws IllegalArgumentException viss <code>type</code> er {@link java.util.Optional}
     */
    <T> S annoter(Class<? extends T> type, T verdi);

    /**
     * Annoterer objektet med alle annotasjonar som <code>kilde</code> er annotert med.
     *
     * @param kilde eit annotebart objekt som annotasjonane skal kopierast fr�
     * @return <code>this</code>
     */
    S annoterFra(final S kilde);
}
