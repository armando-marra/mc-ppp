# Stato dei miglioramenti

Aggiornato al 13 novembre 2026.

## Punto di ripresa corrente — release leggera su tag

Versione del progetto aggiornata a **0.0.10**, con sezione dedicata nel changelog.
L'utente ha autorizzato commit e push su `devel`, quindi integrazione in `main`
solo locale. **Il push di `main` resta in attesa di un'ulteriore indicazione
dell'utente.** Nessun tag o nuova release è richiesto in questo incremento.

Verifica della versione `0.0.10`: sei test Python e actionlint superati;
build locale completa riuscita con **35/35 GameTest** e controllo dei JAR e
checksum riuscito. Il primo tentativo di build ha fallito il GameTest
`cross_dimension_game_tests_overworld_to_end` sulla visibilità dell'entità
al tick 2; la ripetizione senza modifiche al codice è passata. Resta quindi
un caso intermittente da approfondire separatamente, senza attribuire al
singolo esito positivo una garanzia di stabilità della fixture.

Questa sezione sostituisce la proposta CI storica sotto. Il repository è pubblico;
l'utente ha scelto un solo workflow di release, senza rieseguire in Actions i
test già eseguiti in locale. Nessun workflow per push/PR su `devel` o `main`.
I benchmark estesi restano da parte e non sono stati rilanciati.

Implementazione in `.github/workflows/release.yml` e `scripts/release.py`:

- Trigger esclusivo sul push di tag `v*`, con validazione dei formati
  `vMAJOR.MINOR.PATCH` e `vMAJOR.MINOR.PATCH-rc.N`.
- Checkout del commit esatto; versione coerente con `gradle.properties`,
  changelog dedicato, stabile raggiungibile da `origin/main` o RC da `origin/devel`.
- Un solo job Ubuntu con Java 25, action fissate a SHA e cache Gradle base.
  Le cache create da tag hanno riuso limitato allo stesso tag: una nuova release
  può richiedere nuovi download. Loom fissato alla versione disponibile `1.19.4`.
- Solo `jar sourcesJar`: nessun GameTest, benchmark, datagen o artifact intermedio.
- Controllo dei JAR, metadata runtime, esclusione di test/benchmark e checksum;
  caricamento in bozza, controllo elenco/dimensioni allegati e pubblicazione finale.
- RC marcate prerelease; versioni stabili precedenti non promosse a Latest.
  Release esistenti, anche bozze, bloccano il job senza sovrascritture automatiche.
- Solo `GITHUB_TOKEN`, senza PAT. Resta responsabilità locale verificare la
  revisione finale prima del tag: il workflow non certifica i test locali.

Il workflow deve essere presente nel commit del tag. Nessun nuovo tag o release
è stato creato per collaudarlo. La verifica end-to-end in GitHub Actions resta
da effettuare durante il primo rilascio intenzionale.
Verifiche locali dell'incremento:

- `actionlint 1.7.12`: workflow valido, nessun errore.
- Sei test Python del flusso di release superati, con chiamate GitHub simulate:
  nessuna pubblicazione reale durante i test.
- `jar sourcesJar --dry-run`: solo compilazione e packaging, nessun test.
  Packaging reale riuscito e controllo dei due JAR/checksum superato.
- Build locale completa riuscita con Loom `1.19.4`: **35/35 GameTest**.
  È una verifica locale dell'incremento, non un task del workflow.
- `git diff --check` pulito; nessun asset modificato, datagen non necessario.

Il primo avvio Gradle locale ha incontrato `Unable to establish loopback
connection`; risolto nel solo processo usando una directory temporanea breve
per `jdk.net.unixdomain.tmpdir`. Nessun percorso personale o workaround Windows
è stato inserito nel workflow. Non è stata eseguita una build Linux da cache
vuota: tale conferma resta al primo rilascio intenzionale.

## Proposta storica superata — CI e release automatiche

Questa proposta è conservata come storico ed è superata dalla sezione corrente.
Al momento della proposta il lavoro si trovava in `D:\Projects\mc-ppp`, branch
`devel`, collegato a `origin/devel`. I commit `d7361d2` (documentazione e JFR) e
`f8349b6` (benchmark e report) sono stati pubblicati su `origin/devel`.
Le precedenti indicazioni «nessun push» descrivono il momento dei singoli
incrementi e sono superate da questo aggiornamento.

L'utente ha chiesto di documentare la proposta seguente, fare commit e push di
questo documento e fermare il lavoro per ora. **La CI non è ancora implementata:**
non sono stati creati workflow, nuovi tag, merge su main o nuove release.
La versione della mod resta `0.0.9`; `v0.0.10` sotto è soltanto un esempio.
Ultima verifica del codice: build ordinaria riuscita, **35/35 GameTest**;
benchmark concluso con 1.440 operazioni, 3.423 tick campionati e 36 controlli
di rilascio riusciti. Il report e i suoi limiti restano in
[loading-performance-results.md](docs/loading-performance-results.md).

### Obiettivo e file previsti

Implementare due workflow separati:

- `.github/workflows/ci.yml`: verifica delle modifiche a ogni push/PR pertinente.
- `.github/workflows/release.yml`: build verificata e pubblicazione automatica
  quando viene pubblicato sul remoto un tag di versione `v*`.

Mantenere le verifiche di versione, metadata e changelog in piccoli script
riutilizzabili, se necessario, evitando di duplicare logica complessa nei due
YAML. Aggiornare README, CHANGELOG e questa sezione quando i workflow saranno
effettivamente disponibili. La presente modifica è solo documentale.

### CI ordinaria: trigger, ambiente e comandi

| Evento | Comportamento proposto |
| --- | --- |
| Push su `devel` o `main` | Build completa e GameTest; JAR e diagnostica come artifact della run. |
| Pull request verso `devel` o `main` | Stesse verifiche, senza pubblicazione di release. |
| Avvio manuale | Verifica ordinaria; prevedere anche un'opzione per cache disabilitata. |

Usare inizialmente un runner GitHub-hosted Ubuntu 24.04 con Eclipse Temurin
Java 25, impostato tramite `actions/setup-java`. Usare il wrapper del repository,
non un Gradle installato separatamente. Il wrapper attuale è Gradle 9.8.2.
`gradlew` è tracciato con mode `100644`: il comando portabile per il runner
Linux è `bash ./gradlew --no-daemon build`. Un eventuale cambio del bit
eseguibile deve essere intenzionale e verificato, non un prerequisito implicito.

`build` dipende già da `check`/`runGameTest`: non aggiungere una seconda
esecuzione degli stessi GameTest nella medesima run. Il riferimento attuale è
35 test (34 della mod e uno Fabric), non una costante da congelare per sempre:
il job deve fallire per errori o test mancanti, non quando aumentano le coperture.
Controllare il report effettivo del runner e verificare che la suite sia stata
eseguita, oltre al solo codice di uscita della compilazione.

Usare `gradle/actions/setup-gradle` per cache e validazione del wrapper;
non affiancargli una seconda cache Gradle gestita da `setup-java`. Scegliere
versioni delle action attuali e compatibili al momento dell'implementazione,
fissandole a SHA verificati e annotando il riferimento di versione leggibile.
Cache in sola lettura nelle PR; scritture limitate ai branch fidati. Per le
verifiche ordinarie usare `contents: read`; cancellare run obsolete dello stesso
branch/PR con una regola di concurrency, senza applicarla alle pubblicazioni.

Non trasferire nel workflow i percorsi personali o i workaround Windows per
`GRADLE_USER_HOME` e `jdk.net.unixdomain.tmpdir`: sono dettagli dell'ambiente
locale, non requisiti di una build Ubuntu.

### Artifact e diagnostica della CI

- Conservare JAR della mod e sorgenti prodotti in `build/libs`, associati alla
  revisione verificata. Questi sono artifact CI, non release pubbliche.
- Caricare con `if: always()` i report XML dei GameTest, i log del server e gli
  eventuali crash report. Individuare e verificare i percorsi realmente prodotti
  da Fabric/Loom su Linux; non inventarli da quelli di un altro progetto.
- Prevedere una retention esplicita, inizialmente 14 giorni, e un timeout del job,
  inizialmente 30 minuti, da correggere in base alla prima esecuzione da cache vuota.
- Non caricare mondi interi, cache Gradle, registrazioni JFR o directory `build`
  indiscriminate: selezionare JAR e file diagnostici utili.
- Il benchmark prestazionale resta manuale e separato. I tempi dei runner
  condivisi non costituiscono una soglia affidabile per una CI bloccante.

### Prerequisito: build riproducibile senza cache locale

`gradle.properties` dichiara ancora `loom_version=1.19-SNAPSHOT`. Prima di
considerare affidabile la pubblicazione automatica, verificare un rilascio Loom
compatibile e disponibile e fissare quella versione. Il log locale `1.19.4`
non dimostra da solo che quell'esatta coordinata di release sia pubblicata:
controllare il repository ufficiale e risolvere davvero le dipendenze.
Conservare le versioni esplicite di Minecraft, Loader e Fabric API; non
aggiornare insieme lo stack per il solo scopo di introdurre la CI.

Eseguire almeno una build da un Gradle User Home nuovo, senza ripristino della
cache Actions e senza build cache; usare una directory isolata invece di
cancellare la cache personale. Verificare compilazione, suite completa e
packaging anche sul runner Linux. La cache deve accelerare una build che
funziona già senza di essa. La conferma di questo requisito deve riportare
il risultato effettivo, non essere dedotta dalle build Windows già riuscite.

### Release: attivazione e convenzioni dei tag

Usare `push` con filtro sui tag `v*`, quindi validare esplicitamente il nome:
il filtro è più ampio di un formato di versione corretto. Convenzioni iniziali:

| Tag | Versione nel progetto | Pubblicazione |
| --- | --- | --- |
| `v0.0.10` | `mod_version=0.0.10` | Release stabile, da un commit presente su `main`. |
| `v0.0.10-rc.1` | `mod_version=0.0.10-rc.1` | Prerelease; può provenire da `devel` e non deve diventare la release stabile Latest. |

Accettare inizialmente versioni `vMAJOR.MINOR.PATCH` ed eventuale suffisso
`-rc.N`, con componenti numerici validi; rifiutare altri formati finché non
saranno previsti esplicitamente. I tag possono essere creati localmente e
pubblicati con Git oppure tramite le funzionalità GitHub che creano il tag sul
remoto. Il trigger scelto è il tag: non aggiungere contemporaneamente un flusso
`release: published` che esegua una seconda build/pubblicazione dello stesso tag.
La creazione di una release per un tag già esistente è un evento diverso e non
va usata come scorciatoia per il flusso documentato.

Il workflow deve essere presente nel commit puntato dal tag. Fare checkout del
tag/commit esatto, recuperando i riferimenti necessari per verificare `main`;
non costruire implicitamente HEAD del branch predefinito. Per i tag stabili,
controllare che il commit sia raggiungibile da `origin/main`; il nome del tag
non prova l'appartenenza a un branch. Tag nuovi solo dopo i controlli del branch.

### Gate prima della pubblicazione

1. Validare il formato del tag e confrontarne la versione, senza la `v`, con
   `mod_version` in `gradle.properties`. Bloccare il job se divergono; non
   correggere o committare la versione automaticamente durante la release.
2. Per una stabile verificare l'appartenenza a `main`; per una RC applicare la
   convenzione prerelease esplicita. Verificare che il changelog contenga la
   sezione di quella versione, anziché pubblicare direttamente `[Unreleased]`.
3. Eseguire build e GameTest sul commit del tag. Generare gli allegati una sola
   volta; la pubblicazione scarica gli artifact di quella stessa run/revisione,
   senza ricostruirli su un altro checkout o con altre dipendenze.
4. Ispezionare il JAR: versione corretta in `fabric.mod.json`, nome coerente con
   la versione e assenza delle classi GameTest/benchmark nel pacchetto distribuito.
   Verificare la presenza del JAR dei sorgenti e calcolare i checksum SHA-256.
5. Estrarre le note dalla sola sezione della versione in `CHANGELOG.md` e passare
   il testo tramite file, conservando le righe reali. Nessuna sostituzione delle
   note con contenuto generato che perda i limiti descritti nel changelog.

Il versionamento resta responsabilità del commit di preparazione: al momento
la mod è `0.0.9`, quindi creare `v0.0.10` sul codice attuale deve far fallire
il controllo, non produrre un JAR etichettato con la versione sbagliata.

### Pubblicazione, permessi e ripetizione della run

Prevedere un job di pubblicazione con dipendenza esplicita dai controlli/build
riusciti. Solo quel job riceve `contents: write`, usando il `GITHUB_TOKEN` della
run tramite GitHub CLI; per questo flusso nello stesso repository non è previsto
un PAT personale. Validare comunque i permessi effettivi del repository.

Creare la release in bozza con il tag remoto già verificato, caricare tutti gli
allegati, controllarne l'elenco e rendere pubblica la release solo alla fine.
La bozza è una fase tecnica del job automatico: lo stato finale desiderato è
pubblicato, senza un'ulteriore approvazione manuale. Allegati previsti:

- `pointportals-<versione>.jar`;
- `pointportals-<versione>-sources.jar`;
- `SHA256SUMS.txt`, relativo ai due JAR allegati.

Impostare correttamente stabile/prerelease e la regola Latest: una RC non deve
promuoversi a stabile; decidere esplicitamente come gestire eventuali stabili
precedenti all'ultima già pubblicata. Non limitarsi all'ordine lessicografico.
Serializzare le run sullo stesso tag senza annullare una pubblicazione in corso.

Se la release è già pubblicata, fermarsi con una diagnosi chiara e non cancellare,
sovrascrivere o spostare tag/allegati. Per una bozza lasciata dalla stessa run
fallita, prevedere recupero controllato verificando tag, revisione e allegati,
oppure un errore esplicito che descriva cosa riprendere. Non mascherare fallimenti
di caricamento pubblicando una release incompleta. Non creare release reali o
tag fittizi durante la verifica iniziale dei workflow.

### Flusso operativo desiderato

1. Sviluppare su `devel` con CI verde.
2. Preparare versione e sezione changelog in un commit coerente su `devel`.
3. Integrare su `main` tramite PR con CI riuscita.
4. Creare e pubblicare il tag di versione sul commit di `main` verificato.
5. Actions verifica, costruisce e pubblica automaticamente la GitHub Release.
6. Per una candidata, usare il tag `-rc.N` sul commit preparato di `devel`;
   per la stabile preparare la versione senza suffisso e integrare su `main`.

La configurazione dei controlli richiesti per il merge sarà valutata dopo la
prima esecuzione positiva, usando i nomi effettivi dei job. Non presumere che
aggiungere YAML abiliti automaticamente la protezione dei branch.

### Incrementi e criteri di completamento della prossima sessione

**Incremento 1 — CI ordinaria e base riproducibile:** verificare/pinnare Loom,
implementare `ci.yml`, provare cache vuota e build completa, controllare JAR e
conservazione dei log. Pubblicare su `devel` e osservare la run GitHub fino al
risultato; una verifica locale del solo YAML non dimostra che la CI funzioni.

**Incremento 2 — release automatica:** implementare `release.yml` e gli script
di validazione. Provare localmente i casi positivo, versione divergente, tag
malformato, changelog mancante e RC; controllare la selezione degli artifact e
la dipendenza del job di pubblicazione. La prima pubblicazione vera resta da
verificare quando si deciderà intenzionalmente di rilasciare una versione.
Documentare separatamente ciò che è verificato localmente, in Actions e con
una release effettiva. Un eventuale controllo datagen degli asset sarà aggiunto
con esecuzione headless verificata su Linux, senza duplicare i GameTest.

Nella ripresa proporre prima un incremento circoscritto, quindi implementare,
eseguire `gradlew build`, correggere i problemi e registrare le verifiche reali.
Non rilanciare automaticamente il benchmark lungo: i risultati e i suoi limiti
sono già conservati e questo lavoro riguarda la CI.

### Riferimenti ufficiali della proposta

- [Eventi e trigger GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#push).
- [Permessi dei workflow](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax#permissions).
- [GitHub CLI: creazione release](https://cli.github.com/manual/gh_release_create).
- [Setup Java](https://github.com/actions/setup-java).
- [Gradle Actions: setup, cache e wrapper](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md).

### Prompt da incollare nella nuova chat

> Riprendi lo sviluppo in D:\Projects\mc-ppp sul branch devel. Leggi AGENTS.md,
> MIGLIORAMENTI.md dalla sezione «Punto di ripresa corrente — proposta CI e
> release automatiche» e PROJECT_DESIGN.md. La documentazione/JFR (d7361d2) e il
> benchmark (f8349b6) sono già pubblicati su origin/devel; il benchmark ha
> validato 1.440 operazioni e l'ultima build del codice supera 35/35 GameTest.
> Verifica prima stato Git e allineamento al remoto, conservando eventuali
> modifiche locali. Avvia il primo incremento del piano CI: verifica una versione
> Loom compatibile e fissa, implementa ci.yml con Java 25, build/GameTest, artifact
> e diagnostica, e verifica anche la build senza cache. Segui il ciclo iterativo,
> aggiorna MIGLIORAMENTI.md, crea commit coerenti e fai push su devel; segui la
> run GitHub fino al risultato. Il passo successivo è release.yml secondo il
> piano documentato. Non creare tag, release reali o merge su main per collaudare
> la pipeline e non rilanciare il benchmark lungo senza necessità.

## Benchmark dei caricamenti su devel — completato

Base documentale e strumentazione registrate nel commit `d7361d2`.
Il secondo incremento aggiunge il banco GameTest opt-in, il runner con mondi
nuovi, misure di chiamate/operazioni/tick e report CSV/JSON correlati a JFR.
Codice e risultati sono raccolti nel commit `f8349b6`, pubblicato su
`origin/devel`. Nessuna modifica alla politica sincrona del trasporto.

**Campagna conclusiva:** `build/benchmarks/20261113-172415-3c7ea9ca`, 30 richieste
singole a intervallo di un tick e raffiche da 10, tre ripetizioni delle 12
combinazioni. Validati 1.440 esiti: 960 riusciti e 480 rifiuti attesi per target
nuovi/obsoleti. Correlati 1.440 eventi JFR e 3.423 tick completati. Tutte le 36
celle hanno confermato lo scaricamento finale dei target. Arrivi verificati dopo
2–5 tick; conferme di rilascio dopo 350–1856 tick. Questi ultimi valori non sono
la misura esatta del timeout dei ticket vanilla.

[Report e limiti](docs/loading-performance-results.md),
[protocollo e comandi](docs/loading-performance.md), CSV sintetici in
`docs/benchmarks/`. Log, dati grezzi, registrazione JFR, runtime, hardware,
revisione/patch e hash restano nella directory locale della campagna.

**Risultato prestazionale:** 917 chiamate getChunk oltre 5 ms; sul teletrasporto
verso chunk scaricati nel Nether p95 6034,455 ms e massimo 8281,920 ms. Raffica
peggiore: tick da 31286,638 ms. Anche i riferimenti senza nuove richieste superano
50 ms al p95: non attribuire alla sola mod tutti gli sforamenti. I dati includono
fixture, lavoro pendente e costi del vicinato richiesto dai ticket di arrivo.
Servono area interamente pregenerata, quiescenza e server dedicato a 20 TPS per
separare lettura da disco, generazione e costo incrementale prima di una scelta
definitiva fra limiti, accodamento e asincronia.

**Verifiche:** campagna con due GameTest richiesti superati (benchmark e controllo
Fabric); build ordinaria riuscita con 35/35 GameTest, selezione ordinaria delle
cinque suite ripristinata. Selezione benchmark verificata sulle risorse generate;
manifest sorgente JSON valido. I 24 file Java coincidono con gli hash della
campagna. Classi di test/benchmark assenti dal JAR distribuito e nessun asset
modificato: runDatagen non necessario. Restano i noti avvisi per risorse client
assenti e ritardi del server nelle fixture.

I primi run diagnostici interrotti sono conservati e non inclusi nel report.
Il banco è stato corretto preparando le entità prima del carico, mantenendo
attiva la partenza e attendendo l'esposizione degli arrivi con limite di 200 tick.
Il limite iniziale di pulizia di 800 tick è stato sostituito da una diagnostica
fino a 6000 tick: i ticket vanilla possono attendere che il chunk sia pronto per
il salvataggio prima di consumare il timeout. Nessun ticket viene rimosso a forza
per far passare il controllo della sua scadenza naturale.

**Prossimi incrementi:** conferma con riferimento stabilizzato e carico a
intervallo di 20 tick, confronto con il carico sostenuto e decisione sulle
ottimizzazioni. CI e verifiche con client/riavvio reale restano pendenti.

## Primo incremento su devel (stato precedente al benchmark)

Il 13 novembre 2026 è stato creato e selezionato il branch locale `devel`
partendo da main. La modifica locale preesistente di questo documento è
conservata, insieme al registro storico. Nessun commit o push in questo incremento.
Le sezioni sulla release 0.0.9 sotto descrivono lo stato precedente allo sviluppo.

- **Allineamento documentale completato:** AGENTS.md, PROJECT_DESIGN.md, README,
  profili/SOP locali e CHANGELOG allineati a Java 25, Minecraft 26.2 e mappings
  Mojang. Descritti riconciliazione differita e cooldown effettivo; rimossa la
  promessa di attivazione configurabile tramite sneak e le garanzie prestazionali
  non misurate. La modalità sneak non viene sviluppata in questo incremento.
- **Prestazioni dei caricamenti, primo incremento:** aggiunti eventi JFR per
  `tuner_source`, `link_verification` e `teleport_destination`, opzione Loom
  `-PportalProfile` e script PowerShell per conteggi, percentili ed export CSV.
  La strumentazione misura le chiamate esplicite getChunk senza cambiare ticket,
  reciprocità, trasporto o politica di caricamento.
- **Protocollo:** [loading-performance.md](docs/loading-performance.md) definisce
  fixture caricate, generate/scaricate e nuove, carico seriale e raffica, dimensioni,
  ripetizioni e budget iniziale proposto (5 ms/chiamata, 50 ms/tick). Nessuna
  ottimizzazione o asincronia scelta prima delle misure controllate.
- **Verifica con JFR:** compileJava e build con `-PportalProfile` riuscite,
  35/35 GameTest; riepilogo validato su 21 eventi reali, tutti con chunk già
  caricato. Registrazione in `build/run/gameTest/portal-gameTest.jfr` e riepilogo
  locale in `build/portal-chunk-loads.csv`. È una verifica della strumentazione,
  non un benchmark dei caricamenti da disco o della generazione.
- **Verifica senza registrazione:** anche `gradlew build` standard riuscita,
  35/35 GameTest. Nessun asset modificato: runDatagen non necessario. Restano
  gli avvisi per risorse client assenti e ritardi server nelle fixture.
  Il problema ambientale dei socket locali del JDK è stato risolto per i soli
  processi di verifica usando una directory temporanea corta nel workspace,
  senza inserire percorsi macchina nella configurazione del progetto.
- **Prossimo incremento:** automatizzare le fixture della campagna controllata,
  misurare anche i tick complessivi e confrontare chunk scaricati e nuovi.
  CI resta pendente; per richiesta dell'utente le prestazioni iniziano prima di CI.

## Stato precedente: release 0.0.9 e ripresa dopo il cambio dei permessi

Ripresa completata su main a partire da f42f11b. Accesso in scrittura a .git
verificato creando e rimuovendo un file temporaneo, senza modificare le ACL.
I passi 1–4 sono completati e separati nei quattro commit sotto, ciascuno
verificato con una build completa prima del commit. Tutti sono pubblicati su
origin/main e inclusi nella release 0.0.9.
Tutti i sette file di codice, manifest e checklist finali sono identici byte
per byte alla copia iniziale. Questo documento è aggiornato e il suo testo
precedente è conservato integralmente nel registro storico in fondo.

## Commit dei passi 1–4

| Passo | Commit effettivo | Verifica eseguita |
| --- | --- | --- |
| 1. Persistenza e base preesistente | `9cc95b2` — test: cover portal persistence and establish tuner regression suite | build riuscita, 24/24 GameTest |
| 2. Chunk scaricati e scadenza ticket | `e67aa16` — test: verify unloaded portal twins and arrival ticket expiry | build riuscita, 26/26 GameTest |
| 3. Tuner tra dimensioni | `7318be8` — test: cover tuner interactions across dimensions | build riuscita, 29/29 GameTest |
| 4. Giocatori ed End | `702dc87` — test: verify player transfers and End travel | build riuscita, 35/35 GameTest |

## Versione e pubblicazione

Il commit `b386526` aggiorna CHANGELOG.md con un avanzamento per ciascun passo:
0.0.6 per la persistenza e la base preesistente, 0.0.7 per i chunk e i ticket,
0.0.8 per il tuner tra dimensioni e 0.0.9 per giocatori ed End. La versione
della mod in gradle.properties è 0.0.9.

Push di main e del tag annotato `v0.0.9` completato. Il tag punta a `b386526`.
La [release GitHub 0.0.9](https://github.com/armando-marra/mc-ppp/releases/tag/v0.0.9)
è pubblicata come ultima versione, non come bozza o prerelease, con note e
allegati `pointportals-0.0.9.jar` e `pointportals-0.0.9-sources.jar`.
Verificata la versione 0.0.9 nel metadata del JAR e l'assenza delle classi
GameTest dal JAR distribuito. Anche la build della versione 0.0.9 ha superato
35/35 GameTest. La pubblicazione è stata verificata con `gh release view`.

Il passo 1 comprende i quattro test di persistenza, i nove test locali del
tuner, il ticket temporaneo vanilla e le correzioni delle fixture di arrivo.
Il passo 2 registra ChunkLifecycleGameTests e aggiunge il test di scadenza del
ticket nel file condiviso CrossDimensionGameTests. Il passo 3 aggiunge i tre
casi useOn tra dimensioni. Il passo 4 aggiunge due viaggi di entità da/verso
l'End e quattro viaggi di ServerPlayer tra Overworld, Nether ed End.
Manifest e checklist sono aggiornati insieme alle rispettive coperture.

Tutti i controlli git diff --check e git diff --cached --check sono puliti.
La build finale supera 35/35 GameTest (34 della mod e uno Fabric). Nessun
asset modificato: runDatagen non necessario. Restano l'avviso ambientale per
build/resources/client assente e ritardi del server durante la preparazione
dei test; nessun errore di compilazione o test fallito. Gli helper con
connessione simulata mantengono la soppressione locale dell'avviso di rimozione.

## Limiti residui

- Il vero arresto e riavvio del mondo resta manuale: i test di persistenza
  esercitano le API reali di serializzazione, senza riavviare il server.
- La connessione dei giocatori è simulata: feedback visivo, messaggi, suoni e
  conferma del cambio dimensione richiedono una verifica con client reale.
- Restano le prove manuali su entità larghe/alte, ulteriori ostacoli e pericoli,
  permanenza sul portale oltre il cooldown e ritorno dopo essersi allontanati.
- Gli scaricamenti reali e la scadenza dei ticket sono coperti dal passo 2;
  il costo dei caricamenti sincroni non è stato misurato.
- CI resta da implementare; l'allineamento documentale è completato su devel.

La checklist è in [portal-regression-checks.md](docs/portal-regression-checks.md).

## Miglioramenti ancora da implementare

1. **Allineamento documentale (completato su devel):** obiettivo originale: correggere i riferimenti a Java 21,
   Minecraft 1.21 e Yarn nei documenti, usando Minecraft 26.2, Java 25 e
   mappings Mojang. Descrivere nel README la riconciliazione differita dei
   gemelli con chunk scaricati e rimuovere le garanzie prestazionali non misurate.
   L'attivazione configurabile tramite sneak descritta nel README non è
   implementata: decidere se svilupparla o rimuovere quella promessa.
2. **Integrazione continua:** aggiungere GitHub Actions con Java 25, build,
   GameTest e conservazione dei log in caso di errore; verificare che build e
   fixture funzionino anche senza la cache locale.
3. **Prestazioni dei caricamenti (benchmark completato su devel, conferma pendente):** obiettivo originale: misurare le chiamate sincrone a getChunk
   nel tuner e nel teletrasporto con destinazioni già generate e nuove,
   definendo un carico riproducibile e un budget di tick. Valutare limiti,
   accodamento o gestione asincrona solo sulla base dei risultati.

Ordine aggiornato: documentazione e primo benchmark completati; conferma prestazionale su server dedicato e CI restano pendenti. I limiti
di verifica manuale sopra restano separati da questi interventi. I passi
1–4 non richiedono ulteriore implementazione per la copertura concordata.

## Registro storico conservato

Il testo seguente è quello della sessione precedente, conservato integralmente.
Le indicazioni su permessi bloccati, commit mancanti, push non eseguiti e attività
ancora da implementare dei passi 1–4 sono superate dallo stato corrente sopra.

# Stato dei miglioramenti

Aggiornato al 13 novembre 2026. Questo documento raccoglie le proposte emerse
dalla ricognizione del progetto e lo stato della loro implementazione.

## Ripresa nella nuova chat dopo il cambio dei permessi

Questa sezione è il punto di ripresa corrente e prevale sulle vecchie note di
riavvio più sotto. Il lavoro di implementazione e verifica automatica dei passi
1–4 è presente nel checkout `D:\Projects\mc-ppp`, branch `main`, HEAD `f42f11b`.
Ultima build completa riuscita: **35/35 GameTest**. Nessuna build lasciata in
esecuzione e nessun commit o push creato in questa sessione.

L'utente ha modificato la configurazione dei permessi e apre una nuova chat
per ricaricarla. L'ultimo tentativo nella vecchia sessione di `git add` è ancora
fallito con `Unable to create '.git/index.lock': Permission denied`.
`Test-Path .git/index.lock` ha restituito `False`: non è un lock residuo.
L'ACL della directory mostrava ancora divieti espliciti di scrittura. Verificare
i permessi effettivi nella nuova sessione, senza modificare o aggirare le ACL.

File locali da conservare integralmente:

- `src/main/java/com/project/pointportals/block/entity/TeleporterBlockEntity.java`;
- `src/gametest/java/com/project/pointportals/CrossDimensionGameTests.java`;
- `src/gametest/java/com/project/pointportals/TunerGameTests.java` (non tracciato);
- `src/gametest/java/com/project/pointportals/PersistenceGameTests.java` (non tracciato);
- `src/gametest/java/com/project/pointportals/ChunkLifecycleGameTests.java` (non tracciato);
- `src/gametest/resources/fabric.mod.json`;
- `docs/portal-regression-checks.md`;
- `MIGLIORAMENTI.md` (non tracciato).

Prossima azione autorizzata: creare **quattro commit separati**, uno per passo,
con staging selettivo dei file condivisi. Ogni commit deve avere suite registrate
coerenti e una build funzionante; non limitarsi a dividere i file arbitrariamente.
Il passo 1 può includere la base locale preesistente (nove test del tuner,
ticket di arrivo vanilla e correzioni delle fixture cross-dimensione), come già
comunicato all'utente. Ripartizione consigliata:

1. Persistenza e base preesistente: quattro nuovi casi; build attesa 24 test.
2. Chunk scaricati e scadenza dei ticket: due nuovi casi; build attesa 26 test.
3. Tuner tra dimensioni: tre nuovi casi; build attesa 29 test.
4. Giocatori ed End: sei nuovi casi; build attesa 35 test.

Le modifiche dei passi 2 e 4 condividono `CrossDimensionGameTests.java`; quelle
del passo 3 estendono `TunerGameTests.java`. Manifest e documentazione vanno
distribuiti coerentemente. Conservare il risultato finale mentre si prepara lo
staging, senza scartare il lavoro successivo. Aggiornare questo documento con
i commit effettivi e lo stato conclusivo. Nessun push richiesto.

Per Gradle, se la cache torna a risolversi in `C:\.gradle`, usare solo nel
processo PowerShell `$env:GRADLE_USER_HOME = 'C:\Users\armando.marra\.gradle'`
prima di `.\gradlew.bat build`. Non inserire percorsi personali nel repository.
Il vero riavvio del mondo e le verifiche dal client restano manuali e non sono
dimostrati dai GameTest: mantenerli espliciti nel resoconto.

Prompt da incollare nella nuova chat:

> Riprendi in D:\Projects\mc-ppp. Leggi AGENTS.md e MIGLIORAMENTI.md, iniziando
> dalla sezione «Ripresa nella nuova chat dopo il cambio dei permessi». Ho
> aggiornato i permessi per consentire i commit. Conserva tutte le modifiche
> locali: i passi 1–4 sono implementati e l'ultima build ha superato 35/35
> GameTest. Verifica l'accesso a .git e crea quattro commit separati e coerenti,
> uno per passo, con le verifiche indicate nel documento. Aggiorna
> MIGLIORAMENTI.md con i commit effettivi e gli eventuali limiti residui.
> Non fare push. Non richiedere nuovamente l'autorizzazione ai commit.

## Esecuzione autorizzata dei passi 1–4

### Stato attuale dopo la verifica finale

| Passo | Implementazione e prova automatica | Limiti residui |
| --- | --- | --- |
| 1. Persistenza | Quattro nuovi GameTest sulle API reali di salvataggio/caricamento di BlockEntity e ItemStack; dati mancanti, malformati e dimensione inesistente. | Il vero arresto e riavvio del mondo resta una prova manuale distinta. |
| 2. Chunk scaricati | Scaricamento reale, conservazione del legame senza caricamenti, rottura con gemello scaricato, ricaricamento e riconciliazione; ticket dopo un vero trasferimento scaduto con scaricamento verificato. | Nessuna misura prestazionale richiesta in questo passo. |
| 3. Tuner tra dimensioni | Tre scenari con `useOn` reale: collegamento/scollegamento, destinazione occupata e sorgente distrutta. | Feedback client da verificare manualmente. |
| 4. Giocatori ed End | Due viaggi di entità da/verso l'End e quattro viaggi di ServerPlayer: Overworld/Nether e Overworld/End in entrambi i sensi. | Connessione simulata; interfaccia e conferma del cambio dimensione da client reale non verificate. |

**Verifica finale: `.\gradlew.bat build` riuscita, 35/35 GameTest** (34 della
mod e uno Fabric). I test di trasporto verificano posizione, rotazione, UUID,
assenza di duplicati alla partenza, cooldown, rifiuto del ritorno immediato e
reciprocità. Quelli dei giocatori controllano anche identità dell'oggetto,
dimensione attuale e presenza unica nella lista server. Le fixture rimuovono
il giocatore dalla lista e puliscono entrambi i livelli. Gli avvisi di rimozione
dell'helper con connessione simulata sono soppressi localmente con una nota
sul motivo; rimangono gli avvisi dell'ambiente e i ritardi durante la generazione
dei chunk di test. Nessun asset modificato; `runDatagen` non necessario.

**Commit ancora da creare:** la richiesta di un commit per passo resta valida,
ma `.git` è in sola lettura nell'ambiente. Le modifiche di tutti i passi sono
conservate nel checkout e il ramo resta a `f42f11b`; nessun push eseguito.
Per riprendere i commit serve una sessione con accesso in scrittura ai metadata
Git. Non considerare la consegna completa finché non sono stati creati i commit
richiesti. Le note sotto sono il registro cronologico degli incrementi e possono
descrivere stati precedenti.

Il 13 novembre è stata autorizzata l'esecuzione dei passi 1–4 con un commit
alla conclusione di ciascun passo. Le indicazioni successive di attendere
autorizzazione per implementare o creare commit sono quindi superate.

Passo 1, copertura di persistenza: aggiunta `PersistenceGameTests`, registrata
nella mod di test, con quattro scenari. Verifica salvataggio con metadata e
ricostruzione delle BlockEntity, ripristino dei riferimenti reciproci,
caricamento di dati assenti/malformati sopra uno stato precedentemente valido,
riconciliazione del BlockState, portale scollegato, dimensione inesistente e
serializzazione dell'intero ItemStack del tuner con il suo Data Component.
Build riuscita: **24/24 GameTest**. Questi test esercitano le API reali di
persistenza; non dimostrano un vero arresto e riavvio del server, che resta
una verifica manuale da eseguire. Nessuna modifica di produzione necessaria
per questi quattro nuovi casi. Durante la suite è comparso anche un avviso
temporaneo di ritardo del server, senza test falliti.

Il commit previsto del passo 1 deve includere le modifiche locali pregresse
descritte sotto, oltre alla nuova suite e alla documentazione. Il tentativo
di `git add` e `git commit` è fallito: l'ambiente concede sola lettura a `.git`
e impedisce la creazione di `.git/index.lock`. Nessun commit creato e nessuna
modifica scartata. I passi 2–4 restano in corso;
nessuna verifica client è stata eseguita. Le sezioni sul vecchio punto di
ripresa descrivono la situazione precedente a questa esecuzione.

Passo 2, avanzamento: aggiunta `ChunkLifecycleGameTests`. Il test attende un
vero scaricamento del chunk remoto nel Nether, verifica per almeno 45 tick
che la verifica del legame non lo ricarichi, distrugge la sorgente senza
caricare il gemello, ricarica il chunk e controlla persistenza e riconciliazione
del riferimento obsoleto. **Build riuscita con 25/25 GameTest**. Resta da
aggiungere la verifica della scadenza del ticket dopo un trasferimento reale.
La build è terminata; nessun processo Gradle lasciato in esecuzione.

Prossime azioni: completare la copertura dei ticket del passo 2, implementare
il tuner tra dimensioni del passo 3 e giocatori/End del passo 4. Non considerare
concluso l'obiettivo complessivo. I commit per passo rimangono da creare quando
la directory `.git` sarà scrivibile; non tentare aggiramenti dei permessi.

Aggiornamento successivo: **passo 2 verificato**, incluso trasferimento reale,
visibilità dell'entità dopo due tick e scaricamento del chunk remoto dopo
350 tick, senza ricaricamenti causati dalla verifica del legame. Build 26/26.
La destinazione del test dei ticket è isolata dalle altre fixture; la pulizia
ricarica esplicitamente il chunk solo dopo le asserzioni sullo scaricamento.

**Passo 3 verificato**: tre scenari aggiunti a `TunerGameTests`, con vero
`useOn` in Overworld e Nether: collegamento e scollegamento, rifiuto di una
destinazione occupata preservando la coppia e la selezione, e sorgente distrutta
con cancellazione della selezione obsoleta. Il giocatore simulato raggiunge
il Nether tramite il trasferimento vanilla; la pulizia lo rimuove anche dalla
lista server. Reciprocità e alimentazione sono controllate per dimensione.
Build finale di questo incremento: **29/29 GameTest**, nessun processo rimasto
in esecuzione. Il test usa `makeMockServerPlayerInLevel`, attualmente deprecato
ma dotato della connessione simulata richiesta dal trasferimento; questo genera
un avviso di compilazione. Rimane il passo 4: trasporto attraverso il portale
di giocatori e viaggi da/verso l'End. I commit sono ancora impediti dai permessi
di `.git`; non sono stati creati né dichiarati completati.

## Completati e integrati

Le seguenti modifiche sono nel commit `f42f11b`, integrato in `main` e pubblicato
su `origin`.

| Miglioramento | Risultato |
| --- | --- |
| Build portabile | Rimosso il percorso personale `org.gradle.java.home`; dichiarata la toolchain Java 25 e aggiornate le istruzioni nel README. |
| Sicurezza dell'arrivo | Controllo del volume completo dell'entità, delle collisioni parziali, di fuoco e lava nel volume di arrivo e del confine del mondo. |
| Recupero degli stati incoerenti | Il tick ripristina un portale con `LINKED=true` ma senza destinazione e si interrompe quando la verifica invalida il collegamento. |
| Protezione dai riferimenti obsoleti | La rimozione di A scollega B solo se B punta ancora ad A; una coppia B–C valida viene preservata. |
| Scollegamento condiviso | Il tuner riutilizza il controllo di reciprocità impiegato alla rimozione del portale. |
| Distribuzione delle verifiche | I controlli restano ogni 40 tick, con una fase determinata dalla posizione per ridurre la concentrazione del lavoro. |
| Rotazione al teletrasporto | Gli angoli vengono passati come valori assoluti, evitando di sommarli una seconda volta. |
| Infrastruttura GameTest | Source set separato, mod di test e suite server eseguita anche da `build`; i test non entrano nel JAR distribuito. |

### Copertura automatica integrata

Dieci scenari della mod verificano:

- collegamenti reciproci, rimozione con riferimento obsoleto e rottura del gemello;
- recupero di `LINKED` senza destinazione;
- rifiuto dell'arrivo su una lastra, trasporto con cooldown e perdita di Redstone;
- Overworld → Nether e Nether → Overworld: posizione, rotazione, UUID,
  unicità alla partenza, cooldown e conservazione del collegamento;
- rifiuto di una destinazione ostruita nel Nether.

Prima del commit la suite ha superato 11 GameTest: dieci della mod e un controllo
Fabric. Il JDK 25 installato nel sistema è stato verificato con una build riuscita.

## Implementati localmente: incremento corrente

- Nove GameTest del tuner: selezione con Data Component, collegamento e stato
  Redstone, auto-collegamento, destinazione occupata, sorgente occupata nel
  frattempo, sorgente distrutta, cancellazione della selezione, scollegamento
  reciproco e interazione con blocchi ordinari.
- Uso di un giocatore simulato in sopravvivenza e chiamata al vero `useOn` dello
  strumento. Verifica dei risultati, dei componenti e dei BlockState.
- Ricerca dell'entità nella sola dimensione di arrivo nei test tra dimensioni,
  invece della ricerca globale per UUID; pulizia esplicita delle entità di test
  nelle due dimensioni.
- Trasferimento tramite `TeleportTransition.PLACE_PORTAL_TICKET`: il ticket
  vanilla mantiene temporaneamente caricata e attiva la destinazione, anche
  senza giocatori, e scade automaticamente dopo 300 tick (15 secondi).
- Controlli di arrivo differiti di due tick, con la coppia di portali mantenuta
  fino alla verifica. Pulizia centralizzata di entità, portali e alimentazione
  anche se un'asserzione fallisce, sia durante la preparazione sia nel callback.
- Aggiornamento della guida alle verifiche in `docs/portal-regression-checks.md`.

### Diagnosi e verifica del trasferimento Overworld → Nether

Riprodotto il guasto iniziale: 19 GameTest riusciti e ricerca dell'entità nel
Nether fallita al tick 0. Il solo rinvio della query al tick 2 fallisce ancora.
L'indagine sul codice della versione Minecraft 26.2 usata dalla build ha
mostrato che `getChunk(...)` carica i blocchi con un ticket `UNKNOWN`, di breve
durata e senza simulazione, mentre `Entity.teleportTo(...)` usa una transizione
con callback `DO_NOTHING`. Manca quindi il ticket di arrivo che mantiene attiva
una destinazione senza giocatori; l'entità trasferita può restare esclusa dalle
query delle sezioni accessibili.

La correzione usa `Entity.teleport(new TeleportTransition(...))` con il callback
vanilla `PLACE_PORTAL_TICKET`, senza caricamenti forzati permanenti. Il test
attende due tick per interrogare la destinazione dopo l'aggiornamento del server,
mantenendo i controlli su posizione esatta, rotazione, UUID, assenza di duplicati
alla partenza, cooldown minimo di 40 tick, rifiuto del ritorno e reciprocità.

Verifiche del 13 novembre 2026:

- `.\gradlew.bat runGameTest`: 20/20 GameTest riusciti dopo la correzione.
- `.\gradlew.bat build`: **BUILD SUCCESSFUL**, suite completa 20/20 riuscita
  (19 test della mod, inclusi i nove del tuner, e un controllo Fabric).
- `git diff --check`: nessun errore di whitespace.
- Nessun asset modificato: `runDatagen` non necessario per questo incremento.

Restano gli avvisi di avvio dell'ambiente di test sul percorso delle risorse
client assente e sulle mappings non presenti; non ci sono errori di compilazione.
Le modifiche locali precedenti sono conservate e nessun commit è stato creato.

### Punto di ripresa dopo il riavvio

Indagine ripresa e conclusa dopo il riavvio: il difetto di arrivo nel Nether è
corretto e la build completa è verde. Nessuna build è in esecuzione.
Checkout: `D:\Projects\mc-ppp`, branch `main`, ultimo commit `f42f11b` già
pubblicato. Il vecchio worktree è stato rimosso.

Modifiche locali da conservare:

- `src/gametest/java/com/project/pointportals/TunerGameTests.java`: nove nuovi test;
- `src/gametest/resources/fabric.mod.json`: registrazione della nuova suite;
- `src/gametest/java/com/project/pointportals/CrossDimensionGameTests.java`:
  ricerca nella destinazione dopo due tick e gestione della pulizia della coppia;
- `src/main/java/com/project/pointportals/block/entity/TeleporterBlockEntity.java`:
  transizione con ticket temporaneo vanilla;
- `docs/portal-regression-checks.md`: copertura aggiornata;
- questo documento, `MIGLIORAMENTI.md`.

Nessuna correzione pendente per il guasto al tick 0. Il prossimo incremento va
scelto tra le attività proposte sotto. Creare un commit solo quando richiesto;
non scartare le modifiche locali.

Prompt suggerito per una nuova sessione:

> Riprendi il lavoro in D:\Projects\mc-ppp. Leggi AGENTS.md e MIGLIORAMENTI.md,
> soprattutto il punto di ripresa dopo il riavvio. Conserva le modifiche locali.
> Il difetto Overworld → Nether è stato corretto con il ticket temporaneo vanilla
> e i controlli di arrivo dopo due tick. La build completa ha superato tutti i
> 20 GameTest. Nessun commit creato: attendi indicazioni sul prossimo incremento.

## Proposta operativa dopo la ricognizione del 13 novembre 2026

Richiesta corrente: leggere le istruzioni, proporre i prossimi passi e aggiornare
questo documento. Nessuna nuova funzionalità implementata in questa ricognizione;
le modifiche locali dell'incremento precedente vanno conservate, senza creare
commit finché non richiesto.

Il controllo del checkout conferma `main` al commit `f42f11b` e i file locali
elencati sopra. `MIGLIORAMENTI.md` e `TunerGameTests.java` sono ancora non
tracciati. La verifica della pubblicazione su `origin` non è stata ripetuta.

### Ordine consigliato degli incrementi

1. **Persistenza: primo incremento consigliato.** Aggiungere una suite dedicata
   in `src/gametest/java/com/project/pointportals/` e registrarla nella mod di
   test. Esercitare il salvataggio e caricamento effettivi della BlockEntity
   (`saveAdditional`/`loadAdditional`) e dell'ItemStack con il Data Component,
   senza limitarsi al codec isolato. Verificare coppia reciproca, coordinate e
   dimensione, portale non collegato, `twin_target` assente o malformato e
   dimensione formalmente valida ma inesistente. Controllare la riconciliazione
   del BlockState dopo il caricamento. Toccare `TeleporterBlockEntity`,
   `PortalTarget` o i componenti solo se un test evidenzia un difetto.
   Completamento: nuovi casi e suite esistente verdi; il semplice ciclo di
   serializzazione non sostituisce la prova di chiusura e riapertura del mondo,
   che resta manuale finché non viene automatizzato un vero riavvio.
2. **Chunk scaricati e riconciliazione.** Preparare test con chunk realmente
   scaricati, dimostrando la precondizione. Verificare che il controllo periodico
   con `verifyAndSyncLink(false)` conservi il legame senza caricare il gemello;
   dopo la rottura con l'altro capo scaricato, verificare lo scollegamento quando
   il gemello mancante torna ispezionabile. Controllare anche che i ticket
   temporanei di arrivo scadano e non mantengano caricamenti permanenti. Non
   simulare lo scaricamento rimuovendo soltanto una BlockEntity.
3. **Tuner tra dimensioni.** Estendere `TunerGameTests` usando il vero `useOn`:
   selezione in Overworld, collegamento nel Nether, reciprocità, stato Redstone,
   consumo della selezione e scollegamento. Includere riferimenti obsoleti e
   portali già occupati; riusare la pulizia delle fixture tra dimensioni.
4. **Giocatori ed End.** Separare in incrementi distinti il trasporto di un
   giocatore e i viaggi da/verso l'End. Verificare posizione, rotazione, UUID,
   cooldown e assenza di duplicati; mantenere una verifica client per il cambio
   dimensione e i messaggi, che il solo giocatore simulato non dimostra.
5. **Costo dei caricamenti.** Misurare le chiamate sincrone a `getChunk(...)`
   presenti nel teletrasporto e nel tuner, con destinazioni già generate e
   nuove, prima di scegliere limiti, accodamento o gestione asincrona. Definire
   carico e budget di tick riproducibili; evitare una riscrittura preventiva.
6. **Integrazione continua.** Riprodurre la build Java 25 e i GameTest in GitHub
   Actions, conservando i log in caso di errore. Verificare prima che il mondo
   di test e le fixture non dipendano dalla cache locale.

**Allineamento documentale da anticipare:** in un piccolo incremento dedicato,
correggere Java 21/Yarn 1.21.4 nel punto 3.2 di `AGENTS.md` e i riferimenti
obsoleti in `PROJECT_DESIGN.md`, seguendo Minecraft 26.2, Java 25 e i nomi
Mojang effettivamente usati dalla build. Aggiornare il README sulle opzioni
non implementate (attivazione configurabile con sneak) e sulle garanzie ancora
da verificare (assenza di blocchi durante il caricamento dei chunk). Precisare
che la riconciliazione della rottura può essere differita con chunk scaricati.
Questi file non sono stati modificati nella richiesta corrente.

Per ogni incremento: piano circoscritto, implementazione, `build`, revisione e
aggiornamento di questo documento; `runDatagen` solo quando cambiano asset o
dati. Concordare l'incremento successivo prima di avviarlo. Non introdurre in
un'unica modifica persistenza, caricamento chunk e nuova logica di trasporto.

### Verifica della ricognizione

Nuova esecuzione di `.\gradlew.bat build`: **BUILD SUCCESSFUL**, tutti i 20
GameTest superati (19 della mod e un controllo Fabric). Restano gli avvisi già
noti sulle risorse client assenti e sulle mappings non presenti. Nessun asset
modificato: `runDatagen` non necessario.

Il primo tentativo del wrapper si è fermato prima di avviare Gradle perché
cercava la cache in `C:\.gradle`, non scrivibile nell'ambiente corrente. Il
comando è riuscito impostando `GRADLE_USER_HOME=C:\Users\armando.marra\.gradle`
solo nel processo PowerShell, senza aggiungere percorsi personali alla
configurazione del repository. Nessuna build rimane in esecuzione.

## Proposti, ancora da iniziare

| Priorità | Attività | Criterio di completamento |
| --- | --- | --- |
| Alta | Persistenza e riavvio | Test di salvataggio e caricamento dei collegamenti, inclusi dati mancanti o invalidi. |
| Alta | Chunk scaricati | Verificare conservazione del legame, distruzione del gemello e successiva riconciliazione senza caricamenti continui. |
| Alta | Tuner tra dimensioni | Collegare e scollegare due portali in dimensioni diverse usando il vero strumento. |
| Alta | Trasporto di giocatori e viaggi nell'End | Estendere la copertura oltre i maiali e la coppia Overworld–Nether. |
| Media | Caricamenti sincroni dei chunk | Misurare l'impatto di `getChunk(...)` sui tick e progettare una gestione con limiti e rilascio delle risorse. |
| Media | Integrazione continua | Workflow GitHub Actions con Java 25, build, GameTest e conservazione dei log in caso di errore. |
| Media | Allineamento documentazione | Aggiornare `PROJECT_DESIGN.md` e risolvere i riferimenti discordanti a Java 21/Yarn in `AGENTS.md`; correggere nel README le funzionalità descritte ma non implementate. |

## Verifiche manuali ancora necessarie

- Feedback visivo, messaggi, suoni e interazioni dal client.
- Arrivo di entità larghe o alte, ulteriori ostacoli parziali e pericoli.
- Permanenza sul portale oltre il cooldown e successivo ritorno dopo essere usciti.
- Scenari di scaricamento dei chunk e riavvio del mondo finché non automatizzati.

La checklist dettagliata è in [portal-regression-checks.md](docs/portal-regression-checks.md).

## Comandi

```powershell
.\gradlew.bat build        # Compilazione e suite GameTest server
.\gradlew.bat runGameTest  # Solo suite GameTest server
.\gradlew.bat runClient    # Verifiche manuali dal client
```

Per i prossimi incrementi: implementare una modifica circoscritta, eseguire la
suite, aggiornare questo documento e creare il commit quando richiesto.
