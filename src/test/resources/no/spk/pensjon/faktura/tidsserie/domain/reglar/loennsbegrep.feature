# encoding: utf-8
# language: no
Egenskap: Lønnsbegrep som benyttes for en underlagsperiode

  I FFF-metodikken er det flere forskjellige begrep knyttet til lønn.

  Arbeidsgivere innrapporterer stillingsendringer med lønnsnivå for sine medlemmer enten i kroner eller lønnstrinn.

  *Deltidsjustert lønn*
  Deltidsjustert lønn beskriver nivået på årslønn for en stilling, justert med stillingsprosenten som gjelder for stillingen.
  Stillingsendringer som innrapporteres i kroner, angir deltidsjustert lønn direkte.

  Dersom en stillingsendring innrapporteres i lønnstrinn, er deltidsjustert lønn kr 0,- for stillingen.

  *Lønnstrinnbeløp*
  Lønnstrinnbeløp beskriver nivået på årslønn for en 100% stilling i kroner.
  Utifra lønnstrinn, ordning og stillingskode for en underlagsperiode, er lønnstrinnbeløpet gitt av ordningens lønnstrinntabellen i perioden.

  Dersom en stillingsendring innrapporteres i deltidsjustert lønn, er lønnstrinn 0 og lønnstrinnbeløp 0 for stillingen.

  *Regel deltidsjustert lønn*
  Regel deltidsjustert lønn sammenstiller deltidsjustert lønn og lønnstrinnbeløp for en underlagsperiode, slik at
  begge kan behandles likt.

  For underlagsperioder med lønnstrinn er regel deltidsjustert lønn lik lønnstrinnbeløp * stillingsprosent.
  For underlagsperioder uten lønnstrinn er regel deltidsjuster lønn lik deltidsjustert lønn for perioden.

  Unntak: Dersom stillingen er ute i permisjon uten lønn i perioden, eller stillingen er en medregning,
  er regel deltidsjustert lønn kr 0,-

  *Regel lønnstillegg*
  Regel lønnstillegg er lik faste tillegg + variable tillegg + funksjonstillegg.
  Lønnstillegg påvirker pensjonsgivende lønn, men inkluderes ikke i regel deltidsjustert lønn.

  Unntak: Dersom stillingen er ute i permisjon uten lønn i perioden, er regel lønnstillegg kr 0,-

  *Medregning*
  Medregning er et beløp som medlemmet har tjent opp hos en arbeidsgiver tilknyttet en annen
  ordning enn SPK-ordningen som er del av overføringsavtalen. Medregning inkluderes i pensjonsgivende lønn.

  *Pensjonsgivende lønn*
  Se egen spesifikasjon for pensjonsgivende lønn.

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

  Scenario: Regel lønnstillegg er summen av faste tillegg, variable tillegg og funksjonstillegg
    Gitt en underlagsperiode med følgende innhold:
      | Faste tillegg | Variable tillegg | Funksjonstillegg |
      | 1000          | 2000             | 3500             |
    Så skal regel lønnstilegg for perioden være kr 6500






