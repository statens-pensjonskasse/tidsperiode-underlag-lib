# encoding: utf-8
# language: no
Egenskap: Beregne pensjonsgivende lønn for en underlagsperiode

  Pensjonsgivende lønn for en underlagsperiode er definert som:

  Grunnlag for pensjonsgivende lønn * årsfaktor.

  Grunnlag for pensjonsgivende lønn er lik regel deltidsjustert lønn + lønnstilegg + medregning.
  Grunnlag for pensjonsgivende lønn kan ikke være høyere enn øvre lønnsgrense i perioden.
  Årsfaktor er antall dager underlagsperioden strekker seg over, delt på antall dager i året.

  En underlagsperiode med stillingsprosent som er under minstegrensen har pensjonsgivende lønn lik  kr 0.

  Bakgrunn: Verdier som benyttes for underlagsperioden dersom ikke annet er oppgitt
    Gitt en underlagsperiode med følgende innhold:
      | Årsfaktor | Er under minstegrensen | Regel deltidsjustert lønn | Lønnstillegg | Medregning | Øvre lønnsgrense |
      | 1         | Nei                    | kr 0                      | kr 0         | kr 0       | kr 1 500 000     |

  Scenariomal: Grunnlaget for pensjonsgivende lønn er summen av deltidsjustert lønn, lønnstilegg og medregning
    Gitt en underlagsperiode med følgende innhold:
      | Regel deltidsjustert lønn   | Lønnstillegg   | Medregning   |
      | <Regel deltidsjustert lønn> | <Lønnstillegg> | <Medregning> |
    Så skal pensjonsgivende lønn for perioden være <Pensjonsgivende lønn>
    Eksempler:
      | Regel deltidsjustert lønn | Lønnstillegg | Medregning | Pensjonsgivende lønn |
      | kr 200 000                | kr 0         | kr 0       | kr 200 000           |
      | kr 200 000                | kr 100 000   | kr 0       | kr 300 000           |
      | kr 0                      | kr 0         | kr 100 000 | kr 100 000           |

  Scenario: Grunnlaget for pensjonsgivende lønn avkortes til øvre lønnsgrense for underlagsperioden
    Gitt en underlagsperiode med følgende innhold:
      | Regel deltidsjustert lønn | Lønnstillegg | Øvre lønnsgrense |
      | kr 600 000                | kr 600 000   | kr 1 000 000     |
    Så skal pensjonsgivende lønn for perioden være kr 1 000 000

  Scenariomal: Pensjonsgivende lønn er grunnlaget for pensjonsgivende lønn multiplisert med årsfaktor for underlagsperioden
    Gitt en underlagsperiode med følgende innhold:
      | Årsfaktor   | Regel deltidsjustert lønn   |
      | <Årsfaktor> | <Regel deltidsjustert lønn> |
    Så skal pensjonsgivende lønn for perioden være <Pensjonsgivende lønn>
    Eksempler:
      | Årsfaktor | Regel deltidsjustert lønn | Pensjonsgivende lønn |
      | 1         | kr 200 000                | kr 200 000           |
      | 0.5       | kr 200 000                | kr 100 000           |
      | 0.25      | kr 200 000                | kr 50 000            |

  Scenario: En underlagsperiode med stillingsprosent under minstegrensen har kr 0 i pensjonsgivende lønn
    Gitt en underlagsperiode med følgende innhold:
      | Er under minstegrensen | Regel deltidsjustert lønn |
      | Ja                     | kr 100 000                |
    Så skal pensjonsgivende lønn for perioden være kr 0






