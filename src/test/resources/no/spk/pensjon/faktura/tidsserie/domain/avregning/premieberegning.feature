# encoding: cp1252
# language: no
Egenskap: Beregne premiebeløp pr produkt og underlagsperiode

  For å beregne riktig årspremie pr premieår, må det for hver underlagsperiode beregnes en årspremieandel for hvert
  produkt som avtalen har i perioden.

  For hvert produkt skal årspremieandelen deles opp i medlemsandel, arbeidsgiverandel og administrasjonsgebyr.

  Premiebeløpet som beregnes skal ikke ta hensyn til hvilken premiestatus eller premiekategori avtalen er tilknyttet i
  perioden. Dette betyr at både avtaler som er ikke-premiebetalende og avtaler som faktureres basert på hendelsesbasert
  faktureringsmodell, kan få beregnet et premiebeløp ulik kr 0 dersom de ligger inne premiesatser ulik 0% for et eller
  flere av produktene.

  Ved beregning av premiebeløp forventes det at premiesatsene for PEN, AFP og TIP, inneholder prosentsatser med 2
  desimaler. Dersom det ved en feil ligger inne premiesatser med mer enn 2 desimaler, blir premiesatsene rundet
  av til 2 desimaler før de multipliseres med pensjonsgivende årslønn (som alltid er avrundet til nærmeste hele krone).

  Unntaksregler:

  For produktene YSK og GRU skal premieandelen foreløpig settes lik kr 0 i påvente av at FAN og KDF bestemmer hvordan
  forsikringsproduktene skal avregnes.

  Scenariomal: Hovedregel, ordinær beregning av premiebeløp for pensjonsproduktene for ordinære avtaler
    Gitt en underlagsperiode med følgende innhold:
      | Pensjonsgivende lønn |
      | kr 600 000           |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    Så skal totalt premiebeløp for produkt <produkt> være lik <premiebeløp>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebeløp |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av årspremieandel for pensjonsproduktene for ikke-premiebetalende avtaler
    Gitt en underlagsperiode med følgende innhold:
      | Pensjonsgivende lønn | Premiestatus |
      | kr 600 000           | IPB          |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    Så skal totalt premiebeløp for produkt <produkt> være lik <premiebeløp>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebeløp |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av årspremieandel for pensjonsproduktene for hendelsesbaserte avtaler
    Gitt en underlagsperiode med følgende innhold:
      | Pensjonsgivende lønn | Premiekategori |
      | kr 600 000           | LOP            |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    Så skal totalt premiebeløp for produkt <produkt> være lik <premiebeløp>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebeløp |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av årspremieandel for forsikringsproduktene
    Gitt en underlagsperiode med følgende innhold:
      | Årsfaktor | Årsverk |
      | 1         | 1       |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    Så skal totalt premiebeløp for produkt <produkt> være lik <premiebeløp>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebeløp |
      | YSK     | kr 0        | kr 560           | kr 35                    | kr 0        |
      | GRU     | kr 0        | kr 1150          | kr 35                    | kr 0        |

  Scenariomal: Premiebeløp avrundes til 2 desimaler, i henhold til ordinære avrundingsregler
    Gitt en underlagsperiode med følgende innhold:
      | Pensjonsgivende lønn |
      | <lønn>               |
    Og premiesats er lik 0%, <arbeidsgiversats> og 0% for produkt PEN
    Så skal totalt premiebeløp for produkt PEN være lik <premiebeløp>

    Eksempler:
      | lønn   | arbeidsgiversats | premiebeløp |
      | kr 100 | 0.01%            | kr 0.01     |
      | kr 100 | 0.35%            | kr 0.35     |
      | kr 10  | 0.01%            | kr 0.00     |
      | kr 10  | 0.05%            | kr 0.01     |
      | kr 10  | 0.06%            | kr 0.01     |
      | kr 1   | 1.50%            | kr 0.02     |
      | kr 1   | 2.50%            | kr 0.03     |
      | kr 1   | 3.50%            | kr 0.04     |
      | kr 1   | 4.50%            | kr 0.05     |
      | kr 1   | -5.50%           | kr -0.06    |

  Scenariomal: Premiesats avrundes til 2 desimaler før den ganges med lønn
    Gitt en underlagsperiode med følgende innhold:
      | Pensjonsgivende lønn |
      | <lønn>               |
    Og premiesats er lik 0%, <arbeidsgiversats> og 0% for produkt PEN
    Så skal totalt premiebeløp for produkt PEN være lik <premiebeløp>

    Eksempler:
      | lønn       | arbeidsgiversats | premiebeløp |
      | kr 100 000 | 0.001%           | kr  0.00    |
      | kr 100 000 | 0.0049%          | kr  0.00    |
      | kr 100 000 | 0.0050%          | kr 10.00    |