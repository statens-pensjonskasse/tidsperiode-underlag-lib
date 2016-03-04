# encoding: utf-8
# language: no
Egenskap: Faktureringsandel for gruppelivsforsikring

  Gruppeliv er et forsikringsprodukt der beregning av premie må skje pr medlem, ikke per stillingsforhold.
  Gruppelivspremien blir betalt basert på antall dagar medlemmet er aktivt i løpet av et år.

  Ettersom premien er på medlemsnivå, må man ha en strategi for perioder hvor medlemmet har flere paralelle
  stillinger, for å avgjøre hvilke stillingsforhold og avtaler som skal betale premie i en periode.

  SPK har valgt å bruke stillingsstørrelse som strategi for hvilke stillinger og avtaler som skal dekke premien.
  Dersom stillingstørrelsene er like store, brukes stillingsforholdid for å avgjøre rekkefølgen, slik at resultatet
  blir deterministisk mellom kjøringer på underlag.

  Stillinger som oppfyller ett eller flere av følgende regler, skal ikke betale yrkesskadepremie.
  * Stillinger tilknyttet avtaler som ikke er fakturerbare for GRU, skal ikke betale gruppelivspremie.
  * Stillinger tilknyttet medregning skal ikke betale gruppelivspremie.
  * Stillinger some er ute i permisjon uten lønn skal ikke ha gruppelivspremie for perioden permisjonen gjelder.

  Dersom en stilling kan faktureres for gruppelivspremie fordeles premien på følgende måte:
  * Stillingen med størst stillingsprosent er ansvarlig for periodens gruppelivspremie
  * Dersom flere stillinger har samme stillingsprosent i perioden, plukkes stillingen med lavest stillingsforholdid først.


  Scenario: Faktureringsandel for GRU er 0% når avtalen for stillingen ikke er fakturerbar for GRU
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold | Avtale |
      | 1                | 1      |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 011         | 1      |
    Og avtalen for underlagsperioden ikke har noen produkter
    Så har stillingsforhold 1 faktureringsandel for GRU lik 0% i perioden

  Scenario: Faktureringsandel for GRU er 0% når stillingen er ute i permisjon uten lønn (aksjonskode 28) i perioden
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 028         | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 0% i perioden


  Scenario: Faktureringsandel for GRU er 0% når stillingen er en medregning
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Er medregning | Avtale |
      | 1                | Ja            | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 0% i perioden


  Scenario: Faktureringsandel for GRU er 100% når det ikke er paralelle stillingsforhold
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 011         | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 100% i perioden


  Scenario: En stilling med høyest stillingsprosent skal ha 100% faktureringsandel for GRU
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 40%              | 021         | 1      |
      | 2                | 30%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 100% i perioden

  Scenario: En stilling som ikke har høyest stillingsprosent skal ha 0% faktureringsandel for GRU
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 30%              | 021         | 1      |
      | 2                | 40%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 0% i perioden


  Scenario: Parallelle stillinger med samme stillingsprosent: Stillingsforhold 1
  Dersom flere stillinger har samme stillingsprosent i perioden, skal stillingen med lavest
  stillingsforholdnummer ha 100% faktureringsandel for GRU

    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 60%              | 021         | 1      |
      | 2                | 60%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for GRU lik 100% i perioden

  Scenario: Parallelle stillinger med samme stillingsprosent: Stillingsforhold 2
  Dersom flere stillinger har samme stillingsprosent i perioden, skal stillingen med lavest
  stillingsforholdnummer ha 100% faktureringsandel for GRU

    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 2                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 60%              | 021         | 1      |
      | 2                | 60%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | GRU     | 35          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 2 faktureringsandel for GRU lik 0% i perioden
