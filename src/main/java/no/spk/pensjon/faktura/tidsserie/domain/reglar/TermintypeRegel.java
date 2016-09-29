package no.spk.pensjon.faktura.tidsserie.domain.reglar;


import java.util.Optional;

import no.spk.pensjon.faktura.tidsserie.domain.avtaledata.Termintype;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Ordning;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiekategori;
import no.spk.pensjon.faktura.tidsserie.domain.grunnlagsdata.Premiestatus;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.BeregningsRegel;
import no.spk.pensjon.faktura.tidsserie.domain.underlag.Beregningsperiode;

/**
 * {@link TermintypeRegel} representerer reglen som bestemmer hvilken termintype perioden knyttes til
 * basert på ordning, premiekategori og premiestatus.
 *
 */

public class TermintypeRegel implements BeregningsRegel<Termintype> {

    @Override
    public Termintype beregn(Beregningsperiode<?> periode) {
        final Optional<Premiestatus> premiestatus = periode.valgfriAnnotasjonFor(Premiestatus.class);
        final Optional<Premiekategori> premiekategori = periode.valgfriAnnotasjonFor(Premiekategori.class);
        final Optional<Ordning> ordning = periode.valgfriAnnotasjonFor(Ordning.class);

        if (ordning.isPresent() && premiestatus.isPresent() && premiekategori.isPresent()) {
            if (!premiestatus.get().equals(Premiestatus.IPB)
                    && (premiekategori.get().equals(Premiekategori.FASTSATS) || premiekategori.get().equals(Premiekategori.FASTSATS_AARLIG_OPPFOELGING))) {
                Ordning ordKode = ordning.get();
                if (ordKode.equals(Ordning.POA)) {
                    return Termintype.POA;
                } else if (ordKode.equals(Ordning.OPERA) || ordKode.equals(Ordning.SPK)) {
                    return Termintype.SPK;
                }
            }
            return Termintype.ANDRE;
        }
        return Termintype.UKJENT;
    }
}