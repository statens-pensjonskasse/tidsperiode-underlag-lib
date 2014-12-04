package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

/**
 * Created by tas on 02.12.2014.
 */
public class Funksjonstillegg {
    private final Kroner beloep;

    public Funksjonstillegg(final Kroner beloep) {
        this.beloep = beloep;
    }

    public Kroner beloep() {
        return beloep;
    }

    @Override
    public int hashCode() {
        return beloep.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final Funksjonstillegg other = (Funksjonstillegg) obj;
        return beloep.equals(other.beloep);
    }

    @Override
    public String toString() {
        return "funksjonstillegg " + beloep;
    }
}
