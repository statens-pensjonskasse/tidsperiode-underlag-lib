# encoding: utf-8
# language: no
Egenskap: Øvre lønnsgrense for underlagsperioder

  Øvre lønnsgrense for en underlagsperiode setter et tak for hvor høy pensjonsgivende lønn kan være.

  Øvre lønnsgrense er gitt for hver ordning støttet av forenklet fastsats-metodikken som:
  * 12G * stillingsprosent for 3010 (SPK)
  * 12G * stillingsprosent for 3035 (Opera)
  * 10G * stillingsprosent for 3060 (POA)

  Underlagsperioder som er medregning skal ikke avkortest med stillingsprosent, og da gjelder:
  * 12G for 3010 (SPK)
  * 12G for 3035 (Opera)
  * 10G for 3060 (POA)

  Bakgrunn: Verdier for underlagsperioden dersom ikke annet er oppgitt
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsprosent | Er medregning |
      | 100%             | Nei           |

  Scenariomal: Øvre lønnsgrense for ordninger støttet av forenklet fastsats
    Gitt en underlagsperiode med følgende innhold:
      | Ordning   | Grunnbeløp   |
      | <Ordning> | <Grunnbeløp> |
    Så skal øvre lønnsgrense for perioden være <Øvre lønnsgrense>
    Eksempler:
      | Ordning | Grunnbeløp | Øvre lønnsgrense |
      | SPK     | kr 90 000  | kr 1 080 000     |
      | OPERA   | kr 90 000  | kr 1 080 000     |
      | POA     | kr 90 000  | kr 900 000       |

  Scenariomal: Øvre lønnsgrense avkortes med stillingsprosent
    Gitt en underlagsperiode med følgende innhold:
      | Ordning | Grunnbeløp | Stillingsprosent   |
      | SPK     | kr 90 000  | <Stillingsprosent> |
    Så skal øvre lønnsgrense for perioden være <Øvre lønnsgrense>
    Eksempler:
      | Stillingsprosent | Øvre lønnsgrense |
      | 100%             | kr 1 080 000     |
      | 50%              | kr 540 000       |
      | 25%              | kr 270 000       |

  Scenariomal: Unntak: Øvre lønnsgrense avkortes ikke med stillingsprosent for medregning
    Gitt en underlagsperiode med følgende innhold:
      | Ordning | Grunnbeløp | Stillingsprosent   | Er medregning |
      | SPK     | kr 90 000  | <Stillingsprosent> | Ja            |
    Så skal øvre lønnsgrense for perioden være <Øvre lønnsgrense>
    Eksempler:
      | Stillingsprosent | Øvre lønnsgrense |
      | 100%             | kr 1 080 000     |
      | 50%              | kr 1 080 000     |
      | 25%              | kr 1 080 000     |






