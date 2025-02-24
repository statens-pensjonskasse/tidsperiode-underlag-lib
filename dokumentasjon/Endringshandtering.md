# Tidsperiode-underlag-lib - Endringshandtering

<a name="seksjon1"/>

## 1. Innholdsfortegnelse

  1. [Innholdsfortegnelse](#seksjon1)
  2. [Endringshandtering](#seksjon2)
  3. [Bakoverkompatibilitet](#seksjon3)

<a name="seksjon2"/>

## 2. Prinsipp for endringshandtering

For tidsperiode-underlag-lib må vi ha eit svært bevist forhold til korvidt endringar vi gjer på API-nivå, 
er bakoverkompatible med alle klientane som er tett kobla til tidsperiode- og underlag APIane.

Det blir forventa at alle endringar på modulen skal vere kompatible med følgjande system/applikasjonar/modular:

* faktura-tidsserie-batch
  * Modus - Avtaleunderlag
  * Modus - Avregningstidsserie
  * Modus - Live tidsserie
  * Modus - Stillingsforholdobservasjonar
* faktura-prognose-tidsserie
* Rettighetsberegning
* Analyseunderlag opptjening og rettighet

Ettersom vi ikkje ønskjer eller forventar at alle desse applikasjonane og modulane vil bli oppgradert i samme takt ved 
endringar i tidsserie-underlag-lib, ønskjer vi å holde tidsperiode-underlag-lib så stabil og uendra som 
mulig.
 
Prinsippa vi følger for å oppnå dette er som følgjer:

* Vi forsøker å holde alle endringar på modulen bakoverkompatible.
* Vi forsøker å holde APIen til modulen så liten som den må vere, men ikkje mindre.
* Vi følgjer open-closed-prinsippet ved at ny funksjonalitet helst bør kunne pluggast inn i eksisterande API 
  framfor å kreve endringar på den.
* Vi konfererer med og spør alle team som er ansvarlig for applikasjonar og modular som er avhengig av 
  tidsperiode-underlag-lib, om råd i forkant av at endringar bli innført på modulen.

<a name="seksjon3"/>

## 3. Bakoverkompatibilitet

Kva formål har vi for å ønske å vere bakoverkompatible og kva betyr det å vere bakoverkompatibel?
 
Følgjande utsnitt frå [semver.org](http://semver.org/) beskriv ganske godt kva motivasjon som ligg bak formuleringa for vår del:

> ### Summary
>    Given a version number MAJOR.MINOR.PATCH, increment the:
>
>    * MAJOR version when you make incompatible API changes,
>    * MINOR version when you add functionality in a backwards-compatible manner, and
>    * PATCH version when you make backwards-compatible bug fixes.
>
>    Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.
>
> ### Introduction
>
>    In the world of software management there exists a dread place called “dependency hell.” The bigger your system 
>    grows and the more packages you integrate into your software, the more likely you are to find yourself, one day, in 
>    this pit of despair.
>
>    In systems with many dependencies, releasing new package versions can quickly become a nightmare. If the dependency 
>    specifications are too tight, you are in danger of version lock (the inability to upgrade a package without having 
>    to release new versions of every dependent package). If dependencies are specified too loosely, you will inevitably 
>    be bitten by version promiscuity (assuming compatibility with more future versions than is reasonable).Dependency 
>    hell is where you are when version lock and/or version promiscuity prevent you from easily and safely moving your 
>    project forward.
>
>    As a solution to this problem, I propose a simple set of rules and requirements that dictate how version numbers 
>    are assigned and incremented. These rules are based on but not necessarily limited to pre-existing widespread 
>    common practices in use in both closed and open-source software. For this system to work, you first need to declare 
>    a public API. This may consist of documentation or be enforced by the code itself. Regardless, it is important that 
>    this API be clear and precise. Once you identify your public API, you communicate changes to it with specific 
>    increments to your version number. Consider a version format of X.Y.Z (Major.Minor.Patch). Bug fixes not affecting 
>    the API increment the patch version, backwards compatible API additions/changes increment the minor version, and 
>    backwards incompatible API changes increment the major version.
>
>    I call this system “Semantic Versioning.” Under this scheme, version numbers and the way they change convey meaning 
>    about the underlying code and what has been modified from one version to the next.
    
Samtidig er vi beviste på at vi skal ikkje vere bakoverkompatible for ein kvar pris, vi skal ha eit bevist forhold til 
kvifor vi gjer det. Primært kan ein seie at så lenge APIen vi har på denne modulen, ikkje begrensar oss eller hindrar 
oss frå å få til det vi vil gjere, så held vi på intensjonen om å vere bakoverkompatible med eldre versjonar.

Kjem vi ein gang fram til at abstraksjonane modulane inneheld, ikkje oppfyller noverande og framtidige behov, og at dei 
aktivt hindrar oss frå å komme vidare, og at vi etter å ha konferert med alle team som bli påvirka får ein go for å
gjere brytande endringar, då først tar vi smerta med å bryte bakoverkompatibilitet med eldre versjonar og bumpar 
modulen, opp til ny major-versjon.

## 3.1. Deprekering av API som vi vil fjerne

Sidan vi ønskjer å unngå bakoverinkompatible endringar medfører dette at ein ikkje kan gjere visse typer endringar slik 
ein kanskje ellers ville ha valgt å gjere det:

* Fjerning av klasser med public-synligheit
* Fjerning av metoder med public eller protected-synligheit
* Legge til nye parameter i metoder som er public eller protected
* Endring type for returverdi eller parameter på eksisterande metoder som er public eller protected

Og ein myriade andre typer endringar, sjå 
[Java Languge Specification - Chapter 13. Binary Compatibility](https://docs.oracle.com/javase/specs/jls/se7/html/jls-13.html) 
for ze gory detailz.

Sidan fjerning av metoder og klasser ikkje lenger er ønskelig, blir det derfor viktig å ha eit bevist forhold til 
korleis ein skal handtere slike endringar. Vi har valgt å gå for at ein annoterer metoder og klasser med @Deprecated 
for på den måten å flagge for våre klientar at dei ikkje lenger bør benyttast. Vi anbefalar derfor at alle klientar 
bygger med -Xlint:all og -Werror aktivert i maven-compiler-plugin slik at bygging bryter umiddelbart når ein benyttar 
slike metoder og klasser.

Metoder/klasser som blir annotert med @Deprecated bør ha javadoc med en @deprecated som angir versjonen dette skjedde, 
samt en beskrivelse av foretrukket, nytt alternativ.

Men når kan ein så fjerne metoder og klasser som er markert med @Deprecated? Vårt svar er at vi godtar at det skjer ved 
første påfølgjande major-oppgradering av modulen etter at deprekeringa er gjort. Dvs, om ei metode/klasse blir deprekert 
i versjon 2.1.0, godtar vi at den blir fjerna frå og med versjon 3.0.0.

## 3.2. Handheving

For å handheve at modulen er bakoverkompatibel med gjeldande major-versjon, har vi valgt å integrere 
[japicmp-maven-plugin](http://siom79.github.io/japicmp/) inn i Maven-bygget for modulen. I situasjonar der ein bevist, 
eller ubevist har brutt bakoverkompatibilitet med første release av gjeldande majorversjon (X.0.0), vil bygget bryte 
som illustrert nedanfor:


    [INFO] --- maven-source-plugin:2.4:jar (attach-sources) @ tidsperiode-underlag-lib ---
    [INFO] Building jar: C:\wses\ws_t\tidsperiode-underlag-lib\target\tidsperiode-underlag-lib-2.1.1-SNAPSHOT-sources.jar
    [INFO]
    [INFO] --- japicmp-maven-plugin:0.6.2:cmp (check-binary-compatibility) @ tidsperiode-underlag-lib ---
    [INFO] Written file 'C:\wses\ws_t\tidsperiode-underlag-lib\target\japicmp\check-binary-compatibility.diff'.
    [INFO] Written file 'C:\wses\ws_t\tidsperiode-underlag-lib\target\japicmp\check-binary-compatibility.xml'.
    [INFO] Written file 'C:\wses\ws_t\tidsperiode-underlag-lib\target\japicmp\check-binary-compatibility.html'.
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD FAILURE
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 18.415s
    [INFO] Finished at: Fri Jan 29 14:50:51 CET 2016
    [INFO] Final Memory: 24M/168M
    [INFO] ------------------------------------------------------------------------
    [ERROR] Failed to execute goal com.github.siom79.japicmp:japicmp-maven-plugin:0.6.2:cmp (check-binary-compatibility) on project tidsperiode-underlag-lib: Breaking the build because there is at least one modified class: underlag.no.spk.tidsserie.tidsperiode.Underlagsperiode -> [Help 1]

For å rette opp i denne typen feil bør ein derfor gjere om på endringa slik at den bli bakoverkompatibel. F.eks.:

* Har fjerna ei metode -> Legg den inn igjen som @Deprecated og indiker kva som erstattar den fjerna metoda
* Endring av navn på klasse eller metode -> Behold gamalt navn som @Deprecated og legg til ny metode med nytt navn/ny klasse med nytt navn, indiker at ny metode/klasse erstattar den gamle
* Har lagt til ekstra parameter på metode -> Behold gamal metode med opprinnelig antall parameter og legg til ny metode med samme navn men anna antall parameter, vurder om gamal metode skal bli @Deprecated
* Har redusert synligheit på klasse eller metode frå public til something else, reverter endringa.

Har ein derimot i samråd med alle teama som blir påvirka, bevist valgt å bryte bakoverkompatibiliteten for modulen må 
ein gjere følgjande:

* Bumpe opp modulen til ny major-versjon (endre version frå X.N.N-SNAPSHOT til X+1.0.0-SNAPSHOT
* Endre pluginets konfigurasjon til å verifisere mot den nye SNAPSHOT-versjonen (X+1.0.0-SNAPSHOT)
* I etterkant av release av X+1.0.0-SNAPSHOT, endre pluginet til å verifisere mot release-versjon av modulen (X+1.0.0)


    <plugin>
        <groupId>com.github.siom79.japicmp</groupId>
        <artifactId>japicmp-maven-plugin</artifactId>
        <version>0.6.2</version>
        <configuration>
            <oldVersion>
                <dependency>
                    <groupId>${project.groupId}</groupId>
                    <artifactId>${project.artifactId}</artifactId>
                    <!-- NB: Ved binært-inkompatible endringar som fører til bump -->
                    <!-- i versjonsnummer på modulen må også den her bumpast opp -->
                    <version>2.0.0-SNAPSHOT</version>
                </dependency>
            </oldVersion>
            ...
        </configuration>
    </plugin>
