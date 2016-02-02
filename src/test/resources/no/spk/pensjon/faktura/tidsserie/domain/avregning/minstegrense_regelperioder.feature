# encoding: utf-8
# language: no
Egenskap: Minstegrenseregler ble endret 1. april 2016

  Gjeldende minstegrense for fakturering har endret seg over tid. I årene 2013 til og med 31. mars 2016 har fastasts-metodikken
  operert med ett regelsett, og fom. 1. april 2016 benyttes et annet.

  Ved prognoseberegning ønsker man ikke variasjon i minstegrensereglene - og skal benytte samme minstegrenseregel for alle underlagsperioder.
  Prognoseberegningen fom. 2016 skal benytte nye minstegrenseregler.

  Scenario: Periode for minstegrense før 1.april med avregningsregler
    Gitt underlagsperioden sin fra og med-dato er 2015.01.01
    Og underlagsperioden sin til og med-dato er 2016.03.31
    Og underlagsperioden benytter regler for avregning
    Og en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent | Premiestatus |
      | SPK     | 20,0%            | AAO-01       |
    Så er stillingen under minstegrensen

  Scenario: Periode for minstegrense etter 1.april ved avregningsregler
    Gitt underlagsperioden sin fra og med-dato er 2016.04.01
    Og underlagsperioden sin til og med-dato er 2016.12.31
    Og underlagsperioden benytter regler for avregning
    Og en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent | Premiestatus |
      | SPK     | 20,0%            | AAO-01       |
    Så er stillingen over minstegrensen

  Scenario: Regelperiode for minstegrense ved prognose
    Gitt underlagsperioden sin fra og med-dato er 2001.01.01
    Og underlagsperioden sin til og med-dato er 2016.12.31
    Og underlagsperioden benytter regler for prognose
    Og en underlagsperiode med følgende innhold:
      | Ordning | Stillingsprosent | Premiestatus |
      | SPK     | 20,0%            | AAO-01       |
    Så er stillingen over minstegrensen


