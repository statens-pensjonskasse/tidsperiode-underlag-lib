# faktura-prognose-tidsserie
Domene for tidsserier som benyttes ved forenklet fastsatsfakturering.

## Dokumentasjon
[Systemdokumentasjon - faktura-prognose-tidsserie](http://wiki/confluence/display/dok/Fakturamodul+-+faktura-prognose-tidsserie)

## Ofte spurte spørsmål

# 1. Korleis fiksar eg byggefeil generert av japicmp-maven-plugin?

Viss ein har vore inne og gjort endringar bør ein i så stor grad som mulig, unngå å bryte bakoverkompatibilitet med tidligare versjonar. Dersom ein har vore inne og gjort endringar uten å tenke på dette kan ein derfor fort komme til å ha gjort ei endring som bryter bakoverkompatibiliteten fordi ein har renama, fjerna eller endra på metodeparameter og/eller synligheit.

Sidan modulen er satt opp til å verifisere at den er binært bakoverkompatibel med første versjon som har samme major-versjon som gjeldande SNAPSHOT er satt til, feilar derfor bygget med følgjande type feilmelding:

    [INFO] --- maven-source-plugin:2.4:jar (attach-sources) @ faktura-prognose-tidsserie ---
    [INFO] Building jar: C:\wses\ws_t\faktura-prognose-tidsserie\target\faktura-prognose-tidsserie-2.1.1-SNAPSHOT-sources.jar
    [INFO]
    [INFO] --- japicmp-maven-plugin:0.6.2:cmp (check-binary-compatibility) @ faktura-prognose-tidsserie ---
    [INFO] Written file 'C:\wses\ws_t\faktura-prognose-tidsserie\target\japicmp\check-binary-compatibility.diff'.
    [INFO] Written file 'C:\wses\ws_t\faktura-prognose-tidsserie\target\japicmp\check-binary-compatibility.xml'.
    [INFO] Written file 'C:\wses\ws_t\faktura-prognose-tidsserie\target\japicmp\check-binary-compatibility.html'.
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD FAILURE
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 18.415s
    [INFO] Finished at: Fri Jan 29 14:50:51 CET 2016
    [INFO] Final Memory: 24M/168M
    [INFO] ------------------------------------------------------------------------
    [ERROR] Failed to execute goal com.github.siom79.japicmp:japicmp-maven-plugin:0.6.2:cmp (check-binary-compatibility) on project faktura-prognose-tidsserie: Breaking the build because there is at least one modified class: no.spk.pensjon.faktura.tidsserie.domain.tidsserie.TidsserieFacade -> [Help 1]
    
For å rette opp i denne typen feil bør ein derfor gjere om på endringa slik at den bli bakoverkompatibel. F.eks.:
* Har fjerna ei metode -> Legg den inn igjen som @Deprecated og indiker kva som erstattar den fjerna metoda
* Endring av navn på klasse eller metode -> Behold gamalt navn som @Deprecated og legg til ny metode med nytt navn/ny klasse med nytt navn, indiker at ny metode/klasse erstattar den gamle
* Har lagt til ekstra parameter på metode -> Behold gamal metode med opprinnelig antall parameter og legg til ny metode med samme navn men anna antall parameter, vurder om gamal metode skal bli @Deprecated
* Har redusert synligheit på klasse eller metode frå public til something else, reverter endringa.

Alternativt må ein gjere eit veldig bevist valg på at ein ønskjer å bryte denbakoverkompatibilitet med tidligare versjonar. I så fall må ein då bumpe opp majorversjonen til modulen frå X.N.N-SNAPSHOT til X+1.0.0-SNAPSHOT og deretter endre på oppsettet i pom.xml for japicmp-maven-plugin, til å peike mot den nye SNAPSHOT-versjonen (merk at ein her også må følge opp at ein etter release av ny major-versjon endrar innstillinga frå SNAPSHOT til release-versjon):

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
