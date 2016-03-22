# encoding: utf-8
# language: no
Egenskap: Faktureringsandel for yrksesskadeforsikring

  Yrkesskade er et forsikringspordukt der beregningene av premie må skje per medlem, og ikke per stillingsforhold.
  Yrkesskadepremie blir betalt basert på antall årsverk medlemmet er aktivt i løpet av et år,
  avkortet til maksimalt ett årsverk.

  Ettersom premien er på medlemsnivå, må man ha en strategi for perioder hvor medlemmet har flere paralelle
  stillinger, for å avgjøre hvilke stillingsforhold og avtaler som skal betale premie i en periode.

  SPK har valgt å bruke stillingsstørrelse som strategi for hvilke stillinger og avtaler som skal dekke premien.
  Dersom stillingstørrelsene er like store, brukes stillingsforholdid for å avgjøre rekkefølgen, slik at resultatet
  blir deterministisk mellom kjøringer på underlag.

  Stillinger som oppfyller ett eller flere av følgende regler, skal ikke betale yrkesskadepremie.
  * Stillinger tilknyttet avtaler som ikke er fakturerbare for YSK, skal ikke betale yrkesskadepremie.
  * Stillinger tilknyttet medregning skal ikke betale yrkesskadepremie.
  * Stillinger some er ute i permisjon uten lønn skal ikke ha yrkesskadepremie for perioden permisjonen gjelder.

  Dersom en stilling kan faktureres for yrkesskadepremie fordeles premien på følgende måte:
  * Plukk stillingen med størst stillingsprosent. Avtalen skal betale tilsvarende andel av yrkesskadepremie.
  * Fortsett å plukke neste stilling med størst stillingsprosent fra resterende stillinger.
  * Dersom en plukket stilling fører til at samlet stillingsprosent overstiger 100%, skal stillingsprosenten til stillingen
  * avkortes slik at samlet stillingsprosent gir 100%.
  * Stillinger som plukkes etter at samlet stillingsprosent har nådd 100% skal ikke betale yrkesskadepremie (avkortes til 0%,
  * implisitt gitt av regelen over).
  * Dersom flere stillinger har samme stillingsprosent i perioden, plukkes stillingen med lavest stillingsforholdid først.

  Scenario: Faktureringsandel for YSK er 0% når avtalen for stillingen ikke er fakturerbar for YSK
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold | Avtale |
      | 1                | 1      |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 011         | 1      |
    Og avtalen for underlagsperioden ikke har noen produkter
    Så har stillingsforhold 1 faktureringsandel for YSK lik 0% i perioden
    Og fordelingsårsak for YSK lik "avtale ikke fakturerbar for produkt"

  Scenario: Faktureringsandel for YSK er 0% når stillingen er ute i permisjon uten lønn (aksjonskode 28) i perioden
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 028         | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for YSK lik 0% i perioden
    Og fordelingsårsak for YSK lik "er permisjon uten lønn"


  Scenario: Faktureringsandel for YSK er 0% når stillingen er en medregning
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Er medregning | Avtale |
      | 1                | Ja            | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for YSK lik 0% i perioden
    Og fordelingsårsak for YSK lik "er medregning"


  Scenario: Faktureringsandel for YSK er lik stillingsprosent når det ikke er paralelle stillingsforhold
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 50%              | 011         | 1      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for YSK lik 50% i perioden
    Og fordelingsårsak for YSK lik "ordinær"


  Scenario: Faktureringsandel for YSK for parallelle stillinger
  Faktureringsandel for YSK skal avkortes for stillingen med minst stillingsprosent dersom
  samlet stillingsprosent overstiger 100%.
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 1                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 40%              | 021         | 1      |
      | 2                | 90%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 1 faktureringsandel for YSK lik 10% i perioden
    Og fordelingsårsak for YSK lik "avkortet"


  Scenario: Parallelle stillinger med samme stillingsprosent: Stillingsforhold 2
  Dersom flere stillinger har samme stillingsprosent i perioden, avkortes faktureringsandelen for YSK
  for stillingen med høyest stillingsforholdnummer først, dersom samlet stillingsprosent overstiger 100%.
    Gitt en underlagsperiode med følgende innhold:
      | Stillingsforhold |
      | 2                |
    Og underlagsperioden er koblet til følgende aktive stillinger:
      | Stillingsforhold | Stillingsprosent | Aksjonskode | Avtale |
      | 1                | 60%              | 021         | 1      |
      | 2                | 60%              | 021         | 2      |
    Og avtale 1 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Og avtale 2 har følgende produkt:
      | Produkt | Produktinfo | Arbeidsgiverpremie | Medlemspremie | Administrasjonsgebyr |
      | YSK     | 71          | kr  500            | kr 0          | kr 100               |
    Så har stillingsforhold 2 faktureringsandel for YSK lik 40% i perioden
    Og fordelingsårsak for YSK lik "avkortet"













