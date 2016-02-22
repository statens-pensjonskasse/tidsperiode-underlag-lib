# encoding: utf-8
# language: no
Egenskap: Underlagsperiode merket som fakturerbar for produkt

  I FFF anses en underlagsperiode som fakturerbar for et produkt dersom avtalen
  som er koblet til underlagsperioden har produktet, og produktinfo tilsier at
  produktet er fakturerbart.

  Det kun produktene YSK og GRU som bruker produktinfo for å avgjøre om produktet er fakturerbart.
  For PEN, AFP og TIP regnes produktet som fakturerbart dersom avtalen har produktet,
  uavhengig av produktinfo.

  I framtiden kan det hende at PEN, AFP og TIP også vil benytte produktinfo for å avgjøre om
  produktet skal faktureres, slik at beskrivelsen som er angitt i PUMA stemmer overens med løsningen.

  Forklarende tekst for produktinfo hentet fra PUMA:

  PEN
  10 - Omfattet, betaler premie
  11 - Omattet, betaler ikke premie
  13 - Ikke omfattet

  AFP
  41 - Omfattes fra 62-67, betaler itet
  42 - Omfattes fra 62-67, betaler premie
  44 - Omfattes fra 64-67, betaler premie
  45 - Omfattes fra 65-67, betaler premie
  48 - Ikke avklart
  49 - Omfattes ikke

  TIP
  94 - Omfattet

  YSK
  71 - Lov + hovedtariffavtale
  72 - Lov
  73 - Egen avtale, utvidet dekning
  74 - Deler av ansatte omfattet (ikke HTA)
  76 - Deler av ansatte omfattet (ikke HTA)
  76 - Ikke omfattet

  GRU
  31 - Omfattes, betaler ikke premie
  36 - Omfattes, betaler premie
  36 - Valgt, betaler premie
  37 - Ikke avklart forhold
  39 - Omfattes ikke

  FFF støtter ikke produktene VAR, VEN og FTP.

  Scenario: Underlagsperiode med avtale uten produkter er ikke fakturerbar for noen produkter
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden ikke har noen produkter
    Så er underlagsperioden merket som ikke fakturerbar for YSK
    Og ikke fakturerbar for GRU
    Og ikke fakturerbar for PEN
    Og ikke fakturerbar for AFP
    Og ikke fakturerbar for TIP


  Scenariomal: YSK-produkt skal ikke fakturerers for produktinfo 35 og 36
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden merket som <Fakturerbar/Ikke fakturerbar> for YSK
    Eksempler:
      | Produktinfo | Fakturerbar/Ikke fakturerbar |
      | 71          | Fakturerbar                  |
      | 72          | Fakturerbar                  |
      | 73          | Fakturerbar                  |
      | 74          | Fakturerbar                  |
      | 76          | Fakturerbar                  |
      | 79          | Ikke fakturerbar             |


  Scenariomal: GRU-produkt skal kun fakturerers for produktinfo 35 og 36
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden merket som <Fakturerbar/Ikke fakturerbar> for GRU
    Eksempler:
      | Produktinfo | Fakturerbar/Ikke fakturerbar |
      | 31          | Ikke fakturerbar             |
      | 35          | Fakturerbar                  |
      | 36          | Fakturerbar                  |
      | 37          | Ikke fakturerbar             |
      | 39          | Ikke fakturerbar             |


  Scenariomal: Underlagsperioder med PEN-produkt skal fakturerers for alle produktinfo
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | PEN     | <Produktinfo> | 8%                 | 2%            | 0.35%                |
    Så er underlagsperioden merket som <Fakturerbar/Ikke fakturerbar> for PEN
    Eksempler:
      | Produktinfo | Fakturerbar/Ikke fakturerbar |
      | 10          | Fakturerbar                  |
      | 11          | Fakturerbar                  |
      | 13          | Fakturerbar                  |


  Scenariomal: Underlagsperioder med AFP-produkt skal fakturerers for alle produktinfo
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | AFP     | <Produktinfo> | 8%                 | 2%            | 0.35%                |
    Så er underlagsperioden merket som <Fakturerbar/Ikke fakturerbar> for AFP
    Eksempler:
      | Produktinfo | Fakturerbar/Ikke fakturerbar |
      | 41          | Fakturerbar                  |
      | 42          | Fakturerbar                  |
      | 44          | Fakturerbar                  |
      | 45          | Fakturerbar                  |
      | 48          | Fakturerbar                  |
      | 49          | Fakturerbar                  |


  Scenariomal: Underlagsperioder med AFP-produkt skal fakturerers for alle produktinfo
    Gitt en underlagsperiode med følgende innhold:
      | Avtaleid |
      | 1        |
    Og at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | TIP     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden merket som <Fakturerbar/Ikke fakturerbar> for TIP
    Eksempler:
      | Produktinfo | Fakturerbar/Ikke fakturerbar |
      | 94          | Fakturerbar                  |










