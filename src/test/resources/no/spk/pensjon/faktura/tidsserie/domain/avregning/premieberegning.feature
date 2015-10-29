# encoding: cp1252
# language: no
Egenskap: Beregne premiebel�p pr produkt og underlagsperiode

  For � beregne riktig �rspremie pr premie�r, m� det for hver underlagsperiode beregnes en �rspremieandel for hvert
  produkt som avtalen har i perioden.

  For hvert produkt skal �rspremieandelen deles opp i medlemsandel, arbeidsgiverandel og administrasjonsgebyr.

  Premiebel�pet som beregnes skal ikke ta hensyn til hvilken premiestatus eller premiekategori avtalen er tilknyttet i
  perioden. Dette betyr at b�de avtaler som er ikke-premiebetalende og avtaler som faktureres basert p� hendelsesbasert
  faktureringsmodell, kan f� beregnet et premiebel�p ulik kr 0 dersom de ligger inne premiesatser ulik 0% for et eller
  flere av produktene.

  Ved beregning av premiebel�p forventes det at premiesatsene for PEN, AFP og TIP, inneholder prosentsatser med 2
  desimaler. Dersom det ved en feil ligger inne premiesatser med mer enn 2 desimaler, blir premiesatsene rundet
  av til 2 desimaler f�r de multipliseres med pensjonsgivende �rsl�nn (som alltid er avrundet til n�rmeste hele krone).

  Unntaksregler:

  For produktene YSK og GRU skal premieandelen forel�pig settes lik kr 0 i p�vente av at FAN og KDF bestemmer hvordan
  forsikringsproduktene skal avregnes.

  Scenariomal: Hovedregel, ordin�r beregning av premiebel�p for pensjonsproduktene for ordin�re avtaler
    Gitt en underlagsperiode med f�lgende innhold:
      | Pensjonsgivende l�nn |
      | kr 600 000           |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    S� skal totalt premiebel�p for produkt <produkt> v�re lik <premiebel�p>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebel�p |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av �rspremieandel for pensjonsproduktene for ikke-premiebetalende avtaler
    Gitt en underlagsperiode med f�lgende innhold:
      | Pensjonsgivende l�nn | Premiestatus |
      | kr 600 000           | IPB          |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    S� skal totalt premiebel�p for produkt <produkt> v�re lik <premiebel�p>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebel�p |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av �rspremieandel for pensjonsproduktene for hendelsesbaserte avtaler
    Gitt en underlagsperiode med f�lgende innhold:
      | Pensjonsgivende l�nn | Premiekategori |
      | kr 600 000           | LOP            |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    S� skal totalt premiebel�p for produkt <produkt> v�re lik <premiebel�p>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebel�p |
      | PEN     | 2%          | 10%              | 0.35%                    | kr 74 100   |
      | AFP     | 0%          | 5%               | 0%                       | kr 30 000   |
      | TIP     | 0%          | 50%              | 0%                       | kr 300 000  |

  Scenariomal: Beregning av �rspremieandel for forsikringsproduktene
    Gitt en underlagsperiode med f�lgende innhold:
      | �rsfaktor | �rsverk |
      | 1         | 1       |
    Og premiesats er lik <medlemssats>, <arbeidsgiversats> og <administrasjonsgebyrsats> for produkt <produkt>
    S� skal totalt premiebel�p for produkt <produkt> v�re lik <premiebel�p>

    Eksempler:
      | produkt | medlemssats | arbeidsgiversats | administrasjonsgebyrsats | premiebel�p |
      | YSK     | kr 0        | kr 560           | kr 35                    | kr 0        |
      | GRU     | kr 0        | kr 1150          | kr 35                    | kr 0        |

  Scenariomal: Premiebel�p avrundes til 2 desimaler, i henhold til ordin�re avrundingsregler
    Gitt en underlagsperiode med f�lgende innhold:
      | Pensjonsgivende l�nn |
      | <l�nn>               |
    Og premiesats er lik 0%, <arbeidsgiversats> og 0% for produkt PEN
    S� skal totalt premiebel�p for produkt PEN v�re lik <premiebel�p>

    Eksempler:
      | l�nn   | arbeidsgiversats | premiebel�p |
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

  Scenariomal: Premiesats avrundes til 2 desimaler f�r den ganges med l�nn
    Gitt en underlagsperiode med f�lgende innhold:
      | Pensjonsgivende l�nn |
      | <l�nn>               |
    Og premiesats er lik 0%, <arbeidsgiversats> og 0% for produkt PEN
    S� skal totalt premiebel�p for produkt PEN v�re lik <premiebel�p>

    Eksempler:
      | l�nn       | arbeidsgiversats | premiebel�p |
      | kr 100 000 | 0.001%           | kr  0.00    |
      | kr 100 000 | 0.0049%          | kr  0.00    |
      | kr 100 000 | 0.0050%          | kr 10.00    |