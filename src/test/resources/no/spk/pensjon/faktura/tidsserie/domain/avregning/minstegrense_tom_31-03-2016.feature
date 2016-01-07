# encoding: utf-8
# language: no
Egenskap: Minstegrense ved beregning av pensjonsgivende lønn til og med 31. mars 2016

  Ved beregning av pensjonsgivende lønn, skal minstegrensen for medlemskap kunne påvirke
  den pensjonsgivende lønnen for perioden.

  For perioder der stillingsforholdet er under gjeldende minstegrense, skal pensjonsgivende lønn
  alltid være lik kr 0.

  I motsetning til ved pensjonering, tar ikke fastsats-metodikken hensyn til medlemmets totale stillingsstørrelse.
  Minstegrensen blir ved fakturering kun sett i forhold til stillingsforholdets individuelle stillingsstørrelse.

  Minstegrensen tar heller ikke hensyn til medregninger, de er definert til alltid å være over minstegrensen
  slik at bistillinger og tillegg fra annen arbeidsgiver, aldri blir avkortet til kr 0 på grunn av
  stillingsstørrelse.

  Gjeldende minstegrense for fakturering har endret seg over tid. I årene 2013 til og med 31. mars 2016 har fastasts-metodikken
  operert med en forenklet minstegrense for fastsatsfaktureringen når det gjelder SPK-ordningen.

  Den forenklede minstegrensen for SPK-ordningen er i denne perioden basert på gjeldende premiestatus for avtalen
  stillingsforholdet tilhører, ikkje stillingstype og stillingsbetegnelse slik som pensjoneringsområdet benytter.

  Minstegrense for POA-ordningen (apotek) er i denne perioden basert på gjeldende stillingskode for en stilling.

  Minstegrense for opera-ordningen er i denne perioden  i 50%.

  Bakgrunn: Minstegrenseregel ved avregning av perioder fra og med premieår 2016
    Gitt underlagsperioden sin fra og med-dato er 2013.01.01
    Og underlagsperioden sin til og med-dato er 2016.03.31
    Og underlagsperioden benytter regler for avregning

  Scenariomal: Opera
    Gitt en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent   |
      | Opera   | <Stillingsprosent> |

    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Stillingsprosent | Over eller under? |
      | 49,99%           | Under             |
      | 50,0%            | Over              |
      | 100,00%          | Over              |

  Scenariomal: SPK
    Gitt en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent   | Premiestatus   |
      | SPK     | <Stillingsprosent> | <Premiestatus> |

    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Stillingsprosent | Premiestatus | Over eller under? |
      | 34,99%           | AAO-01       | Under             |
      | 35,00%           | AAO-01       | Over              |
      | 100,00%          | AAO-01       | Over              |
      | 34,99%           | AAO-02       | Under             |
      | 35,00%           | AAO-02       | Over              |
      | 37,32%           | ANNEN        | Under             |
      | 37,33%           | ANNEN        | Over              |

  Scenariomal: Apotek
    Gitt en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent   | Stillingskode   |
      | POA     | <Stillingsprosent> | <Stillingskode> |

    Så er stillingen <Over eller under?> minstegrensen?

    Eksempler:
      | Stillingsprosent | Stillingskode | Over eller under? |
      | 38,45%           | 1             | Under             |
      | 38,46%           | 1             | Over              |
      | 38,45%           | 2             | Under             |
      | 38,46%           | 2             | Over              |
      | 38,45%           | 3             | Under             |
      | 38,46%           | 3             | Over              |
      | 38,45%           | 10            | Under             |
      | 38,46%           | 10            | Over              |
      | 38,45%           | 11            | Under             |
      | 38,46%           | 11            | Over              |
      | 37,49%           | 5             | Under             |
      | 38,5             | 5             | Over              |
      | 37,49%           | 60            | Under             |
      | 38,5             | 60            | Over              |
      | 37,49%           | 61            | Under             |
      | 38,5             | 61            | Over              |
      | 37,49%           | 8             | Under             |
      | 38,5             | 8             | Over              |
      | 37,49%           | 9             | Under             |
      | 38,5             | 9             | Over              |
      | 37,49%           | 12            | Under             |
      | 38,5             | 12            | Over              |
      | 37,49%           | 13            | Under             |
      | 38,5             | 13            | Over              |
      | 37,49%           | 14            | Under             |
      | 38,5             | 14            | Over              |

