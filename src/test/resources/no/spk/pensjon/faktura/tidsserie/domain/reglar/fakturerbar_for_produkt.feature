# encoding: utf-8
# language: no
Egenskap: Underlagsperiode merket som fakturerbar for produkt

  I FFF anses en underlagsperiode som fakturerbar for et produkt dersom avtalen
  som er koblet til underlagsperioden har produktet, og produktinfo tilsier at
  produktet er fakturerbart. I tilegg må produktet ha minst én premiesats forskjellig fra 0.

  Det kun produktene YSK og GRU som bruker produktinfo for å avgjøre om produktet er fakturerbart.
  For PEN, AFP og TIP regnes produktet som fakturerbart dersom avtalen har produktet,
  uavhengig av produktinfo.

  I framtiden kan det hende at PEN, AFP og TIP også vil benytte produktinfo for å avgjøre om
  produktet skal faktureres, slik at beskrivelsen som er angitt i PUMA stemmer overens med løsningen.

  Forklarende tekst for produktinfo hentet fra PUMA:

  **PEN**
  * 10 - Omfattet, betaler premie
  * 11 - Omfattet, betaler ikke premie
  * 13 - Ikke omfattet

  **AFP**
   * 41 - Omfattes fra 62-67, betaler intet
   * 42 - Omfattes fra 62-67, betaler premie
   * 44 - Omfattes fra 64-67, betaler premie
   * 45 - Omfattes fra 65-67, betaler premie
   * 48 - Ikke avklart
   * 49 - Omfattes ikke

  **TIP**
   * 94 - Omfattet

  **YSK**
   * 71 - Lov + hovedtariffavtale
   * 72 - Lov
   * 73 - Egen avtale, utvidet dekning
   * 74 - Deler av ansatte omfattet (ikke HTA)
   * 76 - Deler av ansatte omfattet (ikke HTA)
   * 76 - Ikke omfattet

  **GRU**
   * 31 - Omfattes, betaler ikke premie
   * 36 - Omfattes, betaler premie
   * 36 - Valgt, betaler premie
   * 37 - Ikke avklart forhold
   * 39 - Omfattes ikke

  FFF støtter ikke produktene VAR, VEN og FTP.

  Bakgrunn:
    Gitt en underlagsperiode med følgende innhold:
      | Avtale |
      | 1      |


  Scenario: En underlagsperiode med avtale uten produkter er ikke fakturerbar for noen produkter
    Gitt at avtalen for underlagsperioden ikke har noen produkter
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | YSK     | Nei                        |
      | GRU     | Nei                        |
      | PEN     | Nei                        |
      | AFP     | Nei                        |
      | TIP     | Nei                        |

  Scenario: En underlagsperiode med avtale hvor alle produkter har 0 i premiesats er ikke fakturerbar for noen produkter
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  0              | kr 0          | kr 0                 |
      | GRU     | 35          | kr  0              | kr 0          | kr 0                 |
      | PEN     | 10          | 0%                 | 0%            | 0%                   |
      | AFP     | 42          | 0%                 | 0%            | 0%                   |
      | TIP     | 94          | 0%                 | 0%            | 0%                   |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | YSK     | Nei                        |
      | GRU     | Nei                        |
      | PEN     | Nei                        |
      | AFP     | Nei                        |
      | TIP     | Nei                        |

  Scenariomal: YSK-produkt skal ikke faktureres for produktinfo 79
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | YSK     | <Er YSK fakturerbart?>     |
    Eksempler:
      | Produktinfo | Er YSK fakturerbart? |
      | 71          | Ja                   |
      | 72          | Ja                   |
      | 73          | Ja                   |
      | 74          | Ja                   |
      | 76          | Ja                   |
      | 79          | Nei                  |


  Scenariomal: GRU-produkt skal kun faktureres for produktinfo 35 og 36
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | GRU     | <Er GRU fakturerbart?>     |
    Eksempler:
      | Produktinfo | Er GRU fakturerbart? |
      | 31          | Nei                  |
      | 35          | Ja                   |
      | 36          | Ja                   |
      | 37          | Nei                  |
      | 39          | Nei                  |


  Scenariomal: Underlagsperioder med PEN-produkt skal fakturerers for alle produktinfo
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | PEN     | <Produktinfo> | 8%                 | 2%            | 0.35%                |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | PEN     | <Er PEN fakturerbart?>     |
    Eksempler:
      | Produktinfo | Er PEN fakturerbart? |
      | 10          | Ja                   |
      | 11          | Ja                   |
      | 13          | Ja                   |


  Scenariomal: Underlagsperioder med AFP-produkt skal fakturerers for alle produktinfo
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | AFP     | <Produktinfo> | 8%                 | 2%            | 0.35%                |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | AFP     | <Er AFP fakturerbart?>     |
    Eksempler:
      | Produktinfo | Er AFP fakturerbart? |
      | 41          | Ja                   |
      | 42          | Ja                   |
      | 44          | Ja                   |
      | 45          | Ja                   |
      | 48          | Ja                   |
      | 49          | Ja                   |


  Scenariomal: Underlagsperioder med TIP-produkt skal fakturerers for alle produktinfo
    Gitt at avtalen for underlagsperioden har følgende produkt:
      | Produkt | Produktinfo   | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | TIP     | <Produktinfo> | kr  500            | kr 0          | kr 100               |
    Så er underlagsperioden fakturerbar for følgende produkt:
      | Produkt | Er produktet fakturerbart? |
      | TIP     | <Er TIP fakturerbart?>     |
    Eksempler:
      | Produktinfo | Er TIP fakturerbart? |
      | 94          | Ja                   |










