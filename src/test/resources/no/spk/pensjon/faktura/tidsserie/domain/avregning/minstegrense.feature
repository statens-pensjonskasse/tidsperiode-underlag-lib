# encoding: utf-8
# language: no
Egenskap: Minstegrense ved beregning av pensjonsgivende lønn

  Ved beregning av pensjonsgivende lønn, skal minstegrensen for medlemskap kunne påvirke
  den pensjonsgivende lønnen for perioden.

  For perioder der stillingsforholdet er under gjeldende minstegrense, skal pensjonsgivende lønn
  alltid være lik kr 0.

  I motsetning til ved pensjonering, tar ikke fastsats-metodikken hensyn til medlemmets totale stillingsstørrelse.
  Minstegrensen blir ved fakturering kun sett i forhold til stillingsforholdets individuelle stillingsstørrelse.

  Minstegrensen tar heller ikke hensyn til medregninger, de er definert til alltid å være over minstegrensen
  slik at bistillinger og tillegg fra annen arbeidsgiver, aldri blir avkortet til kr 0 på grunn av
  stillingsstørrelse.

  Gjeldende minstegrense for fakturering har endret seg over tid. I årene 2013 til 2015 har fastasts-metodikken
  operert med en forenklet minstegrense for fastsatsfaktureringen når det gjelder SPK-ordningen.

  Den forenklede minstegrensen for SPK-ordningen har basert seg på gjeldende premiestatus for avtalen
  stillingsforholdet tilhører, ikkje stillingstype og stillingsbetegnelse slik som pensjoneringsområdet benytter.

  Fra og med 1. januar 2016 har gjeldende minstegrense blitt forenklet slik at både Apotek- og SPK-ordningene
  har fått en flat minstegrense på 20% for alle stillinger tilknyttet ordningene.

  Opera-ordningen er holdt utenfor forenklingen av minstegrensen og beholder derfor sin gamleminstegrense på 50% både
  før og etter 2016.

  Bakgrunn: Minstegrenseregel ved avregning av perioder fra og med premieår 2016
    Gitt underlagsperioden sin fra og med-dato er 2016.01.01
    Og underlagsperioden sin til og med-dato er 2016.12.31
    Og underlagsperioden benytter regler for avregning

  Scenariomal: Opera
    Gitt en underlagsperiode med følgende innhold:
      | Ordning   | Stillingsprosent   |
      | <Ordning> | <Stillingsprosent> |

    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Ordning | Stillingsprosent | Over eller under? |
      | Opera   | 49,67%           | Under             |
      | Opera   | 50,87%           | Over              |
      | Opera   | 100,00%          | Over              |

  Scenariomal: SPK og Apotek
    Gitt en underlagsperiode med følgende innhold:
      | Ordning   | Stillingsprosent   |
      | <Ordning> | <Stillingsprosent> |

    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Ordning | Stillingsprosent | Over eller under? |
      | SPK     | 19,99%           | Under             |
      | SPK     | 20,00%           | Over              |
      | SPK     | 75,85%           | Over              |
      | Apotek  | 9,95%            | Under             |
      | Apotek  | 19,50%           | Under             |
      | Apotek  | 20,01%           | Over              |
      | Apotek  | 99,99%           | Over              |

  Scenariomal: Minstegrense-paranoia

  Det kan forekomme tilfeller av stillinger med stillingsprosent som er større enn 100% langt tilbake i tid.
  % over 100 skal ikke forekomme fra PUMA, men skal ikke medføre at minstegrenseregelen feiler dersom
  den blir forsøkt brukt på slike stillingsstørrelser.

    Gitt en underlagsperiode med følgende innhold:
      | Ordning   | Stillingsprosent   |
      | <Ordning> | <Stillingsprosent> |
    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Ordning | Stillingsprosent | Over eller under? |
      | SPK     | 100,01%          | Over              |
      | SPK     | 120,17%          | Over              |