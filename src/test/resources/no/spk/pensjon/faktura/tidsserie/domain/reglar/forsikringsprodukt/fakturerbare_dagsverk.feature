# encoding: utf-8
# language: no
Egenskap: Fakturerbare dagsverk for forsikringsproduktene

  Fakturerbare dagsverk for forsikringsproduktene YSK og GRU for en underlagsperiode, beregnes som:
  <antall dager i perioden> * <faktureringsandel for produktet>

  Fakturerbare dagsverk skal ha 5 desimaler.

  Scenariomal: Fakturerbare dagsverk for YSK
    Gitt en underlagsperiode med følgende innhold:
      | Fra og med   | Til og med   | Yrkesskadeandel   |
      | <Fra og med> | <Til og med> | <Yrkesskadeandel> |
    Så er fakturerbare dagsverk for YSK <Fakturerbare dagsverk> i perioden
    Eksempler:
      | Fra og med | Til og med | Yrkesskadeandel | Fakturerbare dagsverk |
      | 2015.01.01 | 2015.01.10 | 100             | 10.00000              |
      | 2015.01.01 | 2015.01.10 | 50              | 5.00000               |
      | 2015.01.01 | 2016.01.01 | 0               | 0.00000               |
      | 2015.01.01 | 2015.01.31 | 37.456          | 11.61136              |

  Scenariomal: Fakturerbare dagsverk for GRU
    Gitt en underlagsperiode med følgende innhold:
      | Fra og med   | Til og med   | Gruppelivandel   |
      | <Fra og med> | <Til og med> | <Gruppelivandel> |
    Og underlagsperioden sin fra og med-dato er <Fra og med>
    Og underlagsperioden sin til og med-dato er <Til og med>
    Så er fakturerbare dagsverk for GRU <Fakturerbare dagsverk> i perioden
    Eksempler:
      | Fra og med | Til og med | Gruppelivandel | Fakturerbare dagsverk |
      | 2015.01.01 | 2015.01.10 | 100            | 10.00000              |
      | 2015.01.01 | 2016.01.01 | 0              | 0.00000               |
