package no.spk.pensjon.faktura.tidsserie.domain.tidsserie;

import static no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale.avtale;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleprodukt;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtalerelatertperiode;
import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Avtaleversjon;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Avtale;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.AvtaleId;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Underlagsperiode;

final class AvtaleFactory {
    /**
     * Orkestrerer oppbygging av ein ny representasjon av gjeldande tilstand for ein avtale basert
     * på underlagsperiodas {@link Avtalerelatertperiode avtale-relaterte periodekoblingar}.
     * <p>
     * Avtalen sin tilstand blir bygd opp basert på alle avtaleprodukt og avtaleversjonen som er
     * kobla til underlagsperioda og som tilhøyrer avtalen.
     *
     * @param periode underlagsperioda som inneheld informasjon om gjeldande tilstand for avtalen
     * @param avtale  avtalen gjeldande tilstand skal byggast opp for
     * @return gjeldande tilstand for avtalen innanfor underlagsperioda
     */
    Avtale lagAvtale(final Underlagsperiode periode, final AvtaleId avtale) {
        final Avtale.AvtaleBuilder builder = avtale(avtale);
        periode.koblingAvType(Avtaleversjon.class, a -> a.tilhoeyrer(avtale))
                .ifPresent(a -> a.populer(builder));
        periode.koblingarAvType(Avtaleprodukt.class)
                .filter(a -> a.tilhoeyrer(avtale))
                .forEach(a -> a.populer(builder));
        return builder.bygg();
    }
}
