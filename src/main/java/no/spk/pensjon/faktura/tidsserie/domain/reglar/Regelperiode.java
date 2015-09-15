package no.spk.pensjon.faktura.tidsserie.domain.reglar;

import java.time.LocalDate;
import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.tidsperiode.AbstractTidsperiode;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Annoterbar;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;

/**
 * {@link Regelperiode} representerer perioda ein
 * {@link no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel} er gjeldande i.
 * <p>
 * Ettersom beregningsreglane kan endre seg over tid, enten fordi SPK endrar metodikk eller på grunn av endringar
 * i regelverk som SPK må forholde seg til, ønskjer vi å kunne handtere dette på generelt vis via underlagets
 * underlagsperioder.
 * </p>
 * <p>
 * For å unngå at ein i alle reglar som kan variere over tid, må sjekke på dato for å finne ut kva regel som er
 * gjeldande på eit bestemt tidspunkt, inkluderer vi heller perioder med gjeldande beregningsregel, som ein del av
 * underlaget. På dette viset får vi då automatisk splitta opp underlaget kvar gang ein byttar beregningsregel og
 * ein kan dermed bere slå opp gjeldande beregningsregel frå kvar underlagsperiodes periodekoblingar.
 * </p>
 * <h2>Eksempel</h2>
 * <p>
 * Permisjonsavtalen vart endra med virkningsdato 1. juli 2013. Før endringa var det kun punkt 4a og 4b som skulle
 * handterast. Etter endringa skulle beregningsregelen også ta hensyn til 4c og utvida medregningsperiode.
 * </p>
 * Dette tilfellet ville kunne handterast ved å ha to beregningsreglar (PermisjonsRegelV1 og PermisjonsRegelV2):
 * <pre>
 * RegelPeriode a = new RegelPeriode(dato("2003.01.01"), of(dato("2013.06.30"), new PermisjonsRegelV1());
 * RegelPeriode b = new RegelPeriode(dato("2013.07.01"), empty(),               new PermisjonsRegelV2());
 * </p>
 * Et reelt tilfelle er Minstegrensen som endres fom 1. januar 2016. Før endringen benyttes SPK-ordning og premiestatus
 * for å bestemme hva som er minste stillingsstørrelse for faktureringen. Etter endringen gjelder kun én
 * stillingsstørrelse, 20%. Vi har to beregningsregler for å håndtere dette: {@link MinstegrenseRegelVersjon1}
 * og {@link MinstegrenseRegelVersjon2}
 * </p>
 * RegelPeriode a = new Regelperiode(dato("2007.01.01"), of(dato("2015.12.31")), MinstegrenseRegel.class, new MinstegrenseRegelVersjon1()),
 * RegelPeriode b = new Regelperiode(dato("2016.01.01"), empty(),  MinstegrenseRegel.class, new MinstegrenseRegelVersjon2()),
 * </pre>
 *
 * @author Tarjei Skorgenes
 */
public class Regelperiode<T> extends AbstractTidsperiode<Regelperiode<T>> {
    private final BeregningsRegel<? extends T> gjeldandeRegel;
    private final Class<? extends BeregningsRegel> regelType;

    /**
     * Konstruerer ei ny tidsperiode som den angitte beregningsregelen er gjeldande i.
     *
     * @param fraOgMed       første dag regelen er gjeldande for
     * @param tilOgMed       siste dag regelen er gjeldande for, eller løpande viss regelen ikkje har blitt erstatta
     *                       av ein ny regel enda
     * @param gjeldandeRegel beregningsregelen som er gjeldande innanfor den angitte tidsperioda
     * @throws java.lang.NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public Regelperiode(final LocalDate fraOgMed, final Optional<LocalDate> tilOgMed, final BeregningsRegel<? extends T> gjeldandeRegel) {
        this(fraOgMed, tilOgMed, gjeldandeRegel.getClass(), gjeldandeRegel);
    }

    /**
     * Konstruerer ei ny tidsperiode som den angitte beregningsregelen er gjeldande i. Har en ekstra parameter for
     * beregningsregeltype.
     *
     * @param fraOgMed       første dag regelen er gjeldande for
     * @param tilOgMed       siste dag regelen er gjeldande for, eller løpande viss regelen ikkje har blitt erstatta
     *                       av ein ny regel enda
     * @param regelType      beregningsregeltype
     * @param gjeldandeRegel beregningsregelen som er gjeldande innanfor den angitte tidsperioda
     * @throws java.lang.NullPointerException viss nokon av parameterverdiane er <code>null</code>
     */
    public Regelperiode(LocalDate fraOgMed, Optional<LocalDate> tilOgMed, final Class<? extends BeregningsRegel> regelType, final BeregningsRegel<? extends T> gjeldandeRegel) {
        super(fraOgMed, tilOgMed);
        this.gjeldandeRegel = gjeldandeRegel;
        this.regelType = regelType;
    }

    /**
     * Annoterer underlagsperioda med gjeldande beregningsregel.
     *
     * @param periode underlagsperioda som skal annoterast
     */
    public void annoter(final Annoterbar<?> periode) {
        periode.annoter(regelType, gjeldandeRegel);
    }

    @Override
    public String toString() {
        return gjeldandeRegel.getClass().getSimpleName()
                + "[" + fraOgMed() + "" + "->" + tilOgMed().map(d -> d.toString()).orElse("") + "" + "]";
    }
}
