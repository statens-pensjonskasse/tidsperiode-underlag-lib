package no.spk.pensjon.faktura.tidsserie.domain.periodetyper;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static no.spk.pensjon.faktura.tidsserie.domain.periodetyper.Feilmeldingar.TIL_OG_MED_PAAKREVD;

/**
 * {@link Observasjonsperiode} representerer
 * nedre grense for fr� og med-datoen og �vre grense for til og med-datoen til eit
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlag}.
 * <p>
 * Hovedintensjonen ved dette konseptet er � kunne avgrense st�rrelsen fram i tid p� underlaget i situasjonar der
 * ei eller fleire av input-periodene til underlaget er l�pande og dermed er varig evig langt inn i framtida.
 *
 * @author Tarjei Skorgenes
 */
public class Observasjonsperiode extends GenerellTidsperiode {
    /**
     * Konstruerer ei ny grenser.
     *
     * @param fraOgMed nedre grense for fr� og med-dato til f�rste underlagsperiode i eit underlag
     * @param tilOgMed �vre grense for til og med-dato til siste underlagsperiode i eit underlag
     * @throws NullPointerException dersom nokon av datoane er <code>null</code>
     */
    public Observasjonsperiode(final LocalDate fraOgMed, final LocalDate tilOgMed) {
        super(fraOgMed, ofNullable(requireNonNull(tilOgMed, TIL_OG_MED_PAAKREVD)));
    }
}
