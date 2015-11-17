package no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsdato.foedselsdato;
import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Personnummer.personnummer;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Test;

/**
 * Enheitstestar for {@link no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Foedselsnummer}.
 *
 * @author Tarjei Skorgenes
 */
public class FoedselsnummerTest {
    @Test
    public void skalVereLikAnnanInstansMedLikeVerdiar() {
        assertFoedselsnummer(
                new Foedselsnummer(
                        foedselsdato(19790101),
                        personnummer(12321)
                )
        )
                .isNotSameAs(
                        // Den her er med for å sikre at extract-variable-kåte utviklarar trekker den her ut til en
                        // variabel uten å tenke på at bieffekten av det er å invalidere heile hensikta med testen :)
                        new Foedselsnummer(foedselsdato(19790101), personnummer(12321))
                )
                .isEqualTo(
                        new Foedselsnummer(foedselsdato(19790101), personnummer(12321))
                );
    }

    @Test
    public void skalVereLikSegSjoelv() {
        final Foedselsnummer me = new Foedselsnummer(foedselsdato(19790101), personnummer(12321));
        assertThat(me).as("fødselsnummer").isSameAs(me).isEqualTo(me);
    }

    @Test
    public void skalVereUlikFoedselsnummerMedAndreVerdiar() {
        final int dato = 19790101;
        final int personnummer = 12321;

        assertFoedselsnummer(
                new Foedselsnummer(
                        foedselsdato(dato),
                        personnummer(personnummer)
                )
        )
                .isNotEqualTo(
                        new Foedselsnummer(
                                foedselsdato(dato + 1),
                                personnummer(personnummer)
                        )
                )
                .isNotEqualTo(
                        new Foedselsnummer(
                                foedselsdato(dato - 1),
                                personnummer(personnummer)
                        )
                )
        ;

        assertFoedselsnummer(
                new Foedselsnummer(
                        foedselsdato(dato),
                        personnummer(personnummer)
                )
        )
                .isNotEqualTo(
                        new Foedselsnummer(
                                foedselsdato(dato),
                                personnummer(personnummer + 1)
                        )
                )
                .isNotEqualTo(
                        new Foedselsnummer(
                                foedselsdato(dato),
                                personnummer(personnummer - 1)
                        )
                )
        ;
    }

    @Test
    public void skalHaLikHashCodeDersomFoedselsnummerErLike() {
        final Foedselsnummer actual = new Foedselsnummer(foedselsdato(19790806), personnummer(1));
        assertThat(
                actual
                        .hashCode()
        )
                .as("hashcode for " + actual)
                .isEqualTo(
                        // Prettay pleas don't replace med actual her
                        new Foedselsnummer(foedselsdato(19790806), personnummer(1))
                                .hashCode()
                );
    }

    private static AbstractObjectAssert<?, Foedselsnummer> assertFoedselsnummer(final Foedselsnummer foedselsnummer) {
        return assertThat(foedselsnummer).as("fødselsnummer");
    }
}