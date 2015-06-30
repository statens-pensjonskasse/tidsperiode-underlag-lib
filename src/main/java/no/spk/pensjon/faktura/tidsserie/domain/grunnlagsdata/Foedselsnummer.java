package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer} representerer
 * ein unik identifikator for eit medlem.
 *
 * @author Tarjei Skorgenes
 */
public final class Foedselsnummer {
    private final Foedselsdato foedselsdato;

    private final Personnummer personnummer;

    /**
     * Konstruerer eit nytt fødselsnummer.
     *
     * @param foedselsdato fødselsdatoen til medlemmet
     * @param personnummer personnummeret som i kombinasjon med fødselsdatoen unikt identifiserer medlemmet
     * @throws NullPointerException viss nokon av argumenta er <code>null</code>
     */
    public Foedselsnummer(final Foedselsdato foedselsdato, final Personnummer personnummer) {
        this.foedselsdato = requireNonNull(foedselsdato, "fødselsdato er påkrevd, men var null");
        this.personnummer = requireNonNull(personnummer, "personnummer er påkrevd, men var null");
    }

    @Override
    public int hashCode() {
        return Objects.hash(foedselsdato, personnummer);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Foedselsnummer)) {
            return false;
        }
        final Foedselsnummer other = (Foedselsnummer) obj;
        return foedselsdato.equals(other.foedselsdato) && personnummer.equals(other.personnummer);
    }

    @Override
    public String toString() {
        return foedselsdato.tilKode() + personnummer.toString();
    }
}
