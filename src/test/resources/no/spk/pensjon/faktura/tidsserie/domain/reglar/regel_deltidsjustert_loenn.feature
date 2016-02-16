# encoding: utf-8
# language: no
Egenskap: Beregne regel deltidsjustert lønn for en underlagsperiode

  Arbeidsgivere innrapporterer lønnsnivå for sine medlemmer enten i kroner eller lønnstrinn.

  Deltidsjuster lønn gis direkte fra en stillingsendring som er innrapportert i kroner.
  Verdien er da allerede justert for stillingsandelen til medlemmet.

  Stillingsendringer som er innrapportert i lønnstrinn må regnes om til lønnstrinnbeløp i kroner.

  Regel deltidsjustert lønn for en underlagsperiode sammenstiller deltidsjustert lønn og lønnstrinnbeløp til et felles
  kronebeløp.

  For underlagsperioder med lønnstrinn er regel deltidsjustert lønn lik lønnstrinnbeløp * stillingsprosent.
  For underlagsperioder uten lønnstrinn er regel deltidsjuster lønn lik deltidsjustert lønn for perioden.

  Bakgrunn: Verdier for underlagsperioden dersom ikke annet er oppgitt
    Gitt en underlagsperiode med følgende innhold:
      | Er medregning | Er permisjon uten lønn |
      | Nei           | Nei                    |

  Scenario: Regel deltidsjustert lønn for periode uten lønnstrinn er lik deltidsjustert lønn
    Gitt en underlagsperiode med følgende innhold:
      | Deltidsjustert lønn |
      | kr 500 000          |
    Så skal regel deltidsjustert lønn for perioden være kr 500 000

  Scenariomal: Regel deltidsjustert lønn for periode med lønnstrinn
    Gitt en underlagsperiode med følgende innhold:
      | Lønnstrinn   | Lønnstrinn beløp   | Stillingsprosent   |
      | <Lønnstrinn> | <Lønnstrinn beløp> | <Stillingsprosent> |
    Så skal regel deltidsjustert lønn for perioden være <Regel deltidsjustert lønn>
    Eksempler:
      | Lønnstrinn | Lønnstrinn beløp | Stillingsprosent | Regel deltidsjustert lønn |
      | 20         | kr 20 000        | 100 %            | kr 20 000                 |
      | 10         | kr 10 000        | 100 %            | kr 10 000                 |
      | 29         | kr 20 000        | 50 %             | kr 10 000                 |

  Scenario: Regel deltidsjustert lønn for periode som er medregning skal være kr 0
    Gitt en underlagsperiode med følgende innhold:
      | Er medregning | Medregning |
      | Ja            | kr 500 000 |
    Så skal regel deltidsjustert lønn for perioden være kr 0

  Scenario: Regel deltidsjustert lønn for periode som er permisjon uten lønn skal være kr 0
    Gitt en underlagsperiode med følgende innhold:
      | Er permisjon uten lønn |
      | Ja                     |
    Så skal regel deltidsjustert lønn for perioden være kr 0






