# encoding: utf-8
# language: no
Egenskap: Antall feil for en underlagsperiode

  Antall feil i live-tidsserie- og avregnings-formatet for en underlagsperiode
  angir hvor mange verdier som ikke kunne beregnes for underlagsperioden som følge av teknisk feil.

  Når antall feil er forskjllig fra 0, betyr det at en eller fler av kolonnene i tidsserie-formatet ikke
  kunne beregnes. Det angis ikke hvilke verdi som har feilet, og det vil ofte være nødvendig med
  en teknisk gransking av grunnlagsdataene for å avgjøre årsaken til feilen.

  Dersom det er mange underlagsperioder (over halvparten)  som har antall feil forskjellig fra 0, kan dette indikere en systematisk
  feil i beregningsmotoren.

  Kolonner som kan hentes direkte fra underlagsperioden, og som ikke er sammensatt av flere verdier, vil ikke
  øke antall feil dersom verdien mangler i underlaget.

  Eksempler på kolonner som *ikke* øker antall feil dersom verdien ikke kunne bestemmes:
  * Orgnummer
  * Ordning
  * Premiestatus

  Kolonner som utledes fra én eller flere verdier i underlagsperioden, vil øke antall feil med én dersom underlagsverdiene kolonna er avhengig av mangler.
  Antall feil vil da økes med én.

  Eksempler på kolonner som øker antall feil dersom verdien ikke kunne bestemmes:
  * Regel pensjonsgivende lønn
  * Regel lønnstillegg
  * Regel minstegrense

  Antall feil indikerer altså ikke noe om hel- og halvfeil som har skjedd i ved innrapportering,
  eller andre logiske feil i grunnlagsdata som er benyttet for å lage tidsserien.

  Scenario: Beregning av regel 'er under minstegrense' for underlagsperiode uten ordning, øker antall feil med én.
    Regelen "Er under minstegrense" benytter ordning for å bestemme minstegrense. Dersom
    ordningen mangler, vil beregning av minstegrense feile, og antall feil økes med en.
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsprosent |
      | 20%              |
    Når beregning av regel 'er under minstegrense' er utført
    Så skal antall feil for perioden være 1

  Scenario: Hente ordning for underlagsperiode uten ordning fører ikke til feil
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsprosent |
      | 20%              |
    Når verdi for ordning hentes for perioden
    Så skal antall feil for perioden være 0